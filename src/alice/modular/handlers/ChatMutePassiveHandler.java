package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.handlers.Handler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.modular.actions.MessageDeleteAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class ChatMutePassiveHandler extends Handler<MessageCreateEvent> {

	public ChatMutePassiveHandler() {
		super("ChatMuteEnforcer", false, MessageCreateEvent.class);
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		if( event.getMessage().getAuthor().isEmpty() ) {
			return false;
		}
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getGuild().block().getId().asString());
		return guildData.has(String.format("chat_muted_%s", event.getMessage().getAuthor().get().getId().asString()));
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		return new MessageDeleteAction(event.getMessage());
	}
	
}
