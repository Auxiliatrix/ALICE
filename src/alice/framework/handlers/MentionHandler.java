package alice.framework.handlers;

import alice.configuration.calibration.Constants;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import discord4j.core.event.domain.message.MessageCreateEvent;

public abstract class MentionHandler extends Handler<MessageCreateEvent> {

	protected PermissionProfile restrictions;
	
	protected MentionHandler(String name, String category, boolean enableWhitelist) {
		super(name, category, enableWhitelist, MessageCreateEvent.class);
		this.restrictions = null;
	}
	
	/* Handler Specific Function */
	protected boolean mentioned(MessageCreateEvent event) {
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		return ts.containsAny(Constants.ALIASES);
	}
	
	/* Overriden Template */
	@Override
	protected boolean filter(MessageCreateEvent event) {
		return super.filter(event) && mentioned(event) && (restrictions == null || restrictions.verify(event.getMessage().getAuthorAsMember()));
	}

}
