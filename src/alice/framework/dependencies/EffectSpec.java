package alice.framework.dependencies;

import java.util.function.Function;

import reactor.core.publisher.Mono;

public abstract class EffectSpec<T> implements Function<T, Mono<?>> {

}
