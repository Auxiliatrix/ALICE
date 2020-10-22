package alice.modular.actions;

import java.util.function.Predicate;

import alice.framework.actions.Action;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import reactor.core.publisher.Mono;

public class MessageDeleteFilteredAction extends Action {
	
	public MessageDeleteFilteredAction(Mono<GuildMessageChannel> channel, Snowflake end, Predicate<? super Message> filter) {
		super(channel.block().bulkDeleteMessages(channel.block().getMessagesBefore(end).filter(filter)).collectList());
	}
	
	public MessageDeleteFilteredAction(Mono<GuildMessageChannel> channel, Snowflake end, int quantity, Predicate<? super Message> filter) {
		super(channel.block().bulkDeleteMessages(channel.block().getMessagesBefore(end).filter(filter).take(quantity)).collectList());
	}
	
}
