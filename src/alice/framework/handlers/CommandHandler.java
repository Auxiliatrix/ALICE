package alice.framework.handlers;

import alice.configuration.calibration.Constants;
import alice.framework.structures.PermissionProfile;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public abstract class CommandHandler extends Handler<MessageCreateEvent> {

	protected PermissionProfile restrictions;
	
	protected CommandHandler(String name, String category, boolean enableWhitelist) {
		super(name, category, enableWhitelist);
		this.restrictions = null;
	}
	
	protected boolean invoked(MessageCreateEvent event) {
		return event.getMessage().getContent().toLowerCase().startsWith(String.format("%s%s", Constants.COMMAND_PREFIX, name));
	}
	
	@Override
	protected boolean filter(MessageCreateEvent event) {
		return invoked(event) && (restrictions == null || restrictions.verify(event.getMessage().getAuthorAsMember())) && super.filter(event);
	}

	@Override
	protected Mono<?> payload(MessageCreateEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

}
