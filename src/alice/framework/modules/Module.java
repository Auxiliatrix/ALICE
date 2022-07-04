package alice.framework.modules;

import java.time.Duration;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.main.Brain;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public abstract class Module<E extends Event> {
	
	private Command<E> command;
		
	protected Module(Class<E> type) {
		load(type);
		this.command = buildCommand(DependencyFactory.<E>builder());
	}
	
	protected void load(Class<E> type) {
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
	
	public abstract Command<E> buildCommand(DependencyFactory.Builder<E> dfb);	
	
}
