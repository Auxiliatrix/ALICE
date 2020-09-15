package alice.framework.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import alice.framework.handlers.EverythingHandler;
import alice.framework.handlers.Handler;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.utilities.AliceLogger;
import alice.modular.handlers.EavesdropPassiveHandler;
import alice.modular.handlers.GuildLoadHandler;
import alice.modular.handlers.PingCommandHandler;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.rest.util.Image;

public class Brain {
	
	public static GatewayDiscordClient client = null;
	
	@SuppressWarnings("rawtypes")
	public static AtomicReference<List<Handler>> handlers = new AtomicReference<List<Handler>>(new ArrayList<Handler>()); // This is disgusting
	
	public static AtomicReference<Map<String, AtomicSaveFile>> guildIndex = new AtomicReference<Map<String, AtomicSaveFile>>(new HashMap<String, AtomicSaveFile>());
	
	public static void main(String[] args) {
		if ( args.length < 1 ) {
			AliceLogger.error("Please pass the TOKEN as the first argument.");
			System.exit(0);
		}
		
		AliceLogger.info("Logging in...");
		login(args[0]);
		AliceLogger.info("Log in successful.");
	}
	
	private static void login(String token) {
		AliceLogger.info("Establishing connection...", 1);
		client = DiscordClientBuilder.create(token).build().login().block();
		
		//updateAvatar("https://i.imgur.com/SBaq6Br.png");
		AliceLogger.info("Initializing modules...", 1);
		handlers.getAndUpdate( c -> { c.add(new PingCommandHandler()); return c; } );
		handlers.getAndUpdate( c -> { c.add(new EavesdropPassiveHandler()); return c; } );
		handlers.getAndUpdate( c -> { c.add(new EverythingHandler()); return c; });
		handlers.getAndUpdate( c -> { c.add(new GuildLoadHandler()); return c; } );
		
		client.onDisconnect().block();
	}
	
	@SuppressWarnings("unused")
	private static void updateAvatar(String url) {
		client.edit(spec -> {
			spec.setAvatar(Image.ofUrl(url).block());
			AliceLogger.info("Avatar updated.");
		}).subscribe();
	}
}
