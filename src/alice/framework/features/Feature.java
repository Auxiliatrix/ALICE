package alice.framework.features;

import java.util.ArrayList;
import java.util.List;

import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;

public abstract class Feature<E extends Event> {
	
	protected static final String ENABLE_PREFIX = "module_enable_";
	protected static final String DISABLE_PREFIX = "module_disable_";
	
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
	
	public Mono<Void> handle(Class<E> type) {
		if( listen(type) ) {
			return respond(type);
		}
		return null;
	}
	
	protected abstract boolean listen(Class<E> type);
	protected abstract Mono<Void> respond(Class<E> type);
		
	public String getName() {
		return name;
	}
	
	public List<String> getAliases() {
		return new ArrayList<String>(aliases);
	}
	
	public boolean isEnabled( boolean whitelist, Mono<Guild> guild ) {
		AtomicSaveFile guildData = Brain.guildIndex.get(guild.block().getId().asString());
		return (!whitelist || guildData.has(String.format(ENABLE_PREFIX + "%", name))) && !guildData.has(String.format(DISABLE_PREFIX + "%", name));
	}
	
	public void addAlias(String alias) {
		this.aliases.add(alias);
	}
}
