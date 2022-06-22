package alina.utilities.multifunctions;

@FunctionalInterface
public interface HexConsumer<A,B,C,D,E,F> {
	void accept(A a, B b, C c, D d, E e, F f);
}