package alice.modular.tasks;

import alice.framework.tasks.Task;
import discord4j.core.object.entity.channel.MessageChannel;

public class MessageSendTask extends Task<MessageChannel> {

	private String message;
	
	public MessageSendTask(String message) {
		this.message = message;
	}

	@Override
	protected void execute(MessageChannel t) {
		t.createMessage(message).block();
	}
	
}
