package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

public class ReactionRemoveAction extends Action {

	public ReactionRemoveAction(Mono<Message> message, ReactionEmoji emoji, Snowflake userId) {
		super(message.block().removeReaction(emoji, userId));
	}
	
}
