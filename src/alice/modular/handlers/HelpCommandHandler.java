package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class HelpCommandHandler extends CommandHandler {

	public HelpCommandHandler() {
		super("Help", "Default", false, PermissionProfile.getAnyonePreset());
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return true;
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		if( ts.size() == 1 ) {
			response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getHelpConstructor()));
		}
		return response;
	}

}
