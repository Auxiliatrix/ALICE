package alice.framework.main;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.reflections.Reflections;

import alice.configuration.calibration.Constants;
import alice.framework.features.ActiveFeature;
import alice.framework.features.Feature;
import alice.framework.features.HelperFeature;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.AtomicSaveFolder;
import alice.framework.utilities.AliceLogger;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;

public class Brain {
	
	public static GatewayDiscordClient client = null;
	public static AtomicBoolean ALIVE = new AtomicBoolean(true);

	public static AtomicSaveFolder guildIndex = new AtomicSaveFolder();
	public static MessageChannel reportChannel;
		
	@SuppressWarnings("rawtypes")
	public static AtomicReference<Map<Class, PriorityQueue<ActiveFeature>>> features = new AtomicReference<Map<Class, PriorityQueue<ActiveFeature>>>();
	@SuppressWarnings("rawtypes")
	public static AtomicReference<Map<Class, List<HelperFeature>>> helpers = new AtomicReference<Map<Class, List<HelperFeature>>>();
	
	public static void main(String[] args) {
		if ( args.length < 1 ) {
			AliceLogger.error("Please pass the TOKEN as the first argument.");
			System.exit(0);
		}
		while( ALIVE.get() ) {
			AliceLogger.info("Starting up...");
			
			features.get().clear();
			helpers.get().clear();
			reload();			
			login(args[0]);
			subscribeFeatures();
			
			reportChannel = (MessageChannel) Brain.client.getChannelById(Snowflake.of(768350880234733568L)).block();
			
			client.onDisconnect().block();
			client = null;
			
			AliceLogger.info("Shutting down...");
		}
	}
	
	private static void reload() {
		AliceLogger.info("Reloading save data...");

		File folder = new File(Constants.GUILD_DATA_DIRECTORY);
		if( folder.isDirectory() ) {
			for( File file : folder.listFiles() ) {
				String fileName = file.getName();
				String guildId = fileName.indexOf(".") > 0 ? fileName.substring(0, fileName.indexOf(".")) : fileName;

				String guildFile = String.format("%s%s%s%s%s.json", "tmp", File.separator, "guilds", File.separator, guildId);
				Brain.guildIndex.put(guildId, new AtomicSaveFile(guildFile));
				AliceLogger.info(String.format("Loaded guild data for %s.", guildId), 1);
			}
		}
	}
	
	private static void login(String token) {
		AliceLogger.info("Logging in...");

		client = DiscordClientBuilder.create(token).build().login().block();
		client.updatePresence(Presence.online(Activity.listening("%help"))).block();
		client.on(ReadyEvent.class)
			.subscribe( event -> {
				AliceLogger.info("Initializing Features...", 1);
				for( String whitelisted : Constants.FEATURE_WHITELIST ) {
					loadFeatures(whitelisted);
				}
			});
		
		AliceLogger.info("Log in successful.");
	}
	
	@SuppressWarnings("rawtypes")
	private static void loadFeatures(String includePrefix) {
		Reflections include = new Reflections(includePrefix);
		Set<Class<? extends Feature>> excluded = new HashSet<Class<? extends Feature>>();
		Reflections exclude = null;
		if( Constants.FEATURE_BLACKLIST.length > 0 ) {
			exclude = new Reflections(Constants.FEATURE_BLACKLIST[0]);
		}
		for( int f=1; f<Constants.FEATURE_BLACKLIST.length; f++ ) {
			exclude.merge(new Reflections(Constants.FEATURE_BLACKLIST[f]));
		}
		if( exclude != null ) {
			excluded = exclude.getSubTypesOf(alice.framework.features.Feature.class);
		}
		
		for( Class<?> c : include.getSubTypesOf(alice.framework.features.Feature.class) ) {
			if( exclude != null && excluded.contains(c) ) {
				continue;
			}
			try {
				c.getConstructor().newInstance();
			} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				AliceLogger.error(String.format("An error occured while instantiating %s.", c.getName() ), 2);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void subscribeFeatures() {
		for( Class event : features.get().keySet() ) {
			Brain.client.on(event).flatMap(e -> {
				Mono<Void> process = Mono.fromRunnable(() -> {});
				
				for( HelperFeature h : helpers.get().get(event) ) {
					Mono<Void> response = h.handle((Class) e);
					if( response != null ) {
						process.and(response);
					}
				}
				
				for( ActiveFeature f : features.get().get(event) ) {
					Mono<Void> response = f.handle((Class) e);
					if( response != null ) {
						process.and(response);
						break;
					}
				}
				
				return process;
			}).subscribe();
		}
	}
	
	public static void updateAvatar(String url) {
		client.edit(spec -> {
			spec.setAvatar(Image.ofUrl(url).block());
			AliceLogger.info("Avatar updated.");
		}).subscribe();
	}
	
	@SuppressWarnings("rawtypes")
	public static Feature getFeatureByName(String name) {
		for( PriorityQueue<ActiveFeature> ff : features.get().values() ) {
			for( Feature f : ff ) {
				if( f.getName().equalsIgnoreCase(name) || f.getAliases().contains(name.toLowerCase()) ) {
					return f;
				}
			}
		}
		return null;
	}
}
