package alice.modular.modules;

import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.modules.MessageModule;
import alice.framework.utilities.EmbedBuilders;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public class ReputationModule extends MessageModule {

	public ReputationModule() {
		super();
	}

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdm = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent, PermissionSet> psdm = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		DependencyFactory<MessageCreateEvent> df = dfb.build();
				
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getHumanCondition());
		command.withCondition(MessageModule.getInvokedCondition("%rep"));
		command.withDependentEffect(d -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
			if( !ssf.has("%rep_map") ) {
				ssf.putJSONObject("%rep_map");
			}
			if( !ssf.has("%rep_last") ) {
				ssf.putJSONObject("%rep_last");
			}
			SyncedJSONObject rep_map = ssf.getJSONObject("%rep_map");
			MessageChannel mc = mcdm.requestFrom(d);
			User targetUser = d.getEvent().getMessage().getAuthor().get();
			Snowflake s = d.getEvent().getMessage().getAuthor().get().getId();
			if( !rep_map.has(s.asString()) ) {
				rep_map.put(s.asString(), 0);
			}
			
			return mc.createMessage(
					EmbedCreateSpec.builder()
						.description(String.format("This user has :scroll:%s reputation!", rep_map.get(s.asString())))
						.color(Color.YELLOW)
						.author(targetUser.getUsername(), null, targetUser.getAvatarUrl())
						.build());
		});
		
		Command<MessageCreateEvent> arg = command.addSubcommand();
		arg.withCondition(MessageModule.getArgumentsCondition(2));
		
//		Command<MessageCreateEvent> leadCommand = arg.addSubcommand();
				
		Command<MessageCreateEvent> repCommand = arg.addSubcommand();
		repCommand.withCondition(MessageModule.getMentionsCondition(1));
		repCommand.withDependentEffect(d -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
			if( !ssf.has("%rep_map") ) {
				ssf.putJSONObject("%rep_map");
			}
			if( !ssf.has("%rep_last") ) {
				ssf.putJSONObject("%rep_last");
			}
			SyncedJSONObject rep_map = ssf.getJSONObject("%rep_map");
			SyncedJSONObject rep_last = ssf.getJSONObject("%rep_last");
			boolean admin = psdm.requestFrom(d).contains(Permission.ADMINISTRATOR);
			MessageChannel mc = mcdm.requestFrom(d);
			Snowflake s = d.getEvent().getMessage().getAuthor().get().getId();
			User targetUser = d.getEvent().getMessage().getUserMentions().get(0);
			Snowflake target = d.getEvent().getMessage().getUserMentionIds().get(0);
			
			if( !rep_map.has(target.asString()) ) {
				rep_map.put(target.asString(), 0);
			}
			if( !rep_map.has(s.asString()) ) {
				rep_map.put(s.asString(), 0);
			}
			if( s.equals(target) && !admin ) {
				return mc.createMessage(EmbedBuilders.applyErrorFormat("You cannot give reputation to yourself!", EmbedBuilders.ERR_PERMISSION));
			} else {
				if( rep_last.has(s.asString()) ) {
					long last = rep_last.getLong(s.asString());
					long current = System.currentTimeMillis();
					long dif = current - last;
					if( dif < 14400000 && !admin ) {
						if( (14400000-dif) > 60000 ) {
							return mc.createMessage(
									EmbedCreateSpec.builder()
										.description(String.format("This user has :scroll:%s reputation!", rep_map.get(target.asString())))
										.color(Color.YELLOW)
										.author(targetUser.getUsername(), null, targetUser.getAvatarUrl())
										.footer(String.format("You can rep someone again in %d minute(s)!", (14400000-dif) / 60000), null)
										.build());
						} else {
							return mc.createMessage(
									EmbedCreateSpec.builder()
										.description(String.format("This user has :scroll:%s reputation!", rep_map.get(target.asString())))
										.color(Color.YELLOW)
										.author(targetUser.getUsername(), null, targetUser.getAvatarUrl())
										.footer(String.format("You can rep someone again in %d second(s)!", (14400000-dif) / 1000), null)
										.build());
						}
					}
				}
				return Mono.fromRunnable(() -> {
					rep_map.increment(target.asString());
					rep_map.increment(s.asString());
					
					rep_last.put(s.asString(), System.currentTimeMillis());
				}).then(mc.createMessage(EmbedCreateSpec.builder().description(String.format("This user now has :scroll:%s reputation!", rep_map.getInt(target.asString())+1)).color(Color.YELLOW).author(targetUser.getUsername(), null, targetUser.getAvatarUrl()).build()));
			}
		});
		
		return command;
	}
	
}
