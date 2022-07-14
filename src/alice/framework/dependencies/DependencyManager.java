package alice.framework.dependencies;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import alice.framework.dependencies.MultiDependencyManagers.DependencyManager2;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public class DependencyManager<E extends Event, T> {

	protected Function<E,Mono<?>> retriever;
	
	protected DependencyManager(Function<E,Mono<?>> retriever) {
		this.retriever = retriever;
	}
	
	public Function<DependencyMap<E>, Mono<?>> buildEffect(Function<T,Mono<?>> spec) {
		return d -> spec.apply(d.<T>request(retriever));
	}
	
	public Consumer<DependencyMap<E>> buildSideEffect(Consumer<T> spec) {
		return d -> spec.accept(d.<T>request(retriever));
	}
	
	public Function<DependencyMap<E>, Boolean> buildCondition(Function<T,Boolean> spec) {
		return d -> spec.apply(d.<T>request(retriever));
	}
	
	public Function<DependencyMap<E>, Mono<?>> buildEffect(BiFunction<E,T,Mono<?>> spec) {
		return d -> spec.apply(d.getEvent(), d.<T>request(retriever));
	}
	
	public Consumer<DependencyMap<E>> buildSideEffect(BiConsumer<E,T> spec) {
		return d -> spec.accept(d.getEvent(), d.<T>request(retriever));
	}
	
	public Function<DependencyMap<E>, Boolean> buildCondition(BiFunction<E,T,Boolean> spec) {
		return d -> spec.apply(d.getEvent(), d.<T>request(retriever));
	}
	
	public T requestFrom(DependencyMap<E> dependency) {
		return dependency.<T>request(retriever);
	}
	
	public <U> DependencyManager2<E,T,U> with(DependencyManager<E,U> effectFactory) {
		return new DependencyManager2<E,T,U>(retriever, effectFactory.retriever);
	}
	
}
