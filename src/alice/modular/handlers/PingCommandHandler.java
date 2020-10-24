package alice.modular.handlers;

import alice.framework.handlers.CommandHandler;
import alice.framework.handlers.Documentable;
import alice.framework.structures.PermissionProfile;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PingCommandHandler extends CommandHandler implements Documentable {
	
	public PingCommandHandler() {
		super("Ping", false, PermissionProfile.getAnyonePreset());
	}
	
	public void execute(MessageCreateEvent event) {
		new MessageCreateAction(event.getMessage().getChannel(), "Pong!").toMono().block();
	}

	@Override
	public String getCategory() {
		return Documentable.DEFAULT.name();
	}

	@Override
	public String getDescription() {
		return "A generic command to test if this bot is currently online.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
				new DocumentationPair(invocation, "Pong!")
		};
	}
	
}
