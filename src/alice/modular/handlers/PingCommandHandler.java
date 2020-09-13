package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.handlers.Handler;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PingCommandHandler extends Handler<MessageCreateEvent> {
	
	public PingCommandHandler() {
		super();
	}
	
	public boolean trigger(MessageCreateEvent event) {
		return event.getMessage().getContent().equals("%ping");
	}
	
	public Action execute(MessageCreateEvent event) {
		long received = event.getMessage().getTimestamp().toEpochMilli();
		long lag = System.currentTimeMillis() - received;
		return new Action()
				.addCreateMessageAction(event.getMessage().getChannel(), String.format("Pong! (%d) milis", lag));
	}
	
}
