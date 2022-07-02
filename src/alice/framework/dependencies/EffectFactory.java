package alice.framework.dependencies;

import java.util.function.Consumer;
import java.util.function.Function;

import alice.framework.dependencies.MultiEffectFactories.EffectFactory2;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public class EffectFactory<E extends Event, T> {

	protected Function<E,Mono<?>> retriever;
	
	protected EffectFactory(Function<E,Mono<?>> retriever) {
		this.retriever = retriever;
	}
	
	public Function<Dependency<E>, Mono<?>> getEffect(Function<T,Mono<?>> spec) {
		return d -> spec.apply(d.<T>request(retriever));
	}
	
	public Consumer<Dependency<E>> getSideEffect(Consumer<T> spec) {
		return d -> spec.accept(d.<T>request(retriever));
	}
	
	public Function<Dependency<E>, Boolean> getCondition(Function<T,Boolean> spec) {
		return d -> spec.apply(d.<T>request(retriever));
	}
	
	public T requestFrom(Dependency<E> dependency) {
		return dependency.<T>request(retriever);
	}
	
	public <U> EffectFactory2<E,T,U> with(EffectFactory<E,U> effectFactory) {
		return new EffectFactory2<E,T,U>(retriever, effectFactory.retriever);
	}
	
}
