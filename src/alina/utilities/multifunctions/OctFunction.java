package alina.utilities.multifunctions;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface OctFunction<A,B,C,D,E,F,G,H,R> {
	R apply(A a, B b, C c, D d, E e, F f, G g, H h);
	
	default <V> OctFunction<A, B, C, D, E, F, G, H, V> andThen(Function<? super R, ? extends V> after) {
		Objects.requireNonNull(after);
		return (A a, B b, C c, D d, E e, F f, G g, H h) -> after.apply(apply(a, b, c, d, e, f, g, h));
	}
}
