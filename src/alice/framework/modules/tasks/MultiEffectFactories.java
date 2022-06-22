package alice.framework.modules.tasks;

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

public class MultiEffectFactories {

	public static class EffectFactory2<E extends Event, T, U> {
	
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		
		protected EffectFactory2(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2) {
			this.r1 = r1;
			this.r2 = r2;
		}
		
		public Function<Dependency<E>, Mono<?>> getEffect(BiFunction<T,U,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2));
		}
		
		public Consumer<Dependency<E>> getEffect(BiConsumer<T,U> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2));
		}
		
		public Function<Dependency<E>, Boolean> getCondition(BiFunction<T,U,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1),d.<U>request(r2));
		}
		
		public <V> EffectFactory3<E, T, U, V> with(EffectFactory<E,V> effectFactory) {
			return new EffectFactory3<E, T, U, V>(r1, r2, effectFactory.retriever);
		}
	
	}
	
	public static class EffectFactory3<E extends Event, T, U, V> {
		
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		protected Function<E,  Mono<?>> r3;
		
		protected EffectFactory3(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2, Function<E, Mono<?>> r3) {
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
		}
		
		public Function<Dependency<E>, Mono<?>> getEffect(TriFunction<T,U,V,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3));
		}
		
		public Consumer<Dependency<E>> getEffect(TriConsumer<T,U,V> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2),d.<V>request(r3));
		}
		
		public Function<Dependency<E>, Boolean> getCondition(TriFunction<T,U,V,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3));
		}
	
		public <W> EffectFactory4<E, T, U, V, W> with(EffectFactory<E,W> effectFactory) {
			return new EffectFactory4<E, T, U, V, W>(r1, r2, r3, effectFactory.retriever);
		}
		
	}
	
	public static class EffectFactory4<E extends Event, T, U, V, W> {
		
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		protected Function<E,  Mono<?>> r3;
		protected Function<E,  Mono<?>> r4;
		
		protected EffectFactory4(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2, Function<E, Mono<?>> r3,  Function<E, Mono<?>> r4) {
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
			this.r4 = r4;
		}
		
		public Function<Dependency<E>, Mono<?>> getEffect(QuadFunction<T,U,V,W,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4));
		}
		
		public Consumer<Dependency<E>> getEffect(QuadConsumer<T,U,V,W> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2),d.<V>request(r3),d.<W>request(r4));
		}
		
		public Function<Dependency<E>, Boolean> getCondition(QuadFunction<T,U,V,W,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4));
		}
		
		public <X> EffectFactory5<E, T, U, V, W, X> with(EffectFactory<E,X> effectFactory) {
			return new EffectFactory5<E, T, U, V, W, X>(r1, r2, r3, r4, effectFactory.retriever);
		}
	
	}

	public static class EffectFactory5<E extends Event, T, U, V, W, X> {
		
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		protected Function<E,  Mono<?>> r3;
		protected Function<E,  Mono<?>> r4;
		protected Function<E,  Mono<?>> r5;
		
		protected EffectFactory5(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2, Function<E, Mono<?>> r3, Function<E, Mono<?>> r4, Function<E, Mono<?>> r5) {
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
			this.r4 = r4;
			this.r5 = r5;
		}
		
		public Function<Dependency<E>, Mono<?>> getEffect(PentFunction<T,U,V,W,X,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5));
		}
		
		public Consumer<Dependency<E>> getEffect(PentConsumer<T,U,V,W,X> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2),d.<V>request(r3),d.<W>request(r4),d.<X>request(r5));
		}
		
		public Function<Dependency<E>, Boolean> getCondition(PentFunction<T,U,V,W,X,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5));
		}

		public <Y> EffectFactory6<E, T, U, V, W, X, Y> with(EffectFactory<E,Y> effectFactory) {
			return new EffectFactory6<E, T, U, V, W, X, Y>(r1, r2, r3, r4, r5, effectFactory.retriever);
		}
		
	}
	
	public static class EffectFactory6<E extends Event, T, U, V, W, X, Y> {
		
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		protected Function<E,  Mono<?>> r3;
		protected Function<E,  Mono<?>> r4;
		protected Function<E,  Mono<?>> r5;
		protected Function<E,  Mono<?>> r6;
		
		protected EffectFactory6(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2, Function<E, Mono<?>> r3, Function<E, Mono<?>> r4, Function<E, Mono<?>> r5, Function<E, Mono<?>> r6) {
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
			this.r4 = r4;
			this.r5 = r5;
			this.r6 = r6;
		}
		
		public Function<Dependency<E>, Mono<?>> getEffect(HexFunction<T,U,V,W,X,Y,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5), d.<Y>request(r6));
		}		
		
		public Consumer<Dependency<E>> getEffect(HexConsumer<T,U,V,W,X,Y> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2),d.<V>request(r3),d.<W>request(r4),d.<X>request(r5),d.<Y>request(r6));
		}
		
		public Function<Dependency<E>, Boolean> getCondition(HexFunction<T,U,V,W,X,Y,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5), d.<Y>request(r6));
		}
	
		public <Z> EffectFactory7<E, T, U, V, W, X, Y, Z> with(EffectFactory<E,Z> effectFactory) {
			return new EffectFactory7<E, T, U, V, W, X, Y, Z>(r1, r2, r3, r4, r5, r6, effectFactory.retriever);
		}
		
	}
	
	public static class EffectFactory7<E extends Event, T, U, V, W, X, Y, Z> {
		
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		protected Function<E,  Mono<?>> r3;
		protected Function<E,  Mono<?>> r4;
		protected Function<E,  Mono<?>> r5;
		protected Function<E,  Mono<?>> r6;
		protected Function<E,  Mono<?>> r7;
		
		protected EffectFactory7(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2, Function<E, Mono<?>> r3, Function<E, Mono<?>> r4, Function<E, Mono<?>> r5, Function<E, Mono<?>> r6, Function<E, Mono<?>> r7) {
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
			this.r4 = r4;
			this.r5 = r5;
			this.r6 = r6;
			this.r7 = r7;
		}
		
		public Function<Dependency<E>, Mono<?>> getEffect(SeptFunction<T,U,V,W,X,Y,Z,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5), d.<Y>request(r6), d.<Z>request(r7));
		}		
		
		public Consumer<Dependency<E>> getEffect(SeptConsumer<T,U,V,W,X,Y,Z> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2),d.<V>request(r3),d.<W>request(r4),d.<X>request(r5),d.<Y>request(r6),d.<Z>request(r7));
		}
		
		public Function<Dependency<E>, Boolean> getCondition(SeptFunction<T,U,V,W,X,Y,Z,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5), d.<Y>request(r6), d.<Z>request(r7));
		}
	
		public <A> EffectFactory8<E, T, U, V, W, X, Y, Z, A> with(EffectFactory<E,A> effectFactory) {
			return new EffectFactory8<E, T, U, V, W, X, Y, Z, A>(r1, r2, r3, r4, r5, r6, r7, effectFactory.retriever);
		}
		
	}

	public static class EffectFactory8<E extends Event, T, U, V, W, X, Y, Z, A> {
		
		protected Function<E,  Mono<?>> r1;
		protected Function<E,  Mono<?>> r2;
		protected Function<E,  Mono<?>> r3;
		protected Function<E,  Mono<?>> r4;
		protected Function<E,  Mono<?>> r5;
		protected Function<E,  Mono<?>> r6;
		protected Function<E,  Mono<?>> r7;
		protected Function<E,  Mono<?>> r8;
		
		protected EffectFactory8(Function<E, Mono<?>> r1,Function<E, Mono<?>> r2, Function<E, Mono<?>> r3, Function<E, Mono<?>> r4, Function<E, Mono<?>> r5, Function<E, Mono<?>> r6, Function<E, Mono<?>> r7, Function<E, Mono<?>> r8) {
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
			this.r4 = r4;
			this.r5 = r5;
			this.r6 = r6;
			this.r7 = r7;
			this.r8 = r8;
		}
		
		public Consumer<Dependency<E>> getEffect(OctConsumer<T,U,V,W,X,Y,Z,A> spec) {
			return d -> spec.accept(d.<T>request(r1),d.<U>request(r2),d.<V>request(r3),d.<W>request(r4),d.<X>request(r5),d.<Y>request(r6),d.<Z>request(r7),d.<A>request(r8));
		}
		
		public Function<Dependency<E>, Mono<?>> getEffect(OctFunction<T,U,V,W,X,Y,Z,A,Mono<?>> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5), d.<Y>request(r6), d.<Z>request(r7), d.<A>request(r8));
		}		
		
		public Function<Dependency<E>, Boolean> getCondition(OctFunction<T,U,V,W,X,Y,Z,A,Boolean> spec) {
			return d -> spec.apply(d.<T>request(r1), d.<U>request(r2), d.<V>request(r3), d.<W>request(r4), d.<X>request(r5), d.<Y>request(r6), d.<Z>request(r7), d.<A>request(r8));
		}
	
	}
	
}