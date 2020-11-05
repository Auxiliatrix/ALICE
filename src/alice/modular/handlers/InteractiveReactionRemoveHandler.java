package alice.modular.handlers;

import alice.framework.handlers.Handler;
import alice.framework.interactives.Interactive;
import alice.framework.main.Brain;
import discord4j.core.event.domain.message.ReactionRemoveEvent;

public class InteractiveReactionRemoveHandler extends Handler<ReactionRemoveEvent> {

	public InteractiveReactionRemoveHandler() {
		super("Interactive", ReactionRemoveEvent.class);
	}

	@Override
	protected boolean trigger(ReactionRemoveEvent event) {
		if( !Brain.channelIndex.containsKey(event.getChannelId().asString()) ) {
			return false;
		}
		return Brain.channelIndex.get(event.getChannelId().asString()).has(event.getMessageId().asString());
	}

	@Override
	protected void execute(ReactionRemoveEvent event) {
		Interactive interactive = Brain.interactives.get(String.format("%s_%s", event.getChannelId().asString(), event.getMessageId().asString()));
		interactive.onRemove(event.getEmoji(), event.getUserId()).block();
	}

}
