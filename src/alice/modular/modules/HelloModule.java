package alice.modular.modules;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyManager;
import alice.framework.effects.MessageSendEffectSpec;
import alice.framework.modules.MessageModule;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class HelloModule extends MessageModule {

	public HelloModule() {
		super();
	}
	
	public Command<MessageCreateEvent> buildCommand(DependencyFactory.Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> ef = dfb.<MessageChannel>addDependency(mce -> mce.getMessage().getChannel());
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getInvokedCondition("%hello"));
		command.withDependentEffect(ef.buildEffect(new MessageSendEffectSpec("Hello world!")));
		command.withSideEffect(mce -> {System.out.println("Hello world!");});
		
		return command;
	}	
	
}
