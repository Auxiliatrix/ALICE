package alice.framework.tasks;

import java.util.function.Function;

import reactor.core.publisher.Mono;

public abstract class Task<E> implements Function<E, Mono<?>> {
	
}
