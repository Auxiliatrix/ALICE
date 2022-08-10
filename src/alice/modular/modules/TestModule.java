package alice.modular.modules;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.modules.MessageModule;
import alice.framework.utilities.SaveFiles;
import alina.structures.SyncedJSONObject;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class TestModule extends MessageModule {
	
	public Command<MessageCreateEvent> buildCommand(DependencyFactory.Builder<MessageCreateEvent> dfb) {
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getInvokedCondition("test"));
		
		Command<MessageCreateEvent> execOrder = command.addSubcommand();
		command.withCondition(MessageModule.getArgumentCondition(1, "exor"));
		
		Command<MessageCreateEvent> c1 = execOrder.addSubcommand();
		c1.withCondition(
			mce -> {
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
				return !ssf.has("tester");
			}
		);
		c1.withSideEffect(
			mce -> {
				System.out.println("..");
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
				ssf.putJSONObject("tester");
			}
		);
		c1.withDependentSideEffect(d -> {
			System.out.println("....");
			SyncedJSONObject ssf = SaveFiles.ofGuild(d.getEvent().getGuildId().get().asLong());
			SyncedJSONObject tester = ssf.getJSONObject("tester");
			tester.put("test1", "......");
		});
		
		Command<MessageCreateEvent> c2 = execOrder.addSubcommand();
		c2.withEffect(mce -> {
			SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
			SyncedJSONObject tester = ssf.getJSONObject("tester");
			System.out.println(tester.get("test1"));
			return Mono.fromRunnable(() -> {
				System.out.println("........");
				tester.put("test2", "..........");
			});
		});
		c2.withDependentEffect(d -> {
			SyncedJSONObject ssf = SaveFiles.ofGuild(d.getEvent().getGuildId().get().asLong());
			SyncedJSONObject tester = ssf.getJSONObject("tester");
			System.out.println(tester.get("test2"));
			return Mono.fromRunnable(() -> {
				System.out.println("............");
				ssf.remove("tester");
			});
		});
		
		return command;
	}	
	
}
