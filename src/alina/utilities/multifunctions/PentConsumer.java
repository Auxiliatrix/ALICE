package alina.utilities.multifunctions;

@FunctionalInterface
public interface PentConsumer<A,B,C,D,E> {
	void accept(A a, B b, C c, D d, E e);
}
