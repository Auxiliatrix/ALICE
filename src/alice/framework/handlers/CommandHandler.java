package alice.framework.handlers;

import java.util.ArrayList;
import java.util.List;

import alice.configuration.calibration.Constants;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EventUtilities;
import discord4j.core.event.domain.message.MessageCreateEvent;

public abstract class CommandHandler extends Handler<MessageCreateEvent> {

	protected PermissionProfile restrictions;
	protected String invocation;
	
	protected CommandHandler(String name, boolean enableWhitelist) {
		this(name, enableWhitelist, null);
	}
	
	protected CommandHandler(String name, boolean enableWhitelist, PermissionProfile restrictions) {
		super(name, enableWhitelist, MessageCreateEvent.class);
		this.restrictions = restrictions;
		this.invocation = String.format("%s%s", Constants.COMMAND_PREFIX, name);
	}
	
	/* Handler Specific Function */
	protected boolean invoked(MessageCreateEvent event) {
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		List<String> prefixedAliases = new ArrayList<String>();
		aliases.forEach( s -> prefixedAliases.add(String.format("%s%s", Constants.COMMAND_PREFIX, s)) );
		return ts.startsWithAnyIgnoreCase(prefixedAliases.toArray(new String[] {}));
	}
	
	/* Overriden Template */
	@Override
	protected boolean filter(MessageCreateEvent event) {
		return super.filter(event) && !EventUtilities.fromSelf(event) && invoked(event) && (restrictions == null || restrictions.verify(event.getMessage().getAuthor(), event.getGuild()));
	}

}
