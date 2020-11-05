package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class ReactionClearAction extends Action {
	
	public ReactionClearAction(Mono<Message> message) {
		super(message.block().removeAllReactions());
	}
	
}
