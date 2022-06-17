package alice.framework.old.tasks;

import java.util.function.Function;

import reactor.core.publisher.Mono;

/**
 * An abstract base class for a structure that processes an Event instance and converts it into a response.
 * @author Auxiliatrix
 *
 * @param <E> Class to accept
 */
public abstract class Task<E> implements Function<E, Mono<?>> {
	// TODO: make it work with multi dependents
}
