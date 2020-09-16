package alice.framework.main;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.reflections.Reflections;

import alice.configuration.calibration.Constants;
import alice.framework.handlers.Handler;
import alice.framework.structures.AtomicSaveFolder;
import alice.framework.utilities.AliceLogger;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.rest.util.Image;

public class Brain {
	
	public static GatewayDiscordClient client = null;
	
	@SuppressWarnings("rawtypes")
	public static AtomicReference<List<Handler>> handlers = new AtomicReference<List<Handler>>(new ArrayList<Handler>()); // This is disgusting
	
	public static AtomicSaveFolder guildIndex = new AtomicSaveFolder();
	
	public static void main(String[] args) {
		if ( args.length < 1 ) {
			AliceLogger.error("Please pass the TOKEN as the first argument.");
			System.exit(0);
		}
		
		AliceLogger.info("Logging in...");
		login(args[0]);
		AliceLogger.info("Shutting down...");
	}
	
	private static void login(String token) {
		AliceLogger.info("Establishing connection...", 1);
		client = DiscordClientBuilder.create(token).build().login().block();
		
		//updateAvatar("https://i.imgur.com/grVaLEQ.png");
		
		AliceLogger.info("Initializing modules...", 1);
		loadModules(Constants.INCLUDED_MODULES, Constants.EXCLUDED_MODULES);
		for( String modules : Constants.ADDITIONAL_MODULES ) {
			loadModules(modules);
		}
		AliceLogger.info("Log in successful.");
		client.onDisconnect().block();
	}
	
	public static void loadModules(String includePrefix) {
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
					//e.printStackTrace();
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
