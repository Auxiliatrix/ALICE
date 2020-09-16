package alice.framework.handlers;

import alice.configuration.calibration.Constants;
import alice.framework.main.Brain;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.MessageUtilities;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel.Type;

public abstract class MentionHandler extends Handler<MessageCreateEvent> {

	protected PermissionProfile restrictions;
	
	protected MentionHandler(String name, String category, boolean enableWhitelist) {
		super(name, category, enableWhitelist, MessageCreateEvent.class);
		this.restrictions = null;
	}
	
	/* Handler Specific Function */
	protected boolean mentioned(MessageCreateEvent event) {
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		return ts.containsAnyIgnoreCase(Constants.ALIASES) || event.getMessage().getChannel().block().getType() == Type.DM || event.getMessage().getUserMentionIds().contains(Brain.client.getSelfId());
	}
	
	/* Overriden Template */
	@Override
	protected boolean filter(MessageCreateEvent event) {
		return super.filter(event) && !MessageUtilities.fromSelf(event) && mentioned(event) && (restrictions == null || restrictions.verify(event.getMessage().getAuthor(), event.getGuild()));
	}

}
