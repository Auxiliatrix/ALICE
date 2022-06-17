package alice.framework.modules.tasks;

import java.util.function.Function;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class MessageChannelEffect extends Effect<MessageCreateEvent> {

	protected Function<MessageChannel, Mono<?>> shortcut;
	
	public MessageChannelEffect(Function<MessageChannel, Mono<?>> shortcut) {
		this.shortcut = shortcut;
	}
	
	@Override
	public Mono<?> apply(Dependency<MessageCreateEvent> t) {
		return shortcut.apply(
				t.<MessageChannel>request(
						(mce -> mce.getMessage().getChannel())
			));
	}
	
}
