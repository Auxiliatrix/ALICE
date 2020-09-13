package alice.framework.handlers;

import java.lang.reflect.ParameterizedType;

import alice.framework.actions.Action;
import alice.framework.main.Brain;
import alice.framework.structures.PermissionProfile;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public abstract class Handler<E extends Event> {
	
	protected String name;
	protected String category;
	protected boolean enableWhitelist;
	
	protected boolean allowBots;
	protected PermissionProfile restrictions;
	
	protected Handler() {
		name = "Handler";
		category = "Default";
		allowBots = false;
		enableWhitelist = false;
		restrictions = null;
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Class<E> type = ((Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);	// Incredibly janky way to get E.class
		Brain.client.on(type)
		.filter(event -> filter(event))
		.flatMap(event -> payload(event))
		.subscribe();
	}
	
	public Handler<E> withName(String name) {
		this.name = name;
		return this;
	}
	
	public Handler<E> withCategory(String category) {
		this.category = category;
		return this;
	}
	
	public Handler<E> withAllowBots() {
		this.allowBots = true;
		return this;
	}
	
	public Handler<E> withEnableWhitelist() {
		this.enableWhitelist = true;
		return this;
	}
	
	public Handler<E> withRestrictions(PermissionProfile restrictions) {
		this.restrictions = restrictions;
		return this;
	}
	
	protected abstract boolean trigger(E event);
	protected abstract Action execute(E event);
	
	protected boolean filter(E event) {
		return trigger(event);
	}
	
	protected Mono<?> payload(E event) {
		return execute(event).toMono();
	}
}
