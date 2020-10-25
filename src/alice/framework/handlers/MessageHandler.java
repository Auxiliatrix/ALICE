package alice.framework.handlers;

import alice.framework.structures.PermissionProfile;
import alice.framework.utilities.EventUtilities;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;

public abstract class MessageHandler extends Handler<MessageCreateEvent> {

	protected PermissionProfile restrictions;
	public boolean whitelist;
	
	public MessageHandler(String name, PermissionProfile restrictions) {
		this(name, false, restrictions);
	}
	
	public MessageHandler(String name, boolean whitelist, PermissionProfile restrictions) {
		super(name, MessageCreateEvent.class);
		this.restrictions = restrictions;
		this.whitelist = whitelist;
	}
	
	@Override
	protected boolean filter(MessageCreateEvent event) {
		return isEnabled(event.getGuild())
				&& !EventUtilities.fromSelf(event)
				&& (restrictions == null || restrictions.verify(event.getMessage().getAuthor(), event.getGuild()))
				&& super.filter(event);
	}
	
	public boolean isEnabled(Mono<Guild> guild) {
		return isEnabled(whitelist, guild);
	}
	
}
