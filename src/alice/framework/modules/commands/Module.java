package alice.framework.modules.commands;

import java.time.Duration;

import alice.framework.main.Brain;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public abstract class Module<E extends Event> {
	
	private Command<E> command;
		
	protected Module(Class<E> type) {
		load(type);
		this.command = buildCommand();
	}
	
	protected void load(Class<E> type) {
//		Brain.client.on(type)
//			.flatMap(e -> handle(e))
//			.retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(5)))
//			.doOnError(e -> {
//				System.err.println("Propagated error detected.");
//				Brain.client.logout().block();
//			})
//			.subscribe();
		Brain.client.on(type).subscribe(e -> handle(e)
				.retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(5)))
				.doOnError(f -> {
					System.err.println("Propagated error detected.");
					Brain.client.logout().block();
				})
				.block()
			);
	}
	
	public Mono<?> handle(E event) {
		return command.apply(event);
	}
	
	public abstract Command<E> buildCommand();	
	
}
