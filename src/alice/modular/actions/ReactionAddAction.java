package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

public class ReactionAddAction extends Action {
	
	public ReactionAddAction(Mono<Message> message, ReactionEmoji emoji) {
		super(message.block().addReaction(emoji));
	}
	
}
