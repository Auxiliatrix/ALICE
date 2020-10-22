package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;

public class ChannelDeleteAction extends Action {
	
	public ChannelDeleteAction(Mono<Guild> guild, Snowflake id) {
		super(
				guild.block().getChannelById(id).block() != null ?
						guild.block().getChannelById(id).block().delete() :
							Mono.fromRunnable( () -> {} )
			);
	}
	
}
