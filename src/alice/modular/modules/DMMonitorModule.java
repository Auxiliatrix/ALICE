package alice.modular.modules;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.main.Brain;
import alice.framework.modules.MessageModule;
import alice.framework.utilities.EmbedFactory;
import alice.framework.utilities.SaveFiles;
import alina.structures.SyncedJSONArray;
import alina.structures.SyncedJSONObject;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public class DMMonitorModule extends MessageModule {

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		
		Command<MessageCreateEvent> report = command.addSubcommand();
		report.withCondition(MessageModule.getDMCondition());
		report.withCondition(mce -> {
			SyncedJSONObject ssf = SaveFiles.of("tmp", "global");
			if( !ssf.has("%dmm_channels") ) {
				ssf.putJSONArray("%dmm_channels");
			}
			return ssf.getJSONArray("%dmm_channels").length() > 0;
		});
		report.withEffect(mce -> {
			SyncedJSONObject ssf = SaveFiles.of("tmp", "global");
			Mono<?> ret = Mono.fromRunnable(() -> {});
			SyncedJSONArray channels = ssf.getJSONArray("%dmm_channels");
			for( int f=0; f<channels.length(); f++ ) {
				String channel = channels.getString(f);
				Mono<Channel> mcm = Brain.gateway.getChannelById(Snowflake.of(channel));
				ret = ret.and(mcm.flatMap(mc -> ((MessageChannel) mc).createMessage(String.format("%s#%s: %s", mce.getMessage().getAuthor().get().getUsername(), mce.getMessage().getAuthor().get().getDiscriminator(), mce.getMessage().getContent()))));
			}
			return ret;
		});
		
		// TODO: give commands withSetup
		
		DependencyManager<MessageCreateEvent, PermissionSet> psdm = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		DependencyManager<MessageCreateEvent, MessageChannel> mcdm = dfb.addDependency(mce -> mce.getMessage().getChannel());
		
		Command<MessageCreateEvent> setup = command.addSubcommand(dfb.build());
		setup.withCondition(MessageModule.getGuildCondition());
		setup.withDependentCondition(MessageModule.getPermissionCondition(psdm, Permission.ADMINISTRATOR));
		setup.withCondition(MessageModule.getArgumentsCondition(2));
		setup.withCondition(MessageModule.getInvokedCondition("dmm"));

		Command<MessageCreateEvent> track = setup.addSubcommand();
		track.withCondition(MessageModule.getArgumentCondition(1, "track"));
		track.withDependentEffect(mcdm.buildEffect(
			(mce,mc) -> {
				SyncedJSONObject ssf = SaveFiles.of("tmp", "global");
				if( !ssf.has("%dmm_channels") ) {
					ssf.putJSONArray("%dmm_channels");
				}
				SyncedJSONArray channels = ssf.getJSONArray("%dmm_channels");
				boolean contains = false;
				for( int f=0; f<channels.length(); f++ ) {
					String channel = channels.getString(f);
					if( channel.equals(mc.getId().asString()) ) {
						contains = true;
						break;
					}
				}
				if( !contains ) {
					return Mono.fromRunnable(() -> {
						channels.put(mc.getId().asString());
					}).and(mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Tracking set up successfully."))));
				} else {
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Tracking already set up for this channel.")));
				}
			}));
		
		Command<MessageCreateEvent> untrack = setup.addSubcommand();
		untrack.withCondition(MessageModule.getArgumentCondition(1, "untrack"));
		untrack.withDependentEffect(mcdm.buildEffect(
				(mce,mc) -> {
					SyncedJSONObject ssf = SaveFiles.of("tmp", "global");
					if( !ssf.has("%dmm_channels") ) {
						ssf.putJSONArray("%dmm_channels");
					}
					SyncedJSONArray channels = ssf.getJSONArray("%dmm_channels");
					int index = -1;
					for( int f=0; f<channels.length(); f++ ) {
						String channel = channels.getString(f);
						if( channel.equals(mc.getId().asString()) ) {
							index = f;
							break;
						}
					}
					if( index != -1 ) {
						final int i = index;
						return Mono.fromRunnable(() -> {
							channels.remove(i);
						}).and(mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Tracking removed successfully."))));
					} else {
						return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Tracking is not set up for this channel.")));
					}
				}));
		
		return command;
	}

}
