package alice.modular.modules;

import alice.framework.modules.commands.Command;
import alice.framework.modules.commands.Module;
import alice.framework.modules.tasks.DependencyFactory;
import alice.framework.modules.tasks.EffectFactory;
import alice.framework.modules.tasks.MessageSendEffectSpec;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class HelloModule extends Module<MessageCreateEvent> {

	public HelloModule() {
		super(MessageCreateEvent.class);
	}
	
	public Command<MessageCreateEvent> buildCommand() {
		DependencyFactory.Builder<MessageCreateEvent> dfb = DependencyFactory.<MessageCreateEvent>builder();
		dfb.addDependency(mce -> mce.getGuild());
		EffectFactory<MessageCreateEvent, MessageChannel> ef = dfb.<MessageChannel>addDependency(mce -> mce.getMessage().getChannel());
		DependencyFactory<MessageCreateEvent> df = dfb.buildDependencyFactory();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		
		command.withCondition(mce -> mce.getMessage().getContent().startsWith("%hello"));
		command.withDependentEffect(ef.getEffect(mc -> mc.createMessage("Hello, world!")));
		command.withDependentEffect(ef.getEffect(new MessageSendEffectSpec("Hello world!")));
		command.withEffect(mce -> {System.out.println("Hello world!");});
		
		return command;
	}	
	
}
