package alice.framework.handlers;

import java.lang.reflect.ParameterizedType;

import alice.framework.main.Brain;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public class Handler<E extends Event> {
	
	public Handler() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Class<E> type = ((Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);	// Incredibly janky way to get E.class
		Brain.client.on(type)
		.filter(event -> trigger(event))
		.flatMap(event -> execute(event))
		.subscribe();
	}
	
	protected boolean trigger(E event) {
		return false;
	}
	
	protected Mono<?> execute(E event) {
		return null;
	}
}
