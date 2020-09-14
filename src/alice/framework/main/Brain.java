package alice.framework.main;

import java.util.ArrayList;
import java.util.List;

import alice.framework.handlers.EverythingHandler;
import alice.framework.handlers.Handler;
import alice.framework.utilities.AliceLogger;
import alice.modular.handlers.EavesdropPassiveHandler;
import alice.modular.handlers.PingCommandHandler;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.rest.util.Image;

public class Brain {
	
	public static GatewayDiscordClient client = null;
	
	@SuppressWarnings("rawtypes")
	public static List<Handler> handlers = new ArrayList<Handler>();
	
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
		handlers.add(new PingCommandHandler());
		handlers.add(new EavesdropPassiveHandler());
		handlers.add(new EverythingHandler());
		
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
