package alice.modular.modules;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyManager;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.modules.Module;
import alice.framework.utilities.SaveFiles;
import alina.structures.SyncedJSONArray;
import alina.structures.SyncedJSONObject;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.GuildMemberEditSpec;
import discord4j.core.spec.VoiceChannelCreateSpec;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Mono;

public class RoomModule extends Module<VoiceStateUpdateEvent> {

	public RoomModule() {
		super(VoiceStateUpdateEvent.class);
	}

	@Override
	public Command<VoiceStateUpdateEvent> buildCommand(Builder<VoiceStateUpdateEvent> dfb) {
		DependencyManager<VoiceStateUpdateEvent,VoiceChannel> ccdm = dfb.<VoiceChannel>addDependency(vsue -> vsue.getCurrent().getChannel());
		DependencyManager<VoiceStateUpdateEvent,VoiceChannel> ocdm = dfb.<VoiceChannel>addDependency(vsue -> vsue.getOld().get().getChannel());
		DependencyManager<VoiceStateUpdateEvent,Member> cmdm = dfb.<Member>addDependency(vsue -> vsue.getCurrent().getMember());
		DependencyManager<VoiceStateUpdateEvent,Long> cdm = dfb.<Long>addDependency(vsue -> vsue.getOld().get().getChannel().flatMap(vc -> vc.getVoiceStates().count()));
		
		DependencyFactory<VoiceStateUpdateEvent> df = dfb.build();
		
		Command<VoiceStateUpdateEvent> command = new Command<VoiceStateUpdateEvent>(df);
		command.withDependentCondition(ccdm.with(ocdm).buildCondition((vc,oc) -> vc != oc));
		command.withCondition(
			vsue -> {
				SyncedJSONObject sf = SaveFiles.ofGuild(vsue.getCurrent().getGuildId().asLong());
				return sf.has("%room_nexus") && sf.has("%room_temps");
			}
		);
		
		Command<VoiceStateUpdateEvent> joinCommand = new Command<VoiceStateUpdateEvent>(df);
		joinCommand.withDependentCondition(ccdm.buildCondition(vc -> vc != null));
		joinCommand.withDependentCondition(ccdm.buildCondition(
			vc -> {
				SyncedJSONObject sf = SaveFiles.ofGuild(vc.getGuildId().asLong());
				return vc.getId().asString().equals(sf.getString("%room_nexus"));
			}
		));
		joinCommand.withDependentEffect(ccdm.with(cmdm).buildEffect(
			(cc,cm) ->
				cc.getGuild()
					.flatMap(g -> g.createVoiceChannel(
						VoiceChannelCreateSpec.builder()
							.name(cm.getUsername()+"#"+cm.getDiscriminator()+"'s Room")
							.parentId(cc.getCategoryId().isPresent() ? Possible.of(cc.getCategoryId().get()) : Possible.absent())
							.build()
						)
					)
					.flatMap(vc -> cm.edit(
						GuildMemberEditSpec.builder()
							.newVoiceChannelOrNull(vc.getId())
							.build()
						)
						.and(Mono.fromRunnable(() -> {
							SyncedJSONObject sf = SaveFiles.ofGuild(cc.getGuildId().asLong());
							SyncedJSONArray temps = sf.getJSONArray("%room_temps");
							temps.put(vc.getId().asString());
						}))
					)
		));
		
		Command<VoiceStateUpdateEvent> leaveCommand = new Command<VoiceStateUpdateEvent>(df);
		leaveCommand.withDependentCondition(ocdm.buildCondition(vc -> vc != null));
		leaveCommand.withDependentCondition(cdm.buildCondition(
			(mce, cd) -> {
				long count = cd;
				return count == 0 || count == 1 && mce.isMoveEvent();
			}
		));
		leaveCommand.withDependentCondition(ocdm.buildCondition(
			oc -> {
				SyncedJSONObject sf = SaveFiles.ofGuild(oc.getGuildId().asLong());
				SyncedJSONArray temps = sf.getJSONArray("%room_temps");
				for( int f=0; f<temps.length(); f++ ) {
					if( temps.get(f).toString().equals(oc.getId().asString()) ) {
						return true;
					}
				}
				return false;
			}
		));
		leaveCommand.withDependentSideEffect(ocdm.buildSideEffect(
			oc -> {
				SyncedJSONObject sf = SaveFiles.ofGuild(oc.getGuildId().asLong());
				SyncedJSONArray temps = sf.getJSONArray("%room_temps");
				int index = -1;
				for( int f=0; f<temps.length(); f++ ) {
					if( temps.get(f).toString().equals(oc.getId().asString()) ) {
						index = f;
						break;
					}
				}
				if( index != -1 ) {
					temps.remove(index);
				}
			}
		));
		leaveCommand.withDependentEffect(ocdm.buildEffect(oc -> oc.delete()));

		command.withSubcommand(leaveCommand);
		command.withSubcommand(joinCommand);
		
		return command;
	}
	
}
