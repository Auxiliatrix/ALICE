package alice.framework.handlers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import alice.framework.actions.Action;
import alice.framework.main.Brain;
import alice.framework.utilities.AliceLogger;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public abstract class Handler<E extends Event> {
	
	protected String name;
	protected boolean enableWhitelist;
	
	protected List<String> aliases;

	protected Handler(String name, boolean enableWhitelist, Class<E> type) {
		this.name = name;
		this.enableWhitelist = enableWhitelist;
		this.aliases = new ArrayList<String>();
		this.aliases.add(name);
		Brain.client.on(type)
		.filter(event -> filter(event))
		.flatMap(event -> payload(event))
		.retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(2))
		.doBeforeRetry(signal -> {AliceLogger.error(String.format("Error occured during execution in %s. Retrying.", name));}))
		.subscribe();
		AliceLogger.info(String.format("%s Module initialized.", name), 2);
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
	
	public boolean getEnableWhitelist() {
		return enableWhitelist;
	}
	
	public List<String> getAliases() {
		return new ArrayList<String>(aliases);
	}
}
