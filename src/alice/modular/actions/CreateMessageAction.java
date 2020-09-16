package alice.modular.actions;

import java.util.function.Consumer;

import alice.framework.actions.Action;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

public class CreateMessageAction extends Action {
		
	public CreateMessageAction(Mono<MessageChannel> channel, String content) {
		super();
		this.mono = channel.block().createMessage(content);
	}
	
	public CreateMessageAction(Mono<MessageChannel> channel, Consumer<? super EmbedCreateSpec> spec) {
		super();
		this.mono = channel.block().createEmbed(spec);
	}
	
}
