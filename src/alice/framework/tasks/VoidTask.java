package alice.framework.tasks;

import reactor.core.publisher.Mono;

/**
 * A type of Task that simply runs its execute() function
 * @author Auxiliatrix
 *
 * @param <E> Event class to accept
 */
public abstract class VoidTask<E> extends Task<E> {

	/**
	 * The code block to run when the Mono response generated from this class is executed.
	 * @param t Event instance to process
	 */
	protected abstract void execute(E t);
	
	@Override
	public Mono<?> apply(E t) {
		return Mono.fromRunnable(() -> execute(t));
	}

}
