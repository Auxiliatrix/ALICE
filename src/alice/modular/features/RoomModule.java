package alice.modular.features;

import java.util.Optional;

import alice.framework.database.SyncedJSONArray;
import alice.framework.database.SyncedSaveFile;
import alice.framework.modules.commands.Command;
import alice.framework.modules.commands.Module;
import alice.framework.modules.tasks.DependencyFactory;
import alice.framework.modules.tasks.DependencyFactory.Builder;
import alice.framework.modules.tasks.EffectFactory;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
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
		
		EffectFactory<VoiceStateUpdateEvent,Guild> gef = dfb.addDependency(vsue -> vsue.getCurrent().getGuild());
		EffectFactory<VoiceStateUpdateEvent,Member> mef = dfb.addDependency(vsue -> vsue.getCurrent().getMember().onErrorReturn(null));
		EffectFactory<VoiceStateUpdateEvent,VoiceChannel> ccef = dfb.addDependency(vsue -> vsue.getCurrent().getChannel().onErrorReturn(null));
		EffectFactory<VoiceStateUpdateEvent,Optional<VoiceState>> vsef = dfb.addWrappedDependency(vsue -> vsue.getOld());
		
		DependencyFactory<VoiceStateUpdateEvent> df = dfb.buildDependencyFactory();
		Command<VoiceStateUpdateEvent> command = new Command<VoiceStateUpdateEvent>(df)
			.withDependentCondition(gef.getCondition(g -> SyncedSaveFile.ofGuild(g.getId().asLong()).has("%room_nexus")))
			.withDependentCondition(gef.getCondition(g -> SyncedSaveFile.ofGuild(g.getId().asLong()).has("%room_temps")))
			.withDependentCondition(ccef.with(vsef).getCondition((cc, vs) -> 
			vs.isPresent() && !cc.getId().asString().equals(vs.get().getChannelId().get().asString()) || !vs.isPresent() ));
		
		Command<VoiceStateUpdateEvent> joinCommand = new Command<VoiceStateUpdateEvent>(df)
				.withDependentCondition(ccef.getCondition(vc -> vc != null)) 
				.withDependentCondition(gef.with(ccef).getCondition((g,cc) -> SyncedSaveFile.ofGuild(g.getId().asLong()).getString("%room_nexus").equals(cc.getId().asString())))
				.withDependentEffect(gef.with(mef).with(ccef).getEffect((g,m,cc) -> {
							return g.createVoiceChannel(VoiceChannelCreateSpec.builder()
																	.name(m.getUsername()+"#"+m.getDiscriminator()+"'s Room")
																	.parentId(cc.getCategoryId().isPresent() ? Possible.of(cc.getCategoryId().get()) : Possible.absent())
																	.build())
													.flatMap(vc -> m.edit(GuildMemberEditSpec.builder()
																	.newVoiceChannelOrNull(vc.getId())
																	.build())
															.and(Mono.fromRunnable(() -> {
																SyncedJSONArray temps = SyncedSaveFile.ofGuild(g.getId().asLong()).getJSONArray("%room_temps");
																temps.put(vc.getId().asString());
															}))
													);
						}));
		
		Command<VoiceStateUpdateEvent> leaveCommand = new Command<VoiceStateUpdateEvent>(df)
			.withDependentCondition(vsef.getCondition(vs -> vs.isPresent()))
			.withDependentEffect(gef.with(vsef).getEffect((g,vs) -> {
				SyncedJSONArray temps = SyncedSaveFile.ofGuild(g.getId().asLong()).getJSONArray("%room_temps");
				int index = -1;
				for( int f=0; f<temps.length(); f++ ) {
					if( temps.get(f).toString().equals(vs.get().getChannelId().get().asString()) ) {
						index = f;
					}
				}
				if( index != -1 ) {
					temps.remove(index);
				}
			}))
			.withDependentEffect(vsef.getEffect(vc -> {
				return vc.get().getChannel().flatMap(c -> c.delete());
			}));
		
		command.withSubcommand(joinCommand)
			.withSubcommand(leaveCommand);
		return command;
	}
	
}
