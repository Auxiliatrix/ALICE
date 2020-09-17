package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildChannel;
import reactor.core.publisher.Mono;

public class ChannelDeleteAction extends Action {
	
	public ChannelDeleteAction(Mono<Guild> guild, Snowflake id) {
		super();
		GuildChannel channel = guild.block().getChannelById(id).block();
		if( channel != null ) {
			this.mono = channel.delete();
		} else {
			this.mono = Mono.fromRunnable(() -> {});
		}
	}
	
}
