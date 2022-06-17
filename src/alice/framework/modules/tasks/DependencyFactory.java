package alice.framework.modules.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public class DependencyFactory<E extends Event> {

	private List<Function<E, Mono<?>>> retrievers;
	
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
			return new Dependency<E>(referenceMap);
		});
	}
	
}
