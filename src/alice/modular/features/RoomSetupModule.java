package alice.modular.features;

import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.modules.commands.Command;
import alice.framework.modules.commands.MessageModule;
import alice.framework.modules.tasks.DependencyFactory;
import alice.framework.modules.tasks.DependencyFactory.Builder;
import alice.framework.modules.tasks.EffectFactory;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;

public class RoomSetupModule extends MessageModule {

	public RoomSetupModule() {
		super();
	}

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		EffectFactory<MessageCreateEvent,Guild> gef = dfb.addDependency(mce -> mce.getGuild());
		EffectFactory<MessageCreateEvent,PermissionSet> psef = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions().onErrorReturn(null));
		EffectFactory<MessageCreateEvent,VoiceChannel> vcef = dfb.addDependency(mce -> mce.getMember().get().getVoiceState().flatMap(vs -> vs.getChannel()).onErrorReturn(null));
		
		DependencyFactory<MessageCreateEvent> df = dfb.buildDependencyFactory();
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		
		// TODO: the problem is dependentCondition
		
		command.withCondition(MessageModule.getInvokedCondition("%room"));
		command.withDependentCondition(d -> d.<PermissionSet>request(psef) != null);
				//.withDependentCondition(psef.getCondition(ps -> ps != null))
		command.withDependentCondition(MessageModule.getPermissionCondition(psef, Permission.ADMINISTRATOR));
		
		Command<MessageCreateEvent> setupCommand = new Command<MessageCreateEvent>(df);
		setupCommand.withCondition(MessageModule.getArgumentCondition(1, "assign"));
		setupCommand.withDependentCondition(d -> {
					return d.<VoiceChannel>request(vcef) != null;
				});
				//.withDependentCondition(vcef.getCondition(vs -> vs != null))
		setupCommand.withDependentEffect(gef.with(vcef).getEffect((g,vc) -> {
					SyncedJSONObject sf = SyncedSaveFile.ofGuild(g.getId().asLong());
					sf.put("%room_nexus", vc.getId().asString());
					sf.putJSONArray("%room_temps");
				}));
		
		Command<MessageCreateEvent> takedownCommand = new Command<MessageCreateEvent>(df);
		takedownCommand.withCondition(MessageModule.getArgumentCondition(1, "remove"));
		takedownCommand.withDependentEffect(gef.getEffect(g -> {
					SyncedJSONObject sf = SyncedSaveFile.ofGuild(g.getId().asLong());
					if( sf.has("%room_nexus") ) {
						sf.remove("%room_nexus");
					}
					if( sf.has("%room_temps") ) {
						sf.remove("%room_temps");
					}
				}));
		
//		command.withSubcommand(setupCommand);
//		command.withSubcommand(takedownCommand);
		
		return command;
	}
	
}
