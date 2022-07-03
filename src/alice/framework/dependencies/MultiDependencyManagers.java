package alice.framework.dependencies;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import alina.utilities.multifunctions.HexConsumer;
import alina.utilities.multifunctions.HexFunction;
import alina.utilities.multifunctions.OctConsumer;
import alina.utilities.multifunctions.OctFunction;
import alina.utilities.multifunctions.PentConsumer;
import alina.utilities.multifunctions.PentFunction;
import alina.utilities.multifunctions.QuadConsumer;
import alina.utilities.multifunctions.QuadFunction;
import alina.utilities.multifunctions.SeptConsumer;
import alina.utilities.multifunctions.SeptFunction;
import alina.utilities.multifunctions.TriConsumer;
import alina.utilities.multifunctions.TriFunction;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public class MultiDependencyManagers {

	public static class DependencyManager2<E extends Event, T, U> {
	
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		
		protected DependencyManager2(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2) {
			this.r1 = r1;
			this.r2 = r2;
		}
		
		public Function<DependencyMap<E>, Mono<?>> buildEffect(BiFunction<T,U,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2));
		}
		
		public Consumer<DependencyMap<E>> buildSideEffect(BiConsumer<T,U> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2));
		}
		
		public Function<DependencyMap<E>, Boolean> buildCondition(BiFunction<T,U,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1),d.<U>request(r2));
		}
		
		public <V> DependencyManager3<E, T, U, V> with(DependencyManager<E,V> effectFactory) {
			return new DependencyManager3<E, T, U, V>(r1, r2, effectFactory.retriever);
		}
	
	}
	
	public static class DependencyManager3<E extends Event, T, U, V> {
		
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		protected Function<E,  Mono<?>> r3;
		
		protected DependencyManager3(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2, Function<E, Mono<?>> r3) {
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
		}
		
		public Function<DependencyMap<E>, Mono<?>> buildEffect(TriFunction<T,U,V,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3));
		}
		
		public Consumer<DependencyMap<E>> buildSideEffect(TriConsumer<T,U,V> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2),d.<V>request(r3));
		}
		
		public Function<DependencyMap<E>, Boolean> buildCondition(TriFunction<T,U,V,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3));
		}
	
		public <W> DependencyManager4<E, T, U, V, W> with(DependencyManager<E,W> effectFactory) {
			return new DependencyManager4<E, T, U, V, W>(r1, r2, r3, effectFactory.retriever);
		}
		
	}
	
	public static class DependencyManager4<E extends Event, T, U, V, W> {
		
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		protected Function<E,  Mono<?>> r3;
		protected Function<E,  Mono<?>> r4;
		
		protected DependencyManager4(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2, Function<E, Mono<?>> r3,  Function<E, Mono<?>> r4) {
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
			this.r4 = r4;
		}
		
		public Function<DependencyMap<E>, Mono<?>> buildEffect(QuadFunction<T,U,V,W,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4));
		}
		
		public Consumer<DependencyMap<E>> buildSideEffect(QuadConsumer<T,U,V,W> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2),d.<V>request(r3),d.<W>request(r4));
		}
		
		public Function<DependencyMap<E>, Boolean> buildCondition(QuadFunction<T,U,V,W,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4));
		}
		
		public <X> DependencyManager5<E, T, U, V, W, X> with(DependencyManager<E,X> effectFactory) {
			return new DependencyManager5<E, T, U, V, W, X>(r1, r2, r3, r4, effectFactory.retriever);
		}
	
	}

	public static class DependencyManager5<E extends Event, T, U, V, W, X> {
		
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		protected Function<E,  Mono<?>> r3;
		protected Function<E,  Mono<?>> r4;
		protected Function<E,  Mono<?>> r5;
		
		protected DependencyManager5(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2, Function<E, Mono<?>> r3, Function<E, Mono<?>> r4, Function<E, Mono<?>> r5) {
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
			this.r4 = r4;
			this.r5 = r5;
		}
		
		public Function<DependencyMap<E>, Mono<?>> buildEffect(PentFunction<T,U,V,W,X,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5));
		}
		
		public Consumer<DependencyMap<E>> buildSideEffect(PentConsumer<T,U,V,W,X> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2),d.<V>request(r3),d.<W>request(r4),d.<X>request(r5));
		}
		
		public Function<DependencyMap<E>, Boolean> buildCondition(PentFunction<T,U,V,W,X,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5));
		}

		public <Y> DependencyManager6<E, T, U, V, W, X, Y> with(DependencyManager<E,Y> effectFactory) {
			return new DependencyManager6<E, T, U, V, W, X, Y>(r1, r2, r3, r4, r5, effectFactory.retriever);
		}
		
	}
	
	public static class DependencyManager6<E extends Event, T, U, V, W, X, Y> {
		
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		protected Function<E,  Mono<?>> r3;
		protected Function<E,  Mono<?>> r4;
		protected Function<E,  Mono<?>> r5;
		protected Function<E,  Mono<?>> r6;
		
		protected DependencyManager6(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2, Function<E, Mono<?>> r3, Function<E, Mono<?>> r4, Function<E, Mono<?>> r5, Function<E, Mono<?>> r6) {
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
			this.r4 = r4;
			this.r5 = r5;
			this.r6 = r6;
		}
		
		public Function<DependencyMap<E>, Mono<?>> buildEffect(HexFunction<T,U,V,W,X,Y,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5), d.<Y>request(r6));
		}		
		
		public Consumer<DependencyMap<E>> buildSideEffect(HexConsumer<T,U,V,W,X,Y> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2),d.<V>request(r3),d.<W>request(r4),d.<X>request(r5),d.<Y>request(r6));
		}
		
		public Function<DependencyMap<E>, Boolean> buildCondition(HexFunction<T,U,V,W,X,Y,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5), d.<Y>request(r6));
		}
	
		public <Z> DependencyManager7<E, T, U, V, W, X, Y, Z> with(DependencyManager<E,Z> effectFactory) {
			return new DependencyManager7<E, T, U, V, W, X, Y, Z>(r1, r2, r3, r4, r5, r6, effectFactory.retriever);
		}
		
	}
	
	public static class DependencyManager7<E extends Event, T, U, V, W, X, Y, Z> {
		
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		protected Function<E,  Mono<?>> r3;
		protected Function<E,  Mono<?>> r4;
		protected Function<E,  Mono<?>> r5;
		protected Function<E,  Mono<?>> r6;
		protected Function<E,  Mono<?>> r7;
		
		protected DependencyManager7(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2, Function<E, Mono<?>> r3, Function<E, Mono<?>> r4, Function<E, Mono<?>> r5, Function<E, Mono<?>> r6, Function<E, Mono<?>> r7) {
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
			this.r4 = r4;
			this.r5 = r5;
			this.r6 = r6;
			this.r7 = r7;
		}
		
		public Function<DependencyMap<E>, Mono<?>> buildEffect(SeptFunction<T,U,V,W,X,Y,Z,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5), d.<Y>request(r6), d.<Z>request(r7));
		}		
		
		public Consumer<DependencyMap<E>> buildSideEffect(SeptConsumer<T,U,V,W,X,Y,Z> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2),d.<V>request(r3),d.<W>request(r4),d.<X>request(r5),d.<Y>request(r6),d.<Z>request(r7));
		}
		
		public Function<DependencyMap<E>, Boolean> buildCondition(SeptFunction<T,U,V,W,X,Y,Z,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5), d.<Y>request(r6), d.<Z>request(r7));
		}
	
		public <A> DependencyManager8<E, T, U, V, W, X, Y, Z, A> with(DependencyManager<E,A> effectFactory) {
			return new DependencyManager8<E, T, U, V, W, X, Y, Z, A>(r1, r2, r3, r4, r5, r6, r7, effectFactory.retriever);
		}
		
	}

	public static class DependencyManager8<E extends Event, T, U, V, W, X, Y, Z, A> {
		
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		protected Function<E,  Mono<?>> r3;
		protected Function<E,  Mono<?>> r4;
		protected Function<E,  Mono<?>> r5;
		protected Function<E,  Mono<?>> r6;
		protected Function<E,  Mono<?>> r7;
		protected Function<E,  Mono<?>> r8;
		
		protected DependencyManager8(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2, Function<E, Mono<?>> r3, Function<E, Mono<?>> r4, Function<E, Mono<?>> r5, Function<E, Mono<?>> r6, Function<E, Mono<?>> r7, Function<E, Mono<?>> r8) {
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
			this.r4 = r4;
			this.r5 = r5;
			this.r6 = r6;
			this.r7 = r7;
			this.r8 = r8;
		}
		
		public Consumer<DependencyMap<E>> buildSideEffect(OctConsumer<T,U,V,W,X,Y,Z,A> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2),d.<V>request(r3),d.<W>request(r4),d.<X>request(r5),d.<Y>request(r6),d.<Z>request(r7),d.<A>request(r8));
		}
		
		public Function<DependencyMap<E>, Mono<?>> buildEffect(OctFunction<T,U,V,W,X,Y,Z,A,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5), d.<Y>request(r6), d.<Z>request(r7), d.<A>request(r8));
		}		
		
		public Function<DependencyMap<E>, Boolean> buildCondition(OctFunction<T,U,V,W,X,Y,Z,A,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5), d.<Y>request(r6), d.<Z>request(r7), d.<A>request(r8));
		}
	
	}
	
}