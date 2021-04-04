package alice.framework.tasks;

import java.util.function.Function;

import reactor.core.publisher.Mono;

public abstract class Task<E> implements Function<E, Mono<Void>> {

	protected abstract void execute(E t);
	
	@Override
	public Mono<Void> apply(E t) {
		return Mono.fromRunnable( () -> execute(t) );
	}

}
