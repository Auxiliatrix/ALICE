package alice.framework.handlers;

import alice.framework.actions.Action;
import alice.framework.main.Brain;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public abstract class Handler<E extends Event> {
	
	protected String name;
	protected String category;
	protected boolean enableWhitelist;

	protected Handler(String name, String category, boolean enableWhitelist, Class<E> type) {
		this.name = name;
		this.category = category;
		this.enableWhitelist = enableWhitelist;
		
		Brain.client.on(type)
		.filter(event -> filter(event))
		.flatMap(event -> payload(event))
		.subscribe();
	}
	
	protected abstract boolean trigger(E event);
	protected abstract Action execute(E event);
	
	protected boolean filter(E event) {
		return trigger(event);
	}
	
	protected Mono<?> payload(E event) {
		return execute(event).toMono();
	}
	
	public String getName() {
		return name;
	}
	
	public String getCategory() {
		return category;
	}
	
	public boolean getEnableWhitelist() {
		return enableWhitelist;
	}
}
