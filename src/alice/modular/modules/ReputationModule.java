package alice.modular.modules;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.modules.MessageModule;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class ReputationModule extends MessageModule {

	public ReputationModule() {
		super();
	}

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdm = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		// TODO: subcommands should be passed by reference and be same format as addDependency (return manager)
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getHumanCondition());
		command.withCondition(MessageModule.getInvokedCondition("%rep"));
		
		
		return command;
	}
	
}
