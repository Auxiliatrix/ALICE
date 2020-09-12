package alice.modular.handlers;

import alice.framework.main.Brain;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class ProtoHandler {
	
	public ProtoHandler() {
		Brain.client.on(MessageCreateEvent.class)
			.filter(event -> trigger(event))
			.flatMap(event -> execute(event))
			.subscribe();
	}
	
	public boolean trigger(MessageCreateEvent event) {
		return event.getMessage().getContent().equals("%ping");
	}
	
	public Mono<?> execute(MessageCreateEvent event) {
		//return event.getMessage().getChannel().flatMap(channel -> channel.createMessage("Pong!")).then(); // Reactive
		return event.getMessage().getChannel().block().createMessage("Pong!"); // Imperative
	}
	
}
