package alice.framework.features;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public abstract class Feature<E extends Event> {
	
	protected String name;
	protected List<String> aliases;
	
	protected Feature(String name, Class<E> type) {
		this.name = name;
		this.aliases = new ArrayList<String>();
		
		load(type);
	}
	
	protected Feature<E> withAliases(String...aliases) {
		for( String alias : aliases ) {
			addAlias(alias);
		}
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
