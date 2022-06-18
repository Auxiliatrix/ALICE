package alice.framework.modules.commands;

import alice.framework.modules.tasks.DependencyFactory;
import alice.framework.modules.tasks.EffectFactory;
import alice.framework.modules.tasks.MessageSendEffectSpec;
import alice.framework.modules.tasks.Task;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class Command<E extends Event> {

	private String description;
	
	protected static final class Builder<E2 extends Event> {
		private String desription;
		
		private Builder() {}
	
		public Builder<E2> description(String description) {
			this.desription = description;
			return this;
		}
		
		public Command<E2> build() {
			return new Command<E2>(this);
		}
	}
	
	protected Command(Builder<E> builder) {
		this.description = builder.desription;
		DependencyFactory.Builder<MessageCreateEvent> dfb = DependencyFactory.<MessageCreateEvent>builder();
		dfb.addDependency(mce -> mce.getGuild());
		EffectFactory<MessageCreateEvent, MessageChannel> ef = dfb.<MessageChannel>addDependency(mce -> mce.getMessage().getChannel());
		
		Task<MessageCreateEvent> task = new Task<MessageCreateEvent>(dfb.buildDependencyFactory());
		
		task.addEffect(ef.getEffect(mc -> mc.createMessage("Hello, world!")));
		task.addEffect(ef.getEffect(new MessageSendEffectSpec("Hello world!")));
	}
	
	public static <E2 extends Event> Builder<E2> builder() {
		return new Builder<E2>();
	}
	
	public String getDescription() {
		return description;
	}
	
}
