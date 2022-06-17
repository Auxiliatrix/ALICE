package alice.framework.modules.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public class DependencyFactoryBuilder<E2 extends Event> {
	
	private List<Function<E2, Mono<?>>> retrievers;
	
	public DependencyFactoryBuilder() {
		retrievers = new ArrayList<Function<E2, Mono<?>>>();
	}
	
	public DependencyFactoryBuilder<E2> addDependency(Function<E2, Mono<?>> dependency) {
		retrievers.add(dependency);
		return this;
	}
	
	public DependencyFactory<E2> build() {
		return new DependencyFactory<E2>(retrievers);
	}
	
	public Mono<Dependency<E2>> build(E2 event) {
		List<Mono<?>> dependencies = new ArrayList<Mono<?>>();
		for( Function<E2, Mono<?>> retriever : retrievers ) {
			dependencies.add(retriever.apply(event));
		}
		return Mono.zip(dependencies, a -> {
			Map<Function<E2,Mono<?>>,Object> referenceMap = new HashMap<Function<E2,Mono<?>>,Object>();
			for( int f=0; f<a.length; f++ ) {
				referenceMap.put(retrievers.get(f), a[f]);
			}
			return new Dependency<E2>(referenceMap);
		});
	}
}
