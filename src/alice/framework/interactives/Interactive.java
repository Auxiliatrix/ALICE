package alice.framework.interactives;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.interactives.builders.InteractiveBuilder;
import alice.framework.main.Brain;
import alice.modular.actions.ReactionAddAction;
import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

public abstract class Interactive {
	
	public final Snowflake channelId;
	public final Snowflake messageId;
	protected final ReactionEmoji[] options;
	
	public Interactive(Snowflake channelId, Snowflake messageId, ReactionEmoji...options) {
		this.channelId = channelId;
		this.messageId = messageId;
		this.options = options;
		initialize();
	}
	
	public final Mono<?> initialize() {
		Action initialization = new NullAction();
		System.out.println("Initializing");
		for( ReactionEmoji option : options ) {
			System.out.println("Adding reaction");
			initialization.addAction(new ReactionAddAction(Brain.client.getMessageById(channelId, messageId), option));
		}
		
		if( !Brain.channelIndex.containsKey(channelId.asString()) ) {
			System.out.println("Error: No channel index");
			return initialization.toMono();
		}
		
		if( !Brain.channelIndex.get(channelId.asString()).has(messageId.asString()) ) {
			System.out.println("Message Id free");
			for( InteractiveBuilder<?> i : Brain.interactiveTypes.get() ) {
				if( i.getBuilderType().equals(this.getClass().getName()) ) {
					System.out.println("Constructing json");
					Brain.channelIndex.get(channelId.asString()).put(messageId.asString(), i.toJSONObject(this));
				}
			}
		}
		
		System.out.println("Returning");
		return initialization.toMono();
	}
	
	public final void destroy() {
		Brain.interactives.remove(String.format("%s_%s", channelId.asString(), messageId.asString()));
		
		if( !Brain.channelIndex.containsKey(channelId.asString()) ) {
			return;
		}		
		Brain.channelIndex.get(channelId.asString()).remove(messageId.asString());
	}
	
	public abstract Mono<?> onAdd(ReactionEmoji reaction, Snowflake userId);
	public abstract Mono<?> onRemove(ReactionEmoji reaction, Snowflake userId);
	
}
