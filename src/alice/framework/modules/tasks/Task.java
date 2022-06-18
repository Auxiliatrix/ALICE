package alice.framework.modules.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public class Task<E extends Event> implements Function<E, Mono<?>> {
	
	protected DependencyFactory<E> dependencies;
	protected List<Function<Dependency<E>, Mono<?>>> effects;
	
	public Task(DependencyFactory<E> dependencies) {
		this.dependencies = dependencies;
		this.effects = new ArrayList<Function<Dependency<E>, Mono<?>>>();
	}
	
	public Task<E> addEffect(Function<Dependency<E>, Mono<?>> effect) {
		effects.add(effect);
		return this;
	}
	
	@Override
	public Mono<?> apply(E t) {
		Mono<Dependency<E>> dependency = dependencies.getDependency(t);
		dependency.flatMapIterable(d -> {
			List<Mono<?>> results = new ArrayList<Mono<?>>();
			for( Function<Dependency<E>, Mono<?>> effect : effects ) {
				results.add(effect.apply(d));
			}
			return results;
		});
		return null;
	}
	
}
