package alice.modular.modules;

import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.modules.commands.Command;
import alice.framework.modules.commands.MessageModule;
import alice.framework.modules.tasks.DependencyFactory;
import alice.framework.modules.tasks.EffectFactory;
import alice.framework.modules.tasks.DependencyFactory.Builder;
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
		EffectFactory<MessageCreateEvent,MessageChannel> mcef = dfb.<MessageChannel>addDependency(mce -> mce.getMessage().getChannel());
		DependencyFactory<MessageCreateEvent> df = dfb.buildDependencyFactory(); // TODO: figure out why this didn't break
		EffectFactory<MessageCreateEvent,PermissionSet> psef = dfb.<PermissionSet>addDependency(mce -> mce.getMember().get().getBasePermissions());		
		EffectFactory<MessageCreateEvent,VoiceChannel> vcef = dfb.<VoiceChannel>addDependency(mce -> mce.getMember().get().getVoiceState().flatMap(vs -> vs.getChannel()));
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getInvokedCondition("%room"));
		command.withCondition(MessageModule.getGuildCondition());
		command.withDependentCondition(MessageModule.getPermissionCondition(psef, Permission.ADMINISTRATOR));
		
		Command<MessageCreateEvent> assignCommand = new Command<MessageCreateEvent>(df);
		assignCommand.withCondition(MessageModule.getArgumentCondition(1, "assign"));
		assignCommand.withDependentCondition(vcef.getCondition(vc -> vc != null));
		assignCommand.withDependentEffect(vcef.getEffect(vc -> {
			SyncedJSONObject sf = SyncedSaveFile.ofGuild(vc.getGuildId().asLong());
			sf.put("%room_nexus", vc.getId().asString());
			sf.putJSONArray("%room_temps");
		}));
		assignCommand.withDependentEffect(mcef.getEffect(mc -> {return mc.createMessage("Nexus assigned successfully!");}));
		
		Command<MessageCreateEvent> unassignCommand = new Command<MessageCreateEvent>(df);
		unassignCommand.withCondition(MessageModule.getArgumentCondition(1, "unassign"));
		unassignCommand.withEffect(mce -> {
			SyncedJSONObject sf = SyncedSaveFile.ofGuild(mce.getGuildId().get().asLong());
			if( sf.has("%room_nexus") ) {
				sf.remove("%room_nexus");
			}
			if( sf.has("%room_temps") ) {
				sf.remove("%room_temps");
			}
		});
		unassignCommand.withDependentEffect(mcef.getEffect(mc -> {return mc.createMessage("Nexus unassigned successfully!");}));

		command.withSubcommand(assignCommand);
		command.withSubcommand(unassignCommand);
		
		
		return command;
	}
	
}
