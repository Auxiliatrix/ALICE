package alice.framework.tasks;

import java.util.function.Function;

import reactor.core.publisher.Mono;

/**
 * An abstract base class for a structure that processes an Event instance and converts it into a response.
 * @author Auxiliatrix
 *
 * @param <E> Event class to accept
 */
public abstract class Task<E> implements Function<E, Mono<?>> {
	
}
