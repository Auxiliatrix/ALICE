package alice.framework.handlers;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public abstract class CommandHandler extends Handler<MessageCreateEvent> {
	
	public CommandHandler() {
		super();
	}
	
	protected boolean invoked(MessageCreateEvent event) {
		return false;
	}
	
	@Override
	protected boolean filter(MessageCreateEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Mono<?> payload(MessageCreateEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

}
