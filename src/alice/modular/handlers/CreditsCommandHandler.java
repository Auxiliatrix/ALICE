package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.handlers.CommandHandler;
import alice.framework.structures.PermissionProfile;
import alice.framework.utilities.EmbedBuilders;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class CreditsCommandHandler extends CommandHandler {
	
	public CreditsCommandHandler() {
		super("Credits", "Default", false, PermissionProfile.getAnyonePreset());
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return true;
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		return new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getCreditsConstructor());
	}
	
}
