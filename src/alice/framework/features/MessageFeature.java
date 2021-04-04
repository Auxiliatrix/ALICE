package alice.framework.features;

import java.util.ArrayList;
import java.util.List;

import alice.framework.main.Brain;
import alice.framework.main.Constants;
import alice.framework.structures.TokenizedString;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel.Type;

public abstract class MessageFeature extends ActiveFeature<MessageCreateEvent> {

	protected String invocation;
	protected boolean checkInvoked;
	protected boolean checkMentioned;
	
	protected MessageFeature(String name, Class<MessageCreateEvent> type) {
		super(name, type);
		invocation = String.format("%s%s", Constants.COMMAND_PREFIX, name);
		checkInvoked = false;
		checkMentioned = false;
	}
	
	protected MessageFeature withCheckInvoked() {
		this.checkInvoked = true;
		return this;
	}
	
	protected MessageFeature withCheckMentioned() {
		this.checkMentioned = true;
		return this;
	}
	
	protected abstract boolean condition(MessageCreateEvent event);
	
	@Override
	protected boolean listen(MessageCreateEvent event) {
		return isAllowed(event.getMember().get()) 
				&& isEnabled(event.getGuildId().get().asString())
				&& (!checkInvoked || invoked(event.getMessage()))
				&& (!checkMentioned || mentioned(event.getMessage()));
	}
	
	protected boolean invoked(Message message) {
		TokenizedString ts = new TokenizedString(message.getContent());
		List<String> prefixedAliases = new ArrayList<String>();
		aliases.forEach( s -> prefixedAliases.add(String.format("%s%s", Constants.COMMAND_PREFIX, s)) );
		return ts.startsWithAnyIgnoreCase(prefixedAliases.toArray(new String[] {}));
	}
	
	protected boolean mentioned(Message message) {
		TokenizedString ts = new TokenizedString(message.getContent());
		return ts.containsAnyIgnoreCase(Constants.ALIASES) || message.getChannel().block().getType() == Type.DM || message.getUserMentionIds().contains(Brain.client.getSelfId());
	}

}
