package alina.utilities.multifunctions;

@FunctionalInterface
public interface SeptConsumer<A,B,C,D,E,F,G> {
	void accept(A a, B b, C c, D d, E e, F f, G g);
}
