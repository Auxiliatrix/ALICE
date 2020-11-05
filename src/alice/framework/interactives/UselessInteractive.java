package alice.framework.interactives;

import alice.framework.main.Brain;
import alice.modular.actions.ReactionRemoveAction;
import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

public class UselessInteractive extends Interactive {

	protected int count;
	
	public UselessInteractive(Snowflake channelId, Snowflake messageId) {
		super(channelId, messageId, ReactionEmoji.unicode("🔼"));
		count = 0;
	}
	
	public UselessInteractive(Snowflake channelId, Snowflake messageId, int count) {
		super(channelId, messageId);
		this.count = count;
	}

	@Override
	public Mono<?> onAdd(ReactionEmoji reaction, Snowflake userId) {
		if( userId.equals(Brain.client.getSelfId()) ) {
			return Mono.fromRunnable(()->{});
		}
		count++;
		return Brain.client.getMessageById(channelId, messageId).block().edit(spec -> spec.setContent((String.format("Count: %d", count))))
				.and(new ReactionRemoveAction(Brain.client.getMessageById(channelId, messageId), reaction, userId).toMono());
	}

	@Override
	public Mono<?> onRemove(ReactionEmoji reaction, Snowflake userId) {
		return Mono.fromRunnable(()->{});
	}
	
	public int getCount() {
		return count;
	}

}
