package alice.framework.handlers;

import alice.configuration.calibration.Constants;
import alice.framework.main.Brain;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel.Type;

public abstract class MentionHandler extends MessageHandler {
	
	protected MentionHandler(String name, boolean enableWhitelist, PermissionProfile restrictions) {
		super(name, enableWhitelist, restrictions);
	}
	
	/* Handler Specific Function */
	protected boolean mentioned(MessageCreateEvent event) {
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		return ts.containsAnyIgnoreCase(Constants.ALIASES) || event.getMessage().getChannel().block().getType() == Type.DM || event.getMessage().getUserMentionIds().contains(Brain.client.getSelfId());
	}
	
	/* Overriden Template */
	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return mentioned(event);
	}

}
