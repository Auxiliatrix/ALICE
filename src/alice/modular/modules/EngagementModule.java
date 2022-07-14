package alice.modular.modules;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.main.Constants;
import alice.framework.modules.MessageModule;
import alice.framework.utilities.EmbedFactory;
import alice.framework.utilities.SaveFiles;
import alina.structures.SyncedJSONObject;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public class EngagementModule extends MessageModule {
	
	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdm = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent, PermissionSet> psdm = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getHumanCondition());
		
		Command<MessageCreateEvent> invokedCommand = command.addSubcommand();
		invokedCommand.withCondition(MessageModule.getInvokedCondition("%engagement"));
		invokedCommand.withDependentCondition(MessageModule.getPermissionCondition(psdm, Permission.ADMINISTRATOR));
		
		Command<MessageCreateEvent> setupCommand = invokedCommand.addSubcommand();
		setupCommand.withCondition(MessageModule.getArgumentCondition(1, "setup"));
		setupCommand.withDependentEffect(mcdm.buildEffect(
			(mce, mc) -> {
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get());
				
				return Mono.fromRunnable(() -> {
					if( !ssf.has("%engagement_daily_messages") ) {
						ssf.putJSONObject("%engagement_daily_messages");
					}
					if( !ssf.has("%engagement_daily_firsts") ) {
						ssf.putJSONObject("%engagement_daily_firsts");
					}
					if( !ssf.has("%engagement_daily_uniques") ) {
						ssf.putJSONObject("%engagement_daily_uniques");
					}
					if( !ssf.has("%engagement_lasts") ) {
						ssf.putJSONObject("%engagement_lasts");
					}
				}).and(mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Setup completed successfully! Now tracking engagement metrics for this server."))));
			}
		));
		
		@SuppressWarnings("unchecked")
		Command<MessageCreateEvent> usageCommand = new Command<MessageCreateEvent>(df,
			new Command<MessageCreateEvent>(df)
				.withCondition(MessageModule.getArgumentCondition(1, "messages"))
				.withDependentEffect(mcdm.buildEffect(
					(mce, mc) -> {
						SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get());
						SyncedJSONObject daily_messages = ssf.getJSONObject("%engagement_daily_messages");
						String key = MessageModule.tokenizeMessage(mce).getString(2);
						return mc.createMessage(daily_messages.has(key) ? EmbedFactory.build(EmbedFactory.modSuccessFormat(daily_messages.get(key)+" messages sent!")) : EmbedFactory.build(EmbedFactory.modErrorFormat("No data collected for the given date!\n*Date Format: YYYY-MM-DD*")));
					}
				)),
			new Command<MessageCreateEvent>(df)
				.withCondition(MessageModule.getArgumentCondition(1, "firsts"))
				.withDependentEffect(mcdm.buildEffect(
					(mce, mc) -> {
						SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get());
						SyncedJSONObject daily_firsts = ssf.getJSONObject("%engagement_daily_firsts");
						String key = MessageModule.tokenizeMessage(mce).getString(2);
						return mc.createMessage(daily_firsts.has(key) ? EmbedFactory.build(EmbedFactory.modSuccessFormat(daily_firsts.get(key)+" first messages!")) : EmbedFactory.build(EmbedFactory.modErrorFormat("No data collected for the given date!\n*Date Format: YYYY-MM-DD*")));
					}
				)),
			new Command<MessageCreateEvent>(df)
				.withCondition(MessageModule.getArgumentCondition(1, "uniques"))
				.withDependentEffect(mcdm.buildEffect(
					(mce, mc) -> {
						SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get());
						SyncedJSONObject daily_uniques= ssf.getJSONObject("%engagement_daily_uniques");
						String key = MessageModule.tokenizeMessage(mce).getString(2);
						return mc.createMessage(daily_uniques.has(key) ? EmbedFactory.build(EmbedFactory.modSuccessFormat(daily_uniques.get(key)+" unique users engaged!")) : EmbedFactory.build(EmbedFactory.modErrorFormat("No data collected for the given date!\n*Date Format: YYYY-MM-DD*")));
					}
				)),
			new Command<MessageCreateEvent>(df)
				.withCondition(MessageModule.getArgumentCondition(1, "report"))
				.withDependentEffect(mcdm.buildEffect(
					(mce, mc) -> {
						String key = MessageModule.tokenizeMessage(mce).getString(2);
						SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
						SyncedJSONObject daily_messages = ssf.getJSONObject("%engagement_daily_messages");
						SyncedJSONObject daily_firsts = ssf.getJSONObject("%engagement_daily_firsts");
						SyncedJSONObject daily_uniques = ssf.getJSONObject("%engagement_daily_uniques");
						if( daily_messages.has(key) && daily_firsts.has(key) && daily_uniques.has(key) ) {
							int dms = daily_messages.getInt(key);
							int dfs = daily_firsts.getInt(key);
							int dus = daily_uniques.getInt(key);
							
							String report = String.format("**Messages Sent**: %d\n**Active Users**: %d\n**Unique Users**: %d\n**Messages/User**: %.2f", dms,dfs,dus,(double)dms/dus);
							return mc.createMessage(EmbedCreateSpec.builder().description(report).color(Color.GREEN).title(String.format("Engagement Report [%s]", key)).build());
						} else {
							return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("No data collected for the given date!\n*Date Format: YYYY-MM-DD*")));
						}
					}))
		);
		usageCommand.withCondition(MessageModule.getArgumentsCondition(3));
		usageCommand.withCondition(
			mce -> {
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get());
				return ssf.has("%engagement_daily_messages") // KEY: YYYY-MM-DD, VALUE: # OF MESSAGES
						&& ssf.has("%engagement_daily_firsts") // KEY: YYYY-MM-DD, VALUE: # OF FIRST SENDS
						&& ssf.has("%engagement_daily_uniques")
						&& ssf.has("%engagement_lasts"); // KEY: YYYY-MM-DD, VALUE: # OF UNIQUES
			}
		);
		invokedCommand.withSubcommand(usageCommand);
		
		Command<MessageCreateEvent> passiveCommand = command.addSubcommand();
		passiveCommand.withCondition(
			mce -> {
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get());
				return ssf.has("%engagement_daily_messages") // KEY: YYYY-MM-DD, VALUE: # OF MESSAGES
						&& ssf.has("%engagement_daily_firsts") // KEY: YYYY-MM-DD, VALUE: # OF FIRST SENDS
						&& ssf.has("%engagement_daily_uniques")
						&& ssf.has("%engagement_lasts"); // KEY: YYYY-MM-DD, VALUE: # OF UNIQUES
			}
		);
		passiveCommand.withDependentCondition(MessageModule.getPermissionCondition(psdm, Permission.ADMINISTRATOR).andThen(b -> !b));
		passiveCommand.withSideEffect(
			mce -> {
				Member m = mce.getMember().get();
				String ID = m.getId().asString();
				LocalDateTime ldt = LocalDateTime.now(ZoneId.ofOffset("GMT", ZoneOffset.ofHours(-7)));
				String dateString = Constants.SDF.format(Date.valueOf(ldt.toLocalDate()));
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
				SyncedJSONObject daily_messages = ssf.getJSONObject("%engagement_daily_messages");
				SyncedJSONObject daily_firsts = ssf.getJSONObject("%engagement_daily_firsts");
				SyncedJSONObject daily_uniques= ssf.getJSONObject("%engagement_daily_uniques");
				SyncedJSONObject lasts = ssf.getJSONObject("%engagement_lasts");
				
				if( !daily_messages.has(dateString) ) {
					daily_messages.put(dateString, 0);
				}
				if( !daily_firsts.has(dateString) ) {
					daily_firsts.put(dateString, 0);
				}
				if( !daily_uniques.has(dateString) ) {
					daily_uniques.put(dateString, 0);
				}
				
				daily_messages.put(dateString, daily_messages.getInt(dateString)+1);
	
				if( !lasts.has(ID) ) {
					daily_firsts.put(dateString, daily_firsts.getInt(dateString)+1);
					daily_uniques.put(dateString, daily_uniques.getInt(dateString)+1);
				} else {
					if( !lasts.getString(ID).equals(dateString) ) {
						daily_uniques.put(dateString, daily_uniques.getInt(dateString)+1);
					}
				}
				
				lasts.put(ID, dateString);
			}
		);
		
		return command;
	}

}
