package alice.framework.modules.commands;

import alice.framework.modules.tasks.DependencyFactory;
import alice.framework.modules.tasks.DependencyFactoryBuilder;
import alice.framework.modules.tasks.Task;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class Command {

	private String description;
	
	protected static final class Builder {
		private String desription;
		
		private Builder() {}
	
		public Builder description(String description) {
			this.desription = description;
			return this;
		}
		
		public Command build() {
			return new Command(this);
		}
	}
	
	protected Command(Builder builder) {
		this.description = builder.desription;
		DependencyFactoryBuilder<MessageCreateEvent> dfb = new DependencyFactoryBuilder<MessageCreateEvent>();
		dfb.addDependency(mce -> mce.getGuild());
		dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		Task<MessageCreateEvent> task = new Task<MessageCreateEvent>(df);
		task.addEffect(d -> {
			return d.<MessageChannel>request(
					(mce -> mce.getMessage().getChannel())
				).createMessage("Hello, world!");
		});
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public String getDescription() {
		return description;
	}
	
}
