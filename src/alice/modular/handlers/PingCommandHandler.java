package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.structures.PermissionProfile;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PingCommandHandler extends CommandHandler {
	
	public PingCommandHandler() {
		super("Ping", "Default", false, PermissionProfile.getAnyonePreset());
	}
	
	public boolean trigger(MessageCreateEvent event) {
		return true;
	}
	
	public Action execute(MessageCreateEvent event) {
		return new NullAction()
				.addAction(new MessageCreateAction(event.getMessage().getChannel(), "Pong!"));
	}
	
}
