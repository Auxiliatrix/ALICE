package alice.framework.tasks;

import reactor.core.publisher.Mono;

public abstract class VoidTask<E> extends Task<E> {

	protected abstract void execute(E t);
	
	@Override
	public Mono<?> apply(E t) {
		return Mono.fromRunnable(() -> execute(t));
	}

}
