package alice.framework.main;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.reflections.Reflections;

import alice.configuration.calibration.Constants;
import alice.framework.handlers.Handler;
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

public class Brain {
	
	public static GatewayDiscordClient client = null;
	
	@SuppressWarnings("rawtypes")
	public static AtomicReference<List<Handler>> handlers = new AtomicReference<List<Handler>>(new ArrayList<Handler>()); // This is disgusting
	
	public static AtomicSaveFolder guildIndex = new AtomicSaveFolder();
		
	private static Runnable upkeep = () -> {
		int cycle = 0;
		MessageChannel channel = (MessageChannel) client.getChannelById(Snowflake.of(757836189687349308L)).block();
		while( true ) {
			AliceLogger.info(String.format("Starting cycle %d", cycle));
			try {
				channel.createMessage(String.format("Starting cycle %d", cycle)).block();
			} catch( Exception e ) {
				AliceLogger.error("Socket reset this cycle. Trying again next cycle.");
			}
			try {
				Thread.sleep(300000);
			} catch (InterruptedException e) {
				break;
			}
			cycle++;
		}
	};
	
	public static void main(String[] args) {
		if ( args.length < 1 ) {
			AliceLogger.error("Please pass the TOKEN as the first argument.");
			System.exit(0);
		}
		AliceLogger.info("Reloading save data...");
		reload();
		
		AliceLogger.info("Logging in...");
		login(args[0]);
		
		upkeep.run();
		
		client.onDisconnect().block();
		AliceLogger.info("Shutting down...");
	}
	
	public static Handler<?> getModuleByName(String name) {
		for( Handler<?> h : handlers.get() ) {
			if( h.getName().equalsIgnoreCase(name) || h.getAliases().contains(name.toLowerCase()) ) {
				return h;
			}
		}
		return null;
	}
	
	private static void reload() {
		File folder = new File("tmp/guilds");
		if( folder.isDirectory() ) {
			for( File file : folder.listFiles() ) {
				String guildId = file.getName();
				int extension = guildId.indexOf(".");
				if( extension > 0 ) {
					guildId = guildId.substring(0, extension);
					String guildFile = String.format("%s%s%s%s%s.json", "tmp", File.separator, "guilds", File.separator, guildId);
					Brain.guildIndex.put(guildId, new AtomicSaveFile(guildFile));
					AliceLogger.info(String.format("Loaded guild data for %s.", guildId), 1);
				}
			}
		}
	}
	
	private static void login(String token) {
		AliceLogger.info("Establishing connection...", 1);
		client = DiscordClientBuilder.create(token).build().login().block();
		client.updatePresence(Presence.online(Activity.listening("%help"))).block();
		client.on(ReadyEvent.class)
			.subscribe( event -> {
				AliceLogger.info("Initializing modules...", 1);
				loadModules(Constants.INCLUDED_MODULES, Constants.EXCLUDED_MODULES);
				for( String modules : Constants.ADDITIONAL_MODULES ) {
					loadModules(modules);
				}
			});
		//updateAvatar("https://i.imgur.com/grVaLEQ.png");
		
		AliceLogger.info("Log in successful.");
	}
	
	private static void loadModules(String includePrefix) {
		loadModules(includePrefix, "");
	}
	
	@SuppressWarnings("rawtypes")
	private static void loadModules(String includePrefix, String excludePrefix) {
		Reflections include = new Reflections(includePrefix);
		Reflections exclude = excludePrefix.isEmpty() ? null : new Reflections(excludePrefix);
		for( Class<?> c : include.getSubTypesOf(alice.framework.handlers.Handler.class) ) {
			if( exclude != null && exclude.getSubTypesOf(alice.framework.handlers.Handler.class).contains(c) ) {
				continue;
			}
			handlers.updateAndGet( h -> { 
				try {
					h.add( (Handler) c.getConstructor().newInstance() );
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
					AliceLogger.error(String.format("An error occured while instantiating %s.", c.getName() ), 2);
				}
				return h;
			} );
		}
	}
	
	@SuppressWarnings("unused")
	private static void updateAvatar(String url) {
		client.edit(spec -> {
			spec.setAvatar(Image.ofUrl(url).block());
			AliceLogger.info("Avatar updated.");
		}).subscribe();
	}
}
