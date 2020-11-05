package alice.modular.handlers;

import alice.framework.handlers.CommandHandler;
import alice.framework.handlers.Documentable;
import alice.framework.interactives.UselessInteractive;
import alice.framework.structures.PermissionProfile;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class CreateUselessInteractiveCommandHandler extends CommandHandler implements Documentable {

	public CreateUselessInteractiveCommandHandler() {
		super("Useless", true, PermissionProfile.getDeveloperPreset());
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		UselessInteractive ui = new UselessInteractive(event.getMessage().getChannelId(), event.getMessage().getChannel().block().createMessage(spec -> spec.setContent("0")).block().getId());
	}

	@Override
	public String getCategory() {
		return DEVELOPER.name();
	}

	@Override
	public String getDescription() {
		return "A useless interactive.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {};
	}
	
}
