package alice.framework.dependencies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public class DependencyFactory<E extends Event> {

	public static class Builder<E2 extends Event> {
		
		private List<Function<E2, Mono<?>>> retrievers;
		
		protected Builder() {
			retrievers = new ArrayList<Function<E2, Mono<?>>>();
		}
		
		// TODO: Add multiple dependencies to create multieffect factories
		
		public <T> EffectFactory<E2,T> addDependency(Function<E2, Mono<?>> dependency) {
			retrievers.add(dependency);
			return new EffectFactory<E2,T>(dependency);
		}
		
		public <T> EffectFactory<E2,T> addWrappedDependency(Function<E2, ?> dependency) {
			Function<E2, Mono<?>> wrappedDependency = dependency.andThen(t -> Mono.just(t));
			retrievers.add(wrappedDependency);
			return new EffectFactory<E2,T>(wrappedDependency);
		}
		
		public DependencyFactory<E2> buildDependencyFactory() {
			return new DependencyFactory<E2>(new ArrayList<Function<E2,Mono<?>>>(retrievers));
		}
		
	}
	
	private List<Function<E, Mono<?>>> retrievers;
	
	public static <E extends Event> Builder<E> builder() {
		return new Builder<E>();
	}
	
	protected DependencyFactory(List<Function<E, Mono<?>>> retrievers) {
		this.retrievers = retrievers;
	}
	
	public Mono<Dependency<E>> getDependency(E event) {
		List<Mono<?>> dependencies = new ArrayList<Mono<?>>();
		for( Function<E, Mono<?>> retriever : retrievers ) {
			try {
				dependencies.add(retriever.apply(event));
			} catch(Exception e) {
				dependencies.add(Mono.empty());
			}
		}
		
		return Mono.fromSupplier(() -> {
			Map<Function<E,Mono<?>>,Object> referenceMap = new HashMap<Function<E,Mono<?>>,Object>();
			for( int f=0; f<dependencies.size(); f++ ) {
				try {
					referenceMap.put(retrievers.get(f), dependencies.get(f).block());
				} catch (Exception e) {
					System.err.println("Error caught while creating dependency for " + event.getClass());
					referenceMap.put(retrievers.get(f), null);
				}
			}
			return new Dependency<E>(referenceMap, event);
		}).share();
	}
	
}
