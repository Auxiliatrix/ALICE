package alice.framework.handlers;

import alice.framework.structures.PermissionProfile;
import alice.framework.utilities.EventUtilities;
import discord4j.core.event.domain.message.MessageCreateEvent;

public abstract class MessageHandler extends Handler<MessageCreateEvent> {

	protected PermissionProfile restrictions;
	
	public MessageHandler(String name, boolean whitelist, PermissionProfile restrictions) {
		super(name, whitelist, MessageCreateEvent.class);
		this.restrictions = restrictions;
	}
	
	protected boolean filter(MessageCreateEvent event) {
		return super.filter(event) && !EventUtilities.fromSelf(event) && (restrictions == null || restrictions.verify(event.getMessage().getAuthor(), event.getGuild()));
	}
	
}
