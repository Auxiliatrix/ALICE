package alice.framework.features;

import java.util.ArrayList;
import java.util.List;

import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

/**
 * An objective container for the logic of a subscribed event.
 * Contains variables to indicate conditions, storage, and reference. 
 * Also has abstracted functions which represent whether or not to respond to an event, and how to respond if it should.
 * @author Auxiliatrix
 *
 * @param <E> Event class to respond to
 */
public abstract class Feature<E extends Event> {
	
	// TODO: rename documentation variables "type" and "event instance/class" for consistency
	
	// Constants to check for in guild data to check if module is enabled or disabled
	protected static final String ENABLE_PREFIX = "module_enable_";
	protected static final String DISABLE_PREFIX = "module_disable_";
		// TODO: move to constants file
	
	/**
	 * Identifier for Feature.
	 */
	protected String name;
		// TODO: features with the same name are activated and deactivated together ? or exclusive features can only share one name
	
	/**
	 * Alternative identifiers for Features
	 */
	protected List<String> aliases;
		// TODO: also check for exclusivity
	
	/**
	 * Whether a Feature is enabled by default
	 */
	protected boolean whitelist;
	
	/**
	 * Construct a Feature with a given name and Event type that triggers it
	 * @param name String used to refer to this Feature
	 * @param type Event class that will trigger this Feature
	 */
	protected Feature(String name, Class<E> type) {
			// TODO: null checking
		this.name = name;
		this.aliases = new ArrayList<String>();
		this.whitelist = false;
		addAlias(name);
		
		load(type);
	}
	
	/**
	 * Add aliases to Feature
	 * @param aliases Strings to add to list of this Feature's aliases
	 * @return the modified Feature
	 */
	protected Feature<E> withAliases(String...aliases) {
			// TODO: null checking
		for( String alias : aliases ) {
			addAlias(alias);
		}
		return this;
	}
	
	/**
	 * Make this Feature enabled by default
	 * @return the modified Feature
	 */
	protected Feature<E> withWhitelist() {
		whitelist = true;
		return this;
	}
	
	/**
	 * Function to run once this class has been constructed. Used to subscribe the Feature and add it to global containers.
	 * @param type Event to associate this Feature with
	 */
	protected abstract void load(Class<E> type);
	
	/**
	 * Simple logic structure for what this Feature should do when it is triggered by an Event.
	 * @param type Event instance that triggerd this Feature
	 * @return a Mono response to be executed, or null if no response was elicited
	 */
	public Mono<?> handle(E type) {
		if( listen(type) ) {
			return respond(type);
		}
		return null;
	}
	
	/**
	 * Function to verify whether this Feature should respond to a given Event instance.
	 * @param type Event instance that was used to trigger this Feature
	 * @return whether or not to respond
	 */
	protected abstract boolean listen(E type);
	
	/**
	 * Function to generate a Mono response when triggered by a given Event instance
	 * @param type Event instance that was used to trigger this Feature
	 * @return a mono response to be executed, or null if no response was elicited
	 */
	protected abstract Mono<?> respond(E type);
	
	/**
	 * Get the name for this Feature.
	 * @return String name of this Feature
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the aliases for this Feature.
	 * @return List of String aliases for this Feature
	 */
	public List<String> getAliases() {
		return new ArrayList<String>(aliases);
	}
	
	/**
	 * Add an alias for this Feature
	 * @param alias String to add to list of this Feature's aliases
	 */
	public void addAlias(String alias) {
			// TODO: null checking
		this.aliases.add(alias);
	}
	
	/**
	 * Verify whether or not this Feature is enabled in a given Guild
	 * @param guildId String to identify the Guild in which to check
	 * @return whether or not this Feature is enabled
	 */
	public boolean isEnabled( String guildId ) {
		AtomicSaveFile guildData = Brain.guildIndex.get(guildId);
			// TODO: exception handling
		return (!whitelist || guildData.has(String.format("%s%s", ENABLE_PREFIX, name))) && !guildData.has(String.format("%s%s", DISABLE_PREFIX, name));
	}
}
