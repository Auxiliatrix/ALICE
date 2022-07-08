package alice.modular.modules;

import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.modules.MessageModule;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class EngagementModule extends MessageModule {

	public EngagementModule() {
		super();
	}
	
	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdf = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		
		Command<MessageCreateEvent> invokedCommand = new Command<MessageCreateEvent>(df);
		invokedCommand.withCondition(MessageModule.getInvokedCondition("%engagement"));

		Command<MessageCreateEvent> setupCommand = new Command<MessageCreateEvent>(df);
		setupCommand.withCondition(MessageModule.getArgumentCondition(1, "setup"));
		setupCommand.withDependentEffect(d -> {
			MessageChannel mc = mcdf.requestFrom(d);
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
			
			return Mono.fromRunnable(() -> {
				if( !ssf.has("%engagement_daily_messages") ) {
					ssf.putJSONObject("%engagement_daily_messages");
				}
				if( !ssf.has("%engagement_daily_firsts") ) {
					ssf.putJSONObject("engagement_daily_firsts");
				}
				if( !ssf.has("%engagement_daily_uniques") ) {
					ssf.putJSONObject("engagement_daily_uniques");
				}
			}).and(mc.createMessage("Setup completed successfully! Now tracking engagement metrics for this server."));
		});
		
		Command<MessageCreateEvent> usageCommand = new Command<MessageCreateEvent>(df);
		usageCommand.withCondition(mce -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(mce.getGuildId().get().asLong());
			return ssf.has("%engagement_daily_messages") // KEY: YYYY-MM-DD, VALUE: # OF MESSAGES
					&& ssf.has("%engagement_daily_firsts") // KEY: YYYY-MM-DD, VALUE: # OF FIRST SENDS
					&& ssf.has("%engagement_daily_firsts"); // KEY: YYYY-MM-DD, VALUE: # OF UNIQUES
		});
		
		
		
		invokedCommand.withSubcommand(setupCommand);
		invokedCommand.withSubcommand(usageCommand);
		
		Command<MessageCreateEvent> passiveCommand = new Command<MessageCreateEvent>(df);
		passiveCommand.withCondition(mce -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(mce.getGuildId().get().asLong());
			return ssf.has("%engagement_daily_messages") // KEY: YYYY-MM-DD, VALUE: # OF MESSAGES
					&& ssf.has("%engagement_daily_firsts") // KEY: YYYY-MM-DD, VALUE: # OF FIRST SENDS
					&& ssf.has("%engagement_daily_firsts"); // KEY: YYYY-MM-DD, VALUE: # OF UNIQUES
		});
		
		command.withSubcommand(invokedCommand);
		command.withSubcommand(passiveCommand);
		
		return command;
	}

}
