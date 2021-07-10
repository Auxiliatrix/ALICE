package alice.framework.features;

import java.util.ArrayList;
import java.util.List;

import alice.framework.main.Brain;
import alice.framework.main.Constants;
import alice.framework.structures.TokenizedString;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel.Type;

/**
 * An ActiveFeature with utility functions to help process MessageCreateEvents.
 * @author Auxiliatrix
 *
 */
public abstract class MessageFeature extends Feature<MessageCreateEvent> {

	/**
	 * The prefix associated with this Feature's invocation.
	 */
	protected String invocation;
	
	/**
	 * Whether this Feature should check if a message begins with its invocation prefix.
	 */
	protected boolean checkInvoked;
	
	/**
	 * Whether this Feature should check if this bot's name was mentioned in the message.
	 */
	protected boolean checkMentioned;
	
	/**
	 * Construct a Feature with the given name.
	 * @param name String used to refer to this Feature
	 */
	protected MessageFeature(String name) {
		super(name, MessageCreateEvent.class);
		invocation = String.format("%s%s", Constants.COMMAND_PREFIX, name);
		checkInvoked = false;
		checkMentioned = false;
	}
	
	/**
	 * If called, this Feature will check if it has been invoked.
	 * @return the modified Feature
	 */
	protected MessageFeature withCheckInvoked() {
		this.checkInvoked = true;
		return this;
	}
	
	/**
	 * If called, this Feature will check if it has been mentioned.
	 * @return the modified Feature
	 */
	protected MessageFeature withCheckMentioned() {
		this.checkMentioned = true;
		return this;
	}
	
	/**
	 * An abstraction of the conditions to be met in order for this Feature to be activated.
	 * @param event Event instance this Feature was called on
	 * @return whether or not this Feature should create a response to the given Event
	 */
	protected abstract boolean condition(MessageCreateEvent event);
	
	@Override
	protected boolean listen(MessageCreateEvent event) {
		return isAllowed(event.getMember().get()) 
				//&& isEnabled(event.getGuildId().get().asString())
				&& (!checkInvoked || invoked(event.getMessage()))
				&& (!checkMentioned || mentioned(event.getMessage()));
	}
	
	/**
	 * Checks whether a message begins with this Feature's invocation prefix.
	 * @param message Message instance to check
	 * @return whether or not the given Message begins with this Feature's invocation prefix
	 */
	protected boolean invoked(Message message) {
		TokenizedString ts = new TokenizedString(message.getContent());
		List<String> prefixedAliases = new ArrayList<String>();
		aliases.forEach( s -> prefixedAliases.add(String.format("%s%s", Constants.COMMAND_PREFIX, s)) );
		return ts.startsWithAnyIgnoreCase(prefixedAliases.toArray(new String[] {}));
	}
	
	/**
	 * Checks whether a message mentions this bot's name.
	 * @param message Message instance to check
	 * @return whether or not the given Message contains this bot's name
	 */
	protected boolean mentioned(Message message) {
		TokenizedString ts = new TokenizedString(message.getContent());
		return ts.containsAnyIgnoreCase(Constants.ALIASES) || message.getChannel().block().getType() == Type.DM || message.getUserMentionIds().contains(Brain.client.getSelfId());
	}

}
