package alice.framework.modules.commands;

import alice.framework.main.Brain;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public abstract class Module<E extends Event> {
	
	private Command<E> command;
		
	protected Module(Class<E> type) {
		load(type);
		this.command = buildCommand();
	}
	
	protected void load(Class<E> type) {
		Brain.client.on(type).flatMap(e -> handle(e)).subscribe();
	}
	
	public Mono<?> handle(E event) {
		return command.apply(event);
	}
	
	public abstract Command<E> buildCommand();	
	
}
