package alice.framework.main;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.reflections.Reflections;

import alice.framework.modules.Module;
import alice.framework.utilities.AliceLogger;
import alice.framework.utilities.SaveFiles;
import alina.structures.SyncedJSONObject;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.spec.UserEditSpec;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.util.Image;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Brain {
	
	public static DiscordClient client = null;
	public static GatewayDiscordClient gateway = null;						// Discord client object
	public static AtomicBoolean ALIVE = new AtomicBoolean(true);			// Global variable to determine if shutdown is a restart command

	public static MessageChannel reportChannel;								// Hard-coded text channel to send error messages to
	public static MessageChannel upkeepChannel;								// Hard-coded text channel to send upkeep messages to
		// TODO: check for null
		// TODO: move to constants file
			
	public static AtomicReference<String> token_ref = new AtomicReference<String>();
	
	public static final Intent[] INTENTS = new Intent[] {
		Intent.DIRECT_MESSAGE_REACTIONS,
		Intent.DIRECT_MESSAGES,
		Intent.GUILD_BANS,
		Intent.GUILD_INVITES,
		Intent.GUILD_MEMBERS,
		Intent.GUILD_MESSAGE_REACTIONS,
		Intent.GUILD_MESSAGES,
		Intent.GUILD_VOICE_STATES,
		Intent.GUILDS
	};
	
	public static void main(String[] args) {
		String token = System.getenv("BOT_TOKEN");
		if( token == null ) {
			if ( args.length < 1 ) {	// Checks if a token was passed
				AliceLogger.error("Please pass the TOKEN as the first argument.");
				System.exit(0);
			} else {
				token = args[0];
			}
		}

		token_ref.set(token);
				
		while( ALIVE.get() ) {		// Primary system loop
			try {
				AliceLogger.info("Starting up...");
				
				login(args[0]);			// Log in to server
				reload();				// Reload guild data
				
				upkeepChannel = (MessageChannel) Brain.gateway.getChannelById(Snowflake.of(757836189687349308L)).block();
				reportChannel = (MessageChannel) Brain.gateway.getChannelById(Snowflake.of(768350880234733568L)).block();	// Hard-coded error message channel
				
				setup();
								
				gateway.onDisconnect().block();	// Wait until disconnected
				gateway = null;
				
				AliceLogger.info("Shutting down...");
			} catch (Exception e) {
				e.printStackTrace();
				AliceLogger.info("Recovering...");
			}
		}
	}
	
	/**
	 * Reloads guild save data files for each registered Guild
	 */
	private static void reload() {
		AliceLogger.info("Reloading save data...");
		for( Guild guild : gateway.getGuilds().collectList().block() ) {
			@SuppressWarnings("unused")
			SyncedJSONObject guildData = SaveFiles.ofGuild(guild.getId().asLong());
		}
	}
	
	/**
	 * Sets up hard-coded Discord channels for maintenance purposes
	 */
	private static void setup() {
		gateway.on(ReadyEvent.class)
		.flatMap(
				event -> Flux.defer( 
						() -> upkeepChannel.createMessage("Running upkeep").and(Mono.delay(Duration.ofMinutes(5))).onErrorContinue((e, o) -> { e.printStackTrace(); }).repeat()
					)
			)
		.subscribe();
	}
	
	/**
	 * Logs in to the Discord API server and subscribes to the client with features.
	 * @param token String used to identify client program
	 */
	private static void login(String token) {
		AliceLogger.info("Logging in...");
		
		client = DiscordClientBuilder.create(token)
				.onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.any(), 1006))
				.build();
		gateway = client.gateway().setEnabledIntents(IntentSet.of(INTENTS)).login().block();
//		
//		gateway = DiscordClientBuilder.create(token)
//				.onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.any(), 1006))
//				.build().login().block();
		gateway.updatePresence(ClientPresence.online(ClientActivity.listening("%help"))).block();
			// TODO: turn into a variable
		gateway.on(ReadyEvent.class)	// Once the Ready event is received from the server
			.subscribe( event -> {
				AliceLogger.info("Initializing Modules...", 1);
				loadModules("alice.modular.modules");
//				for( String whitelisted : Constants.FEATURE_WHITELIST ) {	// Iterate through each directory containing feature classes
//					loadFeatures(whitelisted);	// Load the features contained within those feature classes
//				}
			});
		
		AliceLogger.info("Log in successful.");
	}
	
	public static Flux<Member> getMembers(Snowflake guildId) {
		return DiscordClient.create(token_ref.get())
	            .gateway()
	            .setEnabledIntents(IntentSet.of(Intent.GUILD_MEMBERS))
	            .login()
	            .flatMapMany(gateway ->
	                gateway.requestMembers(guildId)
	            );
	}
	
	@SuppressWarnings("rawtypes")
	private static void loadModules(String includePrefix) {
		Reflections include = new Reflections(includePrefix);	// Classes included within the specified directory
		Set<Class<? extends Module>> excluded = new HashSet<Class<? extends Module>>();	// Classes to exclude
		Reflections exclude = new Reflections("alice.framework.modules");
		excluded = exclude.getSubTypesOf(alice.framework.modules.Module.class);
		
		for( Class<?> c : include.getSubTypesOf(alice.framework.modules.Module.class) ) {
			if( excluded.contains(c) ) {
				continue;
			}
			try {
				c.getConstructor().newInstance();
				AliceLogger.info(String.format("Loaded Module: %s", c.getName()), 2);
			} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				AliceLogger.error(String.format("An error occured while instantiating %s.", c.getName() ), 2);
				e.printStackTrace();
			}
		}
		AliceLogger.info("Modules initialized.", 1);
	}
	
	/**
	 * A function to update the bot's avatar
	 * @param url to retrieve image from
	 */
	public static void updateAvatar(String url) {
		gateway.edit(UserEditSpec.builder().avatar(Image.ofUrl(url).block()).build())
			.onErrorResume(error -> { error.printStackTrace(); return Mono.empty(); }).subscribe();
	}
	
	/**
	 * Find a Feature by its name variable
	 * @param name String to check against Feature name
	 * @return Feature associated with the given name
	 */
//	@SuppressWarnings("rawtypes")
//	public static Feature getFeatureByName(String name) {
//		for( PriorityQueue<Feature> ff : features.get().values() ) {
//			for( Feature f : ff ) {
//				if( f.getName().equalsIgnoreCase(name) || f.getAliases().contains(name.toLowerCase()) ) {
//					return f;
//				}
//			}
//		}
//		return null;
//	}
	
	/**
	 * Find a Documentable Feature by its name variable.
	 * @param name String to check against Feature name
	 * @return Documentable Feature associated with the given name
	 */
//	@SuppressWarnings("rawtypes")
//	public static Feature getDocumentableByName(String name) { // TODO: replace this function with a is-documentable check wherever applicable
//		for( PriorityQueue<Feature> ff : features.get().values() ) {
//			for( Feature f : ff ) {
//				if( f instanceof Documentable && (f.getName().equalsIgnoreCase(name) || f.getAliases().contains(name.toLowerCase())) ) {
//					return f;
//				}
//			}
//		}
//		return null;
//	}
	
}
