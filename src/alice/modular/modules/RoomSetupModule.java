package alice.modular.modules;

import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyManager;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.modules.MessageModule;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;

public class RoomSetupModule extends MessageModule {

	public RoomSetupModule() {
		super();
	}

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent,MessageChannel> mcef = dfb.<MessageChannel>addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent,PermissionSet> psef = dfb.<PermissionSet>addDependency(mce -> mce.getMember().get().getBasePermissions());		
		DependencyManager<MessageCreateEvent,VoiceChannel> vcef = dfb.<VoiceChannel>addDependency(mce -> mce.getMember().get().getVoiceState().flatMap(vs -> vs.getChannel()));
		
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getInvokedCondition("%room"));
		command.withCondition(MessageModule.getGuildCondition());
		command.withDependentCondition(MessageModule.getPermissionCondition(psef, Permission.ADMINISTRATOR));
		
		Command<MessageCreateEvent> assignCommand = new Command<MessageCreateEvent>(df);
		assignCommand.withCondition(MessageModule.getArgumentCondition(1, "assign"));
		assignCommand.withDependentCondition(vcef.buildCondition(vc -> vc != null));
		assignCommand.withDependentSideEffect(vcef.buildSideEffect(
			vc -> {
				SyncedJSONObject sf = SyncedSaveFile.ofGuild(vc.getGuildId().asLong());
				sf.put("%room_nexus", vc.getId().asString());
				sf.putJSONArray("%room_temps");
			}
		));
		assignCommand.withDependentEffect(mcef.buildEffect(mc -> {return mc.createMessage("Nexus assigned successfully!");}));
		
		Command<MessageCreateEvent> unassignCommand = new Command<MessageCreateEvent>(df);
		unassignCommand.withCondition(MessageModule.getArgumentCondition(1, "unassign"));
		unassignCommand.withSideEffect(
			mce -> {
				SyncedJSONObject sf = SyncedSaveFile.ofGuild(mce.getGuildId().get().asLong());
				if( sf.has("%room_nexus") ) {
					sf.remove("%room_nexus");
				}
				if( sf.has("%room_temps") ) {
					sf.remove("%room_temps");
				}
			}
		);
		unassignCommand.withDependentEffect(mcef.buildEffect(mc -> mc.createMessage("Nexus unassigned successfully!")));

		command.withSubcommand(assignCommand);
		command.withSubcommand(unassignCommand);
		
		
		return command;
	}
	
}
