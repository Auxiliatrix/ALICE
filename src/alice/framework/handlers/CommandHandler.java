package alice.framework.handlers;

import java.util.ArrayList;
import java.util.List;

import alice.configuration.calibration.Constants;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.MessageUtilities;
import discord4j.core.event.domain.message.MessageCreateEvent;

public abstract class CommandHandler extends Handler<MessageCreateEvent> {

	protected PermissionProfile restrictions;
	protected List<String> aliases;
	
	protected CommandHandler(String name, String category, boolean enableWhitelist) {
		this(name, category, enableWhitelist, null);
	}
	
	protected CommandHandler(String name, String category, boolean enableWhitelist, PermissionProfile restrictions) {
		super(name, category, enableWhitelist, MessageCreateEvent.class);
		this.restrictions = restrictions;
		this.aliases = new ArrayList<String>();
		this.aliases.add(name);
	}
	
	/* Handler Specific Function */
	protected boolean invoked(MessageCreateEvent event) {
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		List<String> prefixedAliases = new ArrayList<String>(aliases);
		aliases.forEach( s -> prefixedAliases.add(String.format("%s%s", Constants.COMMAND_PREFIX, s)) );
		return ts.startsWithAnyIgnoreCase(prefixedAliases.toArray(new String[] {}));
	}
	
	/* Overriden Template */
	@Override
	protected boolean filter(MessageCreateEvent event) {
		return super.filter(event) && !MessageUtilities.fromSelf(event) && invoked(event) && (restrictions == null || restrictions.verify(event.getMessage().getAuthor(), event.getGuild()));
	}

}
