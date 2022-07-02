package alice.modular.modules;

import alice.framework.database.SyncedJSONArray;
import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.EffectFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.modules.Module;
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
		EffectFactory<VoiceStateUpdateEvent,VoiceChannel> ccef = dfb.<VoiceChannel>addDependency(vsue -> vsue.getCurrent().getChannel());
		EffectFactory<VoiceStateUpdateEvent,VoiceChannel> ocef = dfb.<VoiceChannel>addDependency(vsue -> vsue.getOld().get().getChannel());
		EffectFactory<VoiceStateUpdateEvent,Member> cmef = dfb.<Member>addDependency(vsue -> vsue.getCurrent().getMember());
		EffectFactory<VoiceStateUpdateEvent,Long> cef = dfb.<Long>addDependency(vsue -> vsue.getOld().get().getChannel().flatMap(vc -> vc.getVoiceStates().count()));
		
		DependencyFactory<VoiceStateUpdateEvent> df = dfb.buildDependencyFactory();
		
		Command<VoiceStateUpdateEvent> command = new Command<VoiceStateUpdateEvent>(df);
		command.withDependentCondition(ccef.with(ocef).getCondition((vc,oc) -> vc != oc));
		command.withCondition(vsue -> {
			SyncedJSONObject sf = SyncedSaveFile.ofGuild(vsue.getCurrent().getGuildId().asLong());
			return sf.has("%room_nexus") && sf.has("%room_temps");
		});
		
		Command<VoiceStateUpdateEvent> joinCommand = new Command<VoiceStateUpdateEvent>(df);
		joinCommand.withDependentCondition(ccef.getCondition(vc -> vc != null));
		joinCommand.withDependentCondition(ccef.getCondition(vc -> {
			SyncedJSONObject sf = SyncedSaveFile.ofGuild(vc.getGuildId().asLong());
			return vc.getId().asString().equals(sf.getString("%room_nexus"));
		}));
		joinCommand.withDependentEffect(ccef.with(cmef).getEffect((cc,cm) -> {
			return cc.getGuild()
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
						SyncedJSONObject sf = SyncedSaveFile.ofGuild(cc.getGuildId().asLong());
						SyncedJSONArray temps = sf.getJSONArray("%room_temps");
						temps.put(vc.getId().asString());
					}))
				)
				;
		}
		));
		
		Command<VoiceStateUpdateEvent> leaveCommand = new Command<VoiceStateUpdateEvent>(df);
		leaveCommand.withDependentCondition(ocef.getCondition(vc -> vc != null));
		leaveCommand.withDependentCondition(d -> {
			long count = cef.requestFrom(d);
			return count == 0 || count == 1 && d.getEvent().isMoveEvent();
		});
		leaveCommand.withDependentCondition(ocef.getCondition(oc -> {
			SyncedJSONObject sf = SyncedSaveFile.ofGuild(oc.getGuildId().asLong());
			SyncedJSONArray temps = sf.getJSONArray("%room_temps");
			for( int f=0; f<temps.length(); f++ ) {
				if( temps.get(f).toString().equals(oc.getId().asString()) ) {
					return true;
				}
			}
			return false;
		}));
		leaveCommand.withDependentEffect(ocef.getEffect(oc -> {
			SyncedJSONObject sf = SyncedSaveFile.ofGuild(oc.getGuildId().asLong());
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
		}));
		leaveCommand.withDependentEffect(ocef.getEffect(oc -> {return oc.delete();}));

		command.withSubcommand(leaveCommand);
		command.withSubcommand(joinCommand);
		
		return command;
	}
	
}
