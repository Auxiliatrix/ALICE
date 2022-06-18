package alice.framework.modules.tasks;

import java.util.function.Function;

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
	
}
