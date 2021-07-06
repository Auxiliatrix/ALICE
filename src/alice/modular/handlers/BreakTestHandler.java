package alice.modular.handlers;

import alice.framework.handlers.CommandHandler;
import alice.framework.structures.PermissionProfile;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Deprecated
public class BreakTestHandler extends CommandHandler {

	public BreakTestHandler() {
		super("Break", false, PermissionProfile.getDeveloperPreset());
	}
	
	@Override
	protected void execute(MessageCreateEvent event) {
		try {
			Thread.sleep(11000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		new MessageCreateAction(event.getMessage().getChannel(), "Broken!").toMono().block();
	}
	
}
