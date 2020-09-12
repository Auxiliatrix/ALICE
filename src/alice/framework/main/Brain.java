package alice.framework.main;

import alice.framework.utilities.AliceLogger;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;

public class Brain {
	
	public static GatewayDiscordClient client = null;
	
	public static void main(String[] args) {
		if ( args.length < 1 ) {
			AliceLogger.error("Please pass the TOKEN as the first argument.");
			System.exit(0);
		}
		
		login(args[0]);
	}
	
	private static void login(String token) {
		AliceLogger.info("Logging in...");
		client = DiscordClientBuilder.create(token).build().login().block();
		
		// put reflections here
		
		client.onDisconnect().block();
	}
}
