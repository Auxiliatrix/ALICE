package alina.utilities.multifunctions;

@FunctionalInterface
public interface OctConsumer<A,B,C,D,E,F,G,H> {
	void accept(A a, B b, C c, D d, E e, F f, G g, H h);
}
