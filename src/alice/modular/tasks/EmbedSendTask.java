package alice.modular.tasks;

import alice.framework.old.tasks.Task;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

public class EmbedSendTask extends Task<MessageChannel> {

	private EmbedCreateSpec embed;
	
	public EmbedSendTask(EmbedCreateSpec embed) {
		this.embed = embed;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Mono apply(MessageChannel t) {
		return t.createMessage(embed);
	}
	
}
