package alice.framework.dependencies;

import java.util.Map;
import java.util.function.Function;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public class DependencyMap<E extends Event> {

	private final Map<Function<E,Mono<?>>, Object> referenceMap;
	protected E event;
	
	protected DependencyMap(Map<Function<E,Mono<?>>, Object> referenceMap, E event) {
		this.referenceMap = referenceMap;
		this.event = event;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T request(Function<E,Mono<?>> retriever) {
		T response;
		try {
			response = (T) referenceMap.get(retriever);
		} catch( ClassCastException e ) {
			response = null;
			System.out.println("Invalid type requested from DependencyManager.");
		}
		return response;
	}
	
	public E getEvent() {
		return event;
	}
	
}
