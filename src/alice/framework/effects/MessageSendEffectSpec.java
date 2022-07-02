package alice.framework.effects;

import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class MessageSendEffectSpec extends EffectSpec<MessageChannel> {

	protected String message;
	
	public MessageSendEffectSpec(String message) {
		this.message = message;
	}
	
	@Override
	public Mono<?> apply(MessageChannel t) {
		return t.createMessage(message);
	}

}
