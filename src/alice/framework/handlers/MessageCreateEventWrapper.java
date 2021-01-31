package alice.framework.handlers;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class MessageCreateEventWrapper {

	private MessageCreateEvent event;
	
	public MessageCreateEventWrapper(MessageCreateEvent event) {
		this.event = event;
	}
	
	public void execute() {
		System.out.println("Message Received!");
	}
	
}
