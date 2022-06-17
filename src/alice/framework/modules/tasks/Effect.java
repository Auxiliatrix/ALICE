package alice.framework.modules.tasks;

import java.util.function.Function;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public class Effect<E extends Event> implements Function<Dependency<E>, Mono<?>> {

	public Effect() {
		
	}
	
	@Override
	public Mono<?> apply(Dependency<E> t) {

		// TODO Auto-generated method stub
		return null;
	}

	
	
}
