package alice.framework.modules.tasks;

import java.util.function.Consumer;
import java.util.function.Function;

import alice.framework.modules.tasks.MultiEffectFactories.EffectFactory2;
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
	
	public Consumer<Dependency<E>> getEffect(Consumer<T> spec) {
		return d -> spec.accept(d.<T>request(retriever));
	}
	
	public Function<Dependency<E>, Boolean> getCondition(Function<T,Boolean> spec) {
		return d -> spec.apply(d.<T>request(retriever));
	}
	
	protected Function<E,Mono<?>> getRetriever() {
		return retriever;
	}
	
	public <U> EffectFactory2<E,T,U> with(EffectFactory<E,U> effectFactory) {
		return new EffectFactory2<E,T,U>(getRetriever(), effectFactory.getRetriever());
	}
	
}
