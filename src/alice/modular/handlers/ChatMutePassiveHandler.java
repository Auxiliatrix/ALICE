package alice.modular.handlers;

import alice.framework.handlers.MessageHandler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.modular.actions.MessageDeleteAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class ChatMutePassiveHandler extends MessageHandler {

	public ChatMutePassiveHandler() {
		super("ChatMutePassive", false, PermissionProfile.getAnyonePreset().andFromUser());
		aliases.add("cmp");
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getGuild().block().getId().asString());
		return guildData.has(String.format("chat_muted_%s", event.getMessage().getAuthor().get().getId().asString()));
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		(new MessageDeleteAction(event.getMessage())).toMono().block();
	}
	
}
