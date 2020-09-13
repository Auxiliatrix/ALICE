package alice.framework.actions;

import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class Action {
	
	protected Mono<?> mono;
	
	public Action() {
		this.mono = null;
	}
	
	public final Mono<?> toMono() {
		return mono;
	}
	
	public Action addCreateMessageAction(Mono<MessageChannel> channel, String content) {
		addMono(channel.block().createMessage(content)); // Imperative
		//addMono(channel.flatMap(c -> c.createMessage(content))); // Reactive
		return this;
	}

	public Action addAction(Action action) {
		mono = (mono == null) ? action.toMono() : mono.and(action.toMono());
		return this;
	}
	
	public Action addMono(Mono<?> newMono) {
		mono = (mono == null) ? newMono : mono.and(newMono);
		return this;
	}
	
}
