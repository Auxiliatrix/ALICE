package alice.modular.actions;

import java.util.function.Consumer;

import alice.framework.actions.Action;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

@Deprecated
public class MessageCreateAction extends Action {
		
	public MessageCreateAction(Mono<MessageChannel> channel, String content) {
		super(channel.block().createMessage(content));
	}
	
	public MessageCreateAction(Mono<MessageChannel> channel, Consumer<? super EmbedCreateSpec> spec) {
		super(channel.block().createEmbed(spec));
	}
	
}
