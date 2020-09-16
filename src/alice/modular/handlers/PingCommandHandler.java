package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.handlers.Documentable;
import alice.framework.structures.PermissionProfile;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PingCommandHandler extends CommandHandler implements Documentable {
	
	public PingCommandHandler() {
		super("Ping", false, PermissionProfile.getAnyonePreset());
	}
	
	public boolean trigger(MessageCreateEvent event) {
		return true;
	}
	
	public Action execute(MessageCreateEvent event) {
		return new NullAction()
				.addAction(new MessageCreateAction(event.getMessage().getChannel(), "Pong!"));
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
