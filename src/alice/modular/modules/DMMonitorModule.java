package alice.modular.modules;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.main.Brain;
import alice.framework.modules.MessageModule;
import alice.framework.utilities.SaveFiles;
import alina.structures.SyncedJSONArray;
import alina.structures.SyncedJSONObject;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.entity.RestChannel;
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
			SyncedJSONObject ssf = SaveFiles.of("global");
			return ssf.has("%dmm_channels");
		});
		report.withEffect(mce -> {
			SyncedJSONObject ssf = SaveFiles.of("global");
			SyncedJSONArray channels = ssf.getJSONArray("%dmm_channels");
			Mono<?> ret = Mono.fromRunnable(() -> {});
			for( int f=0; f<channels.length(); f++ ) {
				String channel = channels.getString(f);
				RestChannel rc = Brain.client.getChannelById(Snowflake.of(channel));
				ret.and(rc.createMessage(String.format("%s#%s: %s", mce.getMessage().getAuthor().get().getUsername(), mce.getMessage().getAuthor().get().getDiscriminator(), mce.getMessage().getContent())));
			}
			return ret;
		});
		
		DependencyManager<MessageCreateEvent, PermissionSet> psdm = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		Command<MessageCreateEvent> setup = command.addSubcommand();
		setup.withCondition(MessageModule.getGuildCondition());
		setup.withDependentCondition(MessageModule.getPermissionCondition(psdm, Permission.ADMINISTRATOR));
		
		
		
		return command;
	}

}
