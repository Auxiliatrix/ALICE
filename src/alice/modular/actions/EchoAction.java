package alice.modular.actions;

import java.util.Optional;

import alice.framework.actions.VoidAction;
import alice.framework.utilities.AliceLogger;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import reactor.core.publisher.Mono;

public class EchoAction extends VoidAction {

	public EchoAction(String message, Optional<User> author, Mono<Guild> guild, Mono<MessageChannel> channel) {
		super(() -> {
			String guildName = ":";
			String channelName = channel.block().getId().toString();
			switch( channel.block().getType() ) {
				case DM:
					channelName = ":";
					break;
				case GUILD_TEXT:
					guildName = guild.block().getName();
					channelName = channel.cast(TextChannel.class).block().getName();
					break;
				default:
					guildName = guild.block().getName();
					break;
			}
			AliceLogger.echo(message, String.format("%s#%s", author.get().getUsername(), author.get().getDiscriminator()), guildName, channelName);
		});
	}

}
