package alice.modular.handlers;

import alice.framework.handlers.Handler;
import alice.framework.interactives.Interactive;
import alice.framework.main.Brain;
import discord4j.core.event.domain.message.ReactionAddEvent;

public class InteractiveReactionAddHandler extends Handler<ReactionAddEvent> {

	public InteractiveReactionAddHandler() {
		super("Interactive", ReactionAddEvent.class);
	}

	@Override
	protected boolean trigger(ReactionAddEvent event) {
		if( !Brain.channelIndex.containsKey(event.getChannelId().asString()) ) {
			return false;
		}
		return Brain.channelIndex.get(event.getChannelId().asString()).has(event.getMessageId().asString());
	}

	@Override
	protected void execute(ReactionAddEvent event) {
		Interactive interactive = Brain.interactives.get(String.format("%s_%s", event.getChannelId().asString(), event.getMessageId().asString()));
		interactive.onAdd(event.getEmoji(), event.getUserId()).block();
	}

}
