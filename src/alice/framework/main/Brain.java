package alice.framework.main;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.reflections.Reflections;

import alice.framework.database.SharedSaveFile;
import alice.framework.features.Documentable;
import alice.framework.features.Feature;
import alice.framework.utilities.AliceLogger;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;

public class Brain {
	
	public static GatewayDiscordClient client = null;						// Discord client object
	public static AtomicBoolean ALIVE = new AtomicBoolean(true);			// Global variable to determine if shutdown is a restart command

	public static MessageChannel reportChannel;								// Hard-coded text channel to send error messages to
		// TODO: check for null
		// TODO: move to constants file
	
	/* CLASS SHOULD BE OF TYPE EVENT BUT THIS IS NOT ENFORCED */
	@SuppressWarnings("rawtypes")	// Maps Events to a list of Features they should trigger, ordered by priority
	public static AtomicReference<Map<Class, PriorityQueue<Feature>>> features = new AtomicReference<Map<Class, PriorityQueue<Feature>>>();
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		if ( args.length < 1 ) {	// Checks if a token was passed
			AliceLogger.error("Please pass the TOKEN as the first argument.");
			System.exit(0);
		}
		
		// Initialize Feature maps
		features.set(new HashMap<Class, PriorityQueue<Feature>>());
		
		while( ALIVE.get() ) {		// Primary system loop
			AliceLogger.info("Starting up...");
			
			// Cleanup in case this is a restart
			features.get().clear();
			
			login(args[0]);			// Log in to server
			reload();				// Reload guild data
			subscribeFeatures();	// Run subscription functions for features
			
			reportChannel = (MessageChannel) Brain.client.getChannelById(Snowflake.of(768350880234733568L)).block();	// Hard-coded error message channel
				// TODO: move to constants file
			
			client.onDisconnect().block();	// Wait until disconnected
			client = null;
			
			AliceLogger.info("Shutting down...");
		}
	}
	
	/**
	 * Reloads guild save data files for each registered Guild
	 */
	private static void reload() {
		AliceLogger.info("Reloading save data...");
		for( Guild guild : client.getGuilds().collectList().block() ) {
			@SuppressWarnings("unused")
			SharedSaveFile guildData = new SharedSaveFile(guild.getId().asLong());
			AliceLogger.info(String.format("Loaded guild data for %s.", guild.getName()), 1);
		}
	}
	
	/**
	 * Logs in to the Discord API server and subscribes to the client with features.
	 * @param token String used to identify client program
	 */
	private static void login(String token) {
		AliceLogger.info("Logging in...");

		client = DiscordClientBuilder.create(token).build().login().block();
		client.updatePresence(Presence.online(Activity.listening("%help"))).block();	// Sets Discord bot presence and status
			// TODO: turn into a variable
		client.on(ReadyEvent.class)	// Once the Ready event is received from the server
			.subscribe( event -> {
				AliceLogger.info("Initializing Features...", 1);
				for( String whitelisted : Constants.FEATURE_WHITELIST ) {	// Iterate through each directory containing feature classes
					loadFeatures(whitelisted);	// Load the features contained within those feature classes
				}
			});
		
		AliceLogger.info("Log in successful.");
	}
	
	/**
	 * Loads all features contained within a given directory.
	 * @param includePrefix String prefix to check to verify that a feature is contained within a given directory
	 */
	@SuppressWarnings("rawtypes")
	private static void loadFeatures(String includePrefix) {
		Reflections include = new Reflections(includePrefix);	// Classes included within the specified directory
		Set<Class<? extends Feature>> excluded = new HashSet<Class<? extends Feature>>();	// Classes to exclude
		Reflections exclude = null;
		
		// Track excluded features in a set
		if( Constants.FEATURE_BLACKLIST.length > 0 ) {
			exclude = new Reflections(Constants.FEATURE_BLACKLIST[0]);
		}
		for( int f=1; f<Constants.FEATURE_BLACKLIST.length; f++ ) {
			exclude.merge(new Reflections(Constants.FEATURE_BLACKLIST[f]));
		}
		if( exclude != null ) {
			excluded = exclude.getSubTypesOf(alice.framework.features.Feature.class);
		}
		
		// Load included features that are not in the excluded feature set
		for( Class<?> c : include.getSubTypesOf(alice.framework.features.Feature.class) ) {
			if( exclude != null && excluded.contains(c) ) {
				continue;
			}
			try {
				AliceLogger.info(String.format("Loaded feature: %s", c.getName()), 2);
				c.getConstructor().newInstance();
			} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				AliceLogger.error(String.format("An error occured while instantiating %s.", c.getName() ), 2);
			}
		}
	}
	
	/**
	 * Subscribe Features and dictate their execution logic when an event is received.
	 */
	@SuppressWarnings({"rawtypes"})
	private static void subscribeFeatures() {
		for( Class event : features.get().keySet() ) {					// For every Event
			subscribeEvent(event);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void subscribeEvent(Class event) {
		Brain.client.on(event).flatMap(e -> {						// When the event is received
			Mono<?> process = Mono.fromRunnable(() -> {});
			
			// TODO: handle event superclasses also needing to trigger
			if( features.get().containsKey(event) ) {
				boolean dominant = false;	// Whether a dominant Feature has activated
				boolean standard = false;	// Whether a standard Feature has activated
				for( Feature f : features.get().get(event) ) {	// For every associated Feature
					Mono<?> response = f.handle((Event) e);				// Process the event through the Feature
					if( response != null ) {
						if( f.getExclusionClass() == null ) { // Features with no ExclusionClass will activate no matter what, and not affect the activation of other Features
							process = process.and(response);
						} else {
							switch( f.getExclusionClass() ) {
								// Handle exclusion cases
								case DOMINANT:	// Dominant Features activate no matter what
									process = process.and(response);
									dominant = true;
									break;
								case STANDARD:	// Standard Features activate if no Dominant Features have
									if( !dominant ) {
										process = process.and(response);
										standard = true;
									}
									break;
								case SUBMISSIVE:	// Submissive Features activate if no Dominant or Submissive Features have
									if( !dominant && !standard ) {
										process = process.and(response);
									}
									break;
								default:	// Features with no ExclusionClass will activate no matter what, and not affect the activation of other Features
									process = process.and(response);
									break;
							}
						}
					}
				}
			}

			return process;											// Return the queue to be executed
		}).subscribe();
	}
	
	/**
	 * A function to update the bot's avatar
	 * @param url to retrieve image from
	 */
	public static void updateAvatar(String url) {
		client.edit(spec -> {
			spec.setAvatar(Image.ofUrl(url).block());
				// TODO: verify image exists
			AliceLogger.info("Avatar updated.");
		}).subscribe();
	}
	
	/**
	 * Find a Feature by its name variable
	 * @param name String to check against Feature name
	 * @return Feature associated with the given name
	 */
	@SuppressWarnings("rawtypes")
	public static Feature getFeatureByName(String name) {
		for( PriorityQueue<Feature> ff : features.get().values() ) {
			for( Feature f : ff ) {
				if( f.getName().equalsIgnoreCase(name) || f.getAliases().contains(name.toLowerCase()) ) {
					return f;
				}
			}
		}
		return null;
	}
	
	/**
	 * Find a Documentable Feature by its name variable.
	 * @param name String to check against Feature name
	 * @return Documentable Feature associated with the given name
	 */
	@SuppressWarnings("rawtypes")
	public static Feature getDocumentableByName(String name) { // TODO: replace this function with a is-documentable check wherever applicable
		for( PriorityQueue<Feature> ff : features.get().values() ) {
			for( Feature f : ff ) {
				if( f instanceof Documentable && (f.getName().equalsIgnoreCase(name) || f.getAliases().contains(name.toLowerCase())) ) {
					return f;
				}
			}
		}
		return null;
	}
	
}
