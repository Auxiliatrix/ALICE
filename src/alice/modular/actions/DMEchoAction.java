package alice.modular.actions;

import alice.framework.actions.VoidAction;
import alice.framework.utilities.AliceLogger;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import reactor.core.publisher.Mono;

public class DMEchoAction extends VoidAction {

	public DMEchoAction(String message, Mono<MessageChannel> channel) {
		super(() -> {
			User recipient = channel.cast(PrivateChannel.class).block().getRecipients().blockFirst();
			AliceLogger.DMEcho(message, String.format("%s#%s", recipient.getUsername(), recipient.getDiscriminator()));
		});
	}

}
