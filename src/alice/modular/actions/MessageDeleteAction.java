package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class MessageDeleteAction extends Action {
	
	public MessageDeleteAction(Mono<Message> message) {
		super();
		this.mono = message.block().delete();
	}
	
}
