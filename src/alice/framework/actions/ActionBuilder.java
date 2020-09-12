package alice.framework.actions;

import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class ActionBuilder {
	
	private Mono<?> mono;
	
	public ActionBuilder() {
		this.mono = null;
	}
	
	public static ActionBuilder create() {
		return new ActionBuilder();
	}
	
	private void addMono(Mono<?> addition) {
		mono = (mono == null) ? addition : mono.and(addition);
	}
	
	public Mono<?> build() {
		return mono;
	}
	
	public ActionBuilder addCreateMessageAction(Mono<MessageChannel> channel, String content) {
		addMono(channel.block().createMessage(content)); // Imperative
		// addMono(channel.flatMap(c -> c.createMessage(content))); // Reactive
		return this;
	}

}
