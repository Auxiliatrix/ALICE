package alice.framework.handlers;

import alice.framework.main.Brain;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class ProtoHandler {
	
	public ProtoHandler() {
		Brain.client.on(MessageCreateEvent.class)
			.filter(message -> trigger(message))
			.subscribe(event -> execute(event));
	}
	
	public boolean trigger(MessageCreateEvent event) {
		return false;
	}
	
	public void execute(MessageCreateEvent event) {
		
	}
	
}
