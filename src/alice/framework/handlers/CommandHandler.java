package alice.framework.handlers;

import alice.configuration.calibration.Constants;
import alice.framework.structures.PermissionProfile;
import alice.framework.utilities.MessageUtilities;
import discord4j.core.event.domain.message.MessageCreateEvent;

public abstract class CommandHandler extends Handler<MessageCreateEvent> {

	protected PermissionProfile restrictions;
	
	protected CommandHandler(String name, String category, boolean enableWhitelist) {
		super(name, category, enableWhitelist, MessageCreateEvent.class);
		this.restrictions = null;
	}
	
	/* Handler Specific Function */
	protected boolean invoked(MessageCreateEvent event) {
		return event.getMessage().getContent().toLowerCase().startsWith(String.format("%s%s", Constants.COMMAND_PREFIX, name).toLowerCase());
	}
	
	/* Overriden Template */
	@Override
	protected boolean filter(MessageCreateEvent event) {
		return super.filter(event) && !MessageUtilities.fromSelf(event) && invoked(event) && (restrictions == null || restrictions.verify(event.getMessage().getAuthor(), event.getGuild()));
	}

}
