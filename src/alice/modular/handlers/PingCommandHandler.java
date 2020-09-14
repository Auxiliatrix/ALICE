package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.handlers.CommandHandler;
import alice.framework.structures.PermissionProfile;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PingCommandHandler extends CommandHandler {
	
	public PingCommandHandler() {
		super("Ping", "Default", false);
		this.restrictions = PermissionProfile.getAnyonePreset();
	}
	
	public boolean trigger(MessageCreateEvent event) {
		return true;
	}
	
	public Action execute(MessageCreateEvent event) {
		return new Action()
				.addCreateMessageAction(event.getMessage().getChannel(), "Pong!");
	}
	
}
