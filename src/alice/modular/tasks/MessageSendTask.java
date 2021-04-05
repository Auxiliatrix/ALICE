package alice.modular.tasks;

import alice.framework.tasks.Task;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class MessageSendTask extends Task<MessageChannel> {

	private String message;
	
	public MessageSendTask(String message) {
		this.message = message;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Mono apply(MessageChannel t) {
		return t.createMessage(message);
	}
	
}
