package alice.framework.features;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public abstract class Feature<E extends Event> {

	// TODO: Bring this up to a super, and change feature in brain accordingly, dont forget to implement comparable and make it a priority queue
	public static enum PriorityClass {
		DOMINANT,	// Prevents any other features from activating
		STANDARD,	// Only the first Standard feature will be activated
		SUBMISSIVE,	// Will only activate if no other features are activated
	};
	
	protected String name;
	protected List<String> aliases;
	protected PriorityClass priority;
	
	protected Feature(String name, Class<E> type) {
		this.name = name;
		this.aliases = new ArrayList<String>();
		this.priority = PriorityClass.STANDARD;
		
		load(type);
	}
	
	protected Feature<E> withAliases(String...aliases) {
		for( String alias : aliases ) {
			addAlias(alias);
		}
		return this;
	}
	
	protected Feature<E> withPriority(PriorityClass priority) {
		this.priority = priority;
		return this;
	}
	
	protected abstract void load(Class<E> type);
	
	public abstract boolean filter(Class<E> type); // TODO: overhead / rename
	public abstract Mono<Void> execute(Class<E> type); // TODO: overhead / rename
	
	// TODO: trigger / execute
	
	public String getName() {
		return name;
	}
	
	public List<String> getAliases() {
		return new ArrayList<String>(aliases);
	}
	
	public void addAlias(String alias) {
		this.aliases.add(alias);
	}
	
}
