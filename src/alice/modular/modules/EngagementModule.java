package alice.modular.modules;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.main.Constants;
import alice.framework.modules.MessageModule;
import alice.framework.utilities.EmbedBuilders;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public class EngagementModule extends MessageModule {
	
	public EngagementModule() {
		super();
	}
	
	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdf = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent, PermissionSet> psdf = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getHumanCondition());
		
		Command<MessageCreateEvent> invokedCommand = new Command<MessageCreateEvent>(df);
		invokedCommand.withCondition(MessageModule.getInvokedCondition("%engagement"));
		invokedCommand.withDependentCondition(MessageModule.getPermissionCondition(psdf, Permission.ADMINISTRATOR));
		
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
					ssf.putJSONObject("%engagement_daily_firsts");
				}
				if( !ssf.has("%engagement_daily_uniques") ) {
					ssf.putJSONObject("%engagement_daily_uniques");
				}
				if( !ssf.has("%engagement_lasts") ) {
					ssf.putJSONObject("%engagement_lasts");
				}
			}).and(mc.createMessage(EmbedBuilders.applySuccessFormat("Setup completed successfully! Now tracking engagement metrics for this server.")));
		});
		
		@SuppressWarnings("unchecked")
		Command<MessageCreateEvent> usageCommand = new Command<MessageCreateEvent>(df,
			new Command<MessageCreateEvent>(df)
				.withCondition(MessageModule.getArgumentCondition(1, "messages"))
				.withDependentEffect(d -> {
					SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
					SyncedJSONObject daily_messages = ssf.getJSONObject("%engagement_daily_messages");
					MessageChannel mc = mcdf.requestFrom(d);
					String key = MessageModule.tokenizeMessage(d.getEvent()).getString(2);
					return mc.createMessage(daily_messages.has(key) ? EmbedBuilders.applySuccessFormat(daily_messages.get(key)+" messages sent!") : EmbedBuilders.applyErrorFormat("No data collected for the given date!\n*Date Format: YYYY-MM-DD*"));
				}),
			new Command<MessageCreateEvent>(df)
				.withCondition(MessageModule.getArgumentCondition(1, "firsts"))
				.withDependentEffect(d -> {
					SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
					SyncedJSONObject daily_firsts = ssf.getJSONObject("%engagement_daily_firsts");
					MessageChannel mc = mcdf.requestFrom(d);
					String key = MessageModule.tokenizeMessage(d.getEvent()).getString(2);
					return mc.createMessage(daily_firsts.has(key) ? EmbedBuilders.applySuccessFormat(daily_firsts.get(key)+" first messages!") : EmbedBuilders.applyErrorFormat("No data collected for the given date!\n*Date Format: YYYY-MM-DD*"));
				}),
			new Command<MessageCreateEvent>(df)
				.withCondition(MessageModule.getArgumentCondition(1, "uniques"))
				.withDependentEffect(d -> {
					SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
					SyncedJSONObject daily_uniques= ssf.getJSONObject("%engagement_daily_uniques");
					MessageChannel mc = mcdf.requestFrom(d);
					String key = MessageModule.tokenizeMessage(d.getEvent()).getString(2);
					return mc.createMessage(daily_uniques.has(key) ? EmbedBuilders.applySuccessFormat(daily_uniques.get(key)+" unique users engaged!") : EmbedBuilders.applyErrorFormat("No data collected for the given date!\n*Date Format: YYYY-MM-DD*"));
				}),
			new Command<MessageCreateEvent>(df)
				.withCondition(MessageModule.getArgumentCondition(1, "report"))
				.withDependentEffect(d -> {
					MessageChannel mc = mcdf.requestFrom(d);

					String key = MessageModule.tokenizeMessage(d.getEvent()).getString(2);
					SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
					SyncedJSONObject daily_messages = ssf.getJSONObject("%engagement_daily_messages");
					SyncedJSONObject daily_firsts = ssf.getJSONObject("%engagement_daily_firsts");
					SyncedJSONObject daily_uniques = ssf.getJSONObject("%engagement_daily_uniques");
					if( daily_messages.has(key) && daily_firsts.has(key) && daily_uniques.has(key) ) {
						int dms = daily_messages.getInt(key);
						int dfs = daily_firsts.getInt(key);
						int dus = daily_uniques.getInt(key);
						
//						List<SimpleEntry<String,String>> entries = new ArrayList<SimpleEntry<String,String>>();
//						entries.add(new SimpleEntry<String,String>(String.format("Messages Sent: %d",dms),""));
//						entries.add(new SimpleEntry<String,String>(String.format("Active Users: %d",dfs),""));
//						entries.add(new SimpleEntry<String,String>(String.format("Unique Users: %d",dus),""));
//						entries.add(new SimpleEntry<String,String>(String.format("Messages/User: %.2f",(double)dms/dus),""));
//						return mc.createMessage(EmbedBuilders.applyListFormat(String.format("Engagement Report for %s", key), Color.GREEN, entries, false, false));
						String report = String.format("**Messages Sent**: %d\n**Active Users**: %d\n**Unique Users**: %d\n**Messages/User**: %.2f", dms,dfs,dus,(double)dms/dus);
						return mc.createMessage(EmbedCreateSpec.builder().description(report).color(Color.GREEN).title(String.format("Engagement Report [%s]", key)).build());
					} else {
						return mc.createMessage(EmbedBuilders.applyErrorFormat("No data collected for the given date!\n*Date Format: YYYY-MM-DD*"));
					}
				})
		);
		usageCommand.withCondition(MessageModule.getArgumentsCondition(3));
		usageCommand.withCondition(mce -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(mce.getGuildId().get().asLong());
			return ssf.has("%engagement_daily_messages") // KEY: YYYY-MM-DD, VALUE: # OF MESSAGES
					&& ssf.has("%engagement_daily_firsts") // KEY: YYYY-MM-DD, VALUE: # OF FIRST SENDS
					&& ssf.has("%engagement_daily_uniques")
					&& ssf.has("%engagement_lasts"); // KEY: YYYY-MM-DD, VALUE: # OF UNIQUES
		});
		
		invokedCommand.withSubcommand(setupCommand);
		invokedCommand.withSubcommand(usageCommand);
		
		Command<MessageCreateEvent> passiveCommand = new Command<MessageCreateEvent>(df);
		passiveCommand.withCondition(mce -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(mce.getGuildId().get().asLong());
			return ssf.has("%engagement_daily_messages") // KEY: YYYY-MM-DD, VALUE: # OF MESSAGES
					&& ssf.has("%engagement_daily_firsts") // KEY: YYYY-MM-DD, VALUE: # OF FIRST SENDS
					&& ssf.has("%engagement_daily_uniques")
					&& ssf.has("%engagement_lasts"); // KEY: YYYY-MM-DD, VALUE: # OF UNIQUES
		});
		passiveCommand.withDependentCondition(MessageModule.getPermissionCondition(psdf, Permission.ADMINISTRATOR).andThen(b -> !b));
		passiveCommand.withDependentSideEffect(d -> {
			Member m = d.getEvent().getMember().get();
			String ID = m.getId().asString();
			LocalDateTime ldt = LocalDateTime.now(ZoneId.ofOffset("GMT", ZoneOffset.ofHours(-7)));
			String dateString = Constants.SDF.format(Date.valueOf(ldt.toLocalDate()));
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
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
		});
		
		command.withSubcommand(invokedCommand);
		command.withSubcommand(passiveCommand);
		
		return command;
	}

}
