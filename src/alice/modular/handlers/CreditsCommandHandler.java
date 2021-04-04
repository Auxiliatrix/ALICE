package alice.modular.handlers;

import alice.framework.features.Documentable;
import alice.framework.handlers.CommandHandler;
import alice.framework.structures.PermissionProfile;
import alice.framework.utilities.EmbedBuilders;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class CreditsCommandHandler extends CommandHandler implements Documentable {
	
	public CreditsCommandHandler() {
		super("Credits", false, PermissionProfile.getAnyonePreset());
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getCreditsConstructor()).toMono().block();
	}

	@Override
	public String getDescription() {
		return "Displays the credits for the people who helped contribute to this bot's production.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair(invocation, "Display a credits embed")
		};
	}

	@Override
	public String getCategory() {
		return Documentable.DEFAULT.name();
	}

}
