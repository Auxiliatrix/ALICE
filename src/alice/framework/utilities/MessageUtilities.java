package alice.framework.utilities;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class MessageUtilities {
	
	public static String getGuildId(MessageCreateEvent event) {
		return event.getGuild().block().getId().asString();
	}
	
}
