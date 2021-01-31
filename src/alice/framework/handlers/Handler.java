package alice.framework.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.utilities.AliceLogger;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;

public abstract class Handler<E extends Event> {
	
	protected String name;
	
	protected List<String> aliases;
	
	protected Handler(String name, Class<E> type) {
		this.name = name;
		this.aliases = new ArrayList<String>();
		this.aliases.add(name);
		subscribe(type);
		//redundantSubscribe(type);
	}
	
	protected void subscribe(Class<E> type) {
		AliceLogger.info(String.format("Initializing %s module.", name), 2);
		Brain.client.on(type)
		.filter(event -> filter(event))
//		.flatMap(event -> payload(event)
//					.doOnError(e -> subscribe(type))
//					.onErrorStop()
//				)
		.subscribe(event -> payload(event).block());
		AliceLogger.info(String.format("%s Module initialized.", name), 2);
	}
	
	protected AtomicReference<E> currentEvent = new AtomicReference<E>(null);
	
	protected void redundantSubscribe(Class<E> type) {
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
				AliceLogger.report(String.format("Synchronization failure detected by watchdog in %s", this.getClass().getName()));
				AliceLogger.error(String.format("Synchronization failure detected by watchdog in %s", this.getClass().getName()));
				subscribe(type);
				//return payload(event);
			}
		});
	}
	
	protected abstract boolean trigger(E event);
	protected abstract void execute(E event);
	
	protected boolean filter(E event) {
		return trigger(event);
	}
	
	protected Mono<?> payload(E event) {
		currentEvent.set(event);
		synchronized(currentEvent) {
			currentEvent.notifyAll();
		}
		Thread thread = new Thread(() -> {
			execute(event);
		});
		return Mono.fromRunnable(() -> thread.start());
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getAliases() {
		return new ArrayList<String>(aliases);
	}
	
	public boolean isEnabled( boolean whitelist, Mono<Guild> guild ) {
		if( guild.block() == null ) {
			return false;
		}
		if( !Brain.guildIndex.containsKey(guild.block().getId().asString()) ) {
			return false;
		}
		AtomicSaveFile guildData = Brain.guildIndex.get(guild.block().getId().asString());
		return (!whitelist || guildData.has(String.format("module_enable_%s", name))) && !guildData.has(String.format("module_disable_%s", name));
	}
}
