package alice.framework.utilities;

import alice.framework.main.Brain;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class MessageUtilities {
	
	public static synchronized String getGuildId(MessageCreateEvent event) {
		return event.getGuild().block().getId().asString();
	}
	
	public static synchronized boolean fromSelf(MessageCreateEvent event) {
		return event.getMessage().getAuthor().isEmpty() ? false : event.getMessage().getAuthor().get().equals(Brain.client.getSelf().block());
	}
	
}
