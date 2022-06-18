package alice.framework.modules.tasks;

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
		
		public <T> EffectFactory<E2,T> addDependency(Function<E2, Mono<?>> dependency) {
			retrievers.add(dependency);
			return new EffectFactory<E2,T>(dependency);
		}
		
		public DependencyFactory<E2> buildDependencyFactory() {
			return new DependencyFactory<E2>(retrievers);
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
			dependencies.add(retriever.apply(event));
		}
		return Mono.zip(dependencies, a -> {
			Map<Function<E,Mono<?>>,Object> referenceMap = new HashMap<Function<E,Mono<?>>,Object>();
			for( int f=0; f<a.length; f++ ) {
				referenceMap.put(retrievers.get(f), a[f]);
			}
			return new Dependency<E>(referenceMap, event);
		}).share();
	}
	
}