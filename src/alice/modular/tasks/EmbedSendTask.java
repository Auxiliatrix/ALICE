package alice.modular.tasks;

import java.util.function.Consumer;

import alice.framework.tasks.Task;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

public class EmbedSendTask extends Task<MessageChannel> {

	private Consumer<? super EmbedCreateSpec> embed;
	
	public EmbedSendTask(Consumer<? super EmbedCreateSpec> embed) {
		this.embed = embed;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Mono apply(MessageChannel t) {
		return t.createEmbed(embed);
	}
	
}
