package alice.framework.handlers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import alice.framework.actions.Action;
import alice.framework.main.Brain;
import alice.framework.utilities.AliceLogger;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public abstract class Handler<E extends Event> {
	
	protected String name;
	protected boolean enableWhitelist;
	
	protected List<String> aliases;
	
	protected Handler(String name, boolean enableWhitelist, Class<E> type) {
		this.name = name;
		this.enableWhitelist = enableWhitelist;
		this.aliases = new ArrayList<String>();
		this.aliases.add(name);
		subscribe(type);
		redundantSubscribe(type);
	}
	
	private void subscribe(Class<E> type) {
		AliceLogger.info(String.format("Initializing %s module.", name), 2);
		Brain.client.on(type)
		.filter(event -> filter(event))
		.flatMap(event -> payload(event)
					.doOnError(e -> subscribe(type))
					.onErrorStop()
					.timeout(
							Duration.ofSeconds(10), 
							Mono.fromRunnable(() -> {
								AliceLogger.report(String.format("Timeout in %s", name));
								AliceLogger.error(String.format("Timeout in %s", name));
							})
					)
				)
		.subscribe();
		// if that doesnt work, catch the propagated timeout exception and resubscribe
		AliceLogger.info(String.format("%s Module initialized.", name), 2);
	}
	
	private AtomicReference<E> currentEvent = new AtomicReference<E>(null);
	
	private void redundantSubscribe(Class<E> type) {
		Brain.client.on(type)
		.filter(event -> filter(event))
		.flatMap(event -> watchdog(event, type))
		.subscribe();
	}
	
	private Mono<?> watchdog(E event, Class<E> type) {
		return Mono.fromRunnable(() -> {
			long now = System.currentTimeMillis();
			while( currentEvent.get() != event && System.currentTimeMillis() <= now + 5000 ) {
				try {
					synchronized(currentEvent) {
						currentEvent.wait(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if( currentEvent.get() != event ) {
				AliceLogger.report(String.format("Synchronization failure detected by watchdog in ", this.getClass().getName()));
				subscribe(type);
				execute(event);
			}
		});
	}
	
	protected abstract boolean trigger(E event);
	protected abstract Action execute(E event);
	
	protected boolean filter(E event) {
		return trigger(event);
	}
	
	protected Mono<?> payload(E event) {
		currentEvent.set(event);
		synchronized(currentEvent) {
			currentEvent.notifyAll();
		}
		return execute(event).toMono();
	}
	
	public String getName() {
		return name;
	}
	
	public boolean getEnableWhitelist() {
		return enableWhitelist;
	}
	
	public List<String> getAliases() {
		return new ArrayList<String>(aliases);
	}
}
