package alice.framework.modules.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public class Task<E extends Event> implements Function<E, Mono<?>> {
	
	protected DependencyFactory<E> dependencies;
	protected List<Effect<E>> effects;
	
	public Task(DependencyFactory<E> dependencies) {
		this.dependencies = dependencies;
		this.effects = new ArrayList<Effect<E>>();
	}
	
	public Task<E> addEffect(Function<Dependency<E>, Mono<?>> effect) {
		
		return this;
	}
	
	@Override
	public Mono<?> apply(E t) {
		Mono<Dependency<E>> dependency = dependencies.getDependency(t);
		return null;
	}
	
}
