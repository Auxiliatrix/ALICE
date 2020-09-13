package alice.framework.actions;

import alice.framework.utilities.AliceLogger;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import reactor.core.publisher.Mono;

public class SayAction extends VoidAction {
// 					User recipient = channel.cast(PrivateChannel.class).block().getRecipients().blockFirst();
	public SayAction(String message, Mono<Guild> guild, Mono<MessageChannel> channel) {
		super(() -> {
			AliceLogger.say(message, guild.block().getName(), channel.cast(TextChannel.class).block().getName());
		});
	}

}
