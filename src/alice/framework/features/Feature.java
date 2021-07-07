package alice.framework.features;

import java.util.ArrayList;
import java.util.List;

import alice.framework.features.ActiveFeature.ExclusionClass;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * An objective container for the logic of a subscribed event.
 * Contains variables to indicate conditions, storage, and reference. 
 * Also has abstracted functions which represent whether or not to respond to an event, and how to respond if it should.
 * @author Auxiliatrix
 *
 * @param <E> Event class to respond to
 */
public abstract class Feature<E extends Event> implements Comparable<Feature<E>> {
	
	// TODO: rename documentation variables "type" and "event instance/class" for consistency
	
	/**
	 * Each Exclusion Class that is activated will prevent the activation of any Features in the Exclusion Classes below it.
	 * @author Auxiliatrix
	 *
	 */
	public static enum ExclusionClass {
		DOMINANT,	// Prevents non-dominant Features from activating. Best used with Features that alter save data, or important soft-matched Features to prevent overlap with other trigger conditions.
		STANDARD,	// In most cases, this Feature will be activated as intended. Best used with hard-matched cases that are mostly self-contained.
		SUBMISSIVE,	// Will only activate if no non-submissive Features are activated. Best used with soft-matching Features to prevent collisions with hard-matched ones.
	};
	
	/**
	 * What Exclusion Class this Feature belongs to. If set to null, this Feature will not stop any other Features from activating, nor will it be stopped by any other Features.
	 */
	protected ExclusionClass exclusionClass;
	
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
		withExclusionClass(null);
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
	 * Set the Exclusion Class of this Feature. Can be null.
	 * @param exclusionClass
	 * @return the modified Feature
	 */
	protected Feature<E> withExclusionClass(@Nullable ExclusionClass exclusionClass) {
		this.exclusionClass = exclusionClass;
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
	 * Get the ExclusionClass for this Feature.
	 * @return ExclusionClass of this Feature
	 */
	public ExclusionClass getExclusionClass() {
		return exclusionClass;
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
	
	@Override
	public int compareTo(Feature<E> f) {
		if( this.exclusionClass == null && f.getExclusionClass() == null ) {
			return 0;
		} else if( this.exclusionClass == null ) {
			return -1;
		} else if( f.getExclusionClass() == null ) {
			return 1;
		} else {
			return this.exclusionClass.ordinal() - f.getExclusionClass().ordinal();
		}
	}
}
