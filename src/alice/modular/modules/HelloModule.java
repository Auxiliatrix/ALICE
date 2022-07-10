package alice.modular.modules;

import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
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
		
		Command<MessageCreateEvent> c1 = command.addSubcommand();
		c1.withCondition(mce -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(mce.getMessage().getGuildId().get().asLong());
			return !ssf.has("test"); // This must check for a variable referenced in the other method as well.
		});
		c1.withSideEffect(mce -> {}); // The presence of this line causes an error in the next execution.
		
		Command<MessageCreateEvent> c2 = command.addSubcommand();
		c2.withDependentEffect(d -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getMessage().getGuildId().get().asLong());
			if( !ssf.has("test") ) {
				ssf.putJSONObject("test");
			}
			SyncedJSONObject test = ssf.getJSONObject("test");
			if( !test.has("tester") ) {
				test.put("tester", 0);
			}
			return ef.requestFrom(d).createMessage(test.getInt("tester")+"");
		});
		
		return command;
	}	
	
}
