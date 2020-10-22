package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import reactor.core.publisher.Mono;

public class MessageDeleteBulkAction extends Action {
	
	public MessageDeleteBulkAction(Mono<GuildMessageChannel> channel, String start) {
		super(channel.block().bulkDeleteMessages(channel.block().getMessagesAfter(Snowflake.of(start))).collectList());
	}
	
}
