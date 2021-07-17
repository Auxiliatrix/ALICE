package alice.modular.features;

import java.util.List;

import alice.framework.database.SharedSaveFile;
import alice.framework.features.Feature;
import alice.framework.features.MessageFeature;
import alice.framework.structures.TokenizedString;
import alice.framework.tasks.DependentStacker;
import alice.framework.tasks.IndependentStacker;
import alice.framework.tasks.MultipleDependentStacker;
import alice.framework.utilities.EmbedBuilders;
import alice.modular.tasks.EmbedSendTask;
import alice.modular.tasks.MemberVoiceMoveTask;
import alice.modular.tasks.MessageSendTask;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Mono;


public class RoomFeature extends MessageFeature {

	protected class RoomPassiveFeature extends Feature<VoiceStateUpdateEvent> {

		protected RoomPassiveFeature() {
			super("room", VoiceStateUpdateEvent.class);
		}

		@Override
		protected boolean listen(VoiceStateUpdateEvent type) {
			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Mono<?> respond(VoiceStateUpdateEvent type) {
			
			IndependentStacker response = new IndependentStacker();
			
			MultipleDependentStacker stacker = new MultipleDependentStacker(type.getCurrent().getChannel(), 
																			type.getOld().isPresent() ? type.getOld().get().getChannel() : Mono.empty(), 
																			type.getCurrent().getGuild(), 
																			type.getCurrent().getMember(), 
																			type.getOld().isPresent() ? type.getOld().get().getChannel().map(c -> c.getVoiceStates().collectList().block()) : Mono.empty()
																			);
			
			stacker.addTask(a -> {
				SharedSaveFile sf = new SharedSaveFile(((Guild) a.get(2)).getId().asLong());
				if( (VoiceChannel) a.get(0) != null && (VoiceChannel) a.get(1) == null ) {	// If joined a voice channel
					if( !sf.has(HUB_CHANNEL_KEY) ) {
						return Mono.fromRunnable(() -> {});
					} else if( sf.getString(HUB_CHANNEL_KEY).equals(((VoiceChannel) a.get(0)).getId().asString()) ) {
						DependentStacker<VoiceChannel> vcStacker = new DependentStacker<VoiceChannel>(((Guild) a.get(2)).createVoiceChannel(c -> c.setName("Room")));
						vcStacker.addEffect(vc -> sf.putBoolean(String.format("%s%s", ROOM_CHANNEL_PREFIX, vc.getId().asString()), true));
						return vcStacker.addTask(new MemberVoiceMoveTask((Member) a.get(3)));
					} else {
						return Mono.fromRunnable(() -> {});
					}
				} else if( (VoiceChannel) a.get(0) != null && (VoiceChannel) a.get(0) != (VoiceChannel) a.get(1) ) {	// If moved from one voice channel to a different one
					// TODO: Move
					return Mono.fromRunnable(() -> {});
				} else if( (VoiceChannel) a.get(0) == null && (VoiceChannel) a.get(1) != null ) {	// If disconnected from a voice channel
					if( sf.has(String.format("%s%s", ROOM_CHANNEL_PREFIX, ((VoiceChannel) a.get(1)).getId().asLong())) ) {
						if( ((List<VoiceState>) a.get(4)).size() == 0 ) {
							return ((VoiceChannel) a.get(1)).delete();
						}
					}
					return Mono.fromRunnable(() -> {});
				} else {
					return Mono.fromRunnable(() -> {});
				}
			});
			
			response.append(stacker);
			
			return response.toMono();
		}
		
	}
	
	protected static final String HUB_CHANNEL_KEY = ".room_hub_id";
	protected static final String ROOM_CHANNEL_PREFIX = ".room_child_id_";
	
	public RoomFeature() {
		super("room");
		RoomPassiveFeature rpf = new RoomPassiveFeature();
		withCheckInvoked();
		withExclusionClass(ExclusionClass.STANDARD);
	}

	@Override
	protected boolean condition(MessageCreateEvent event) {
		return true;
	}

	@Override
	protected Mono<?> respond(MessageCreateEvent type) {
		IndependentStacker response = new IndependentStacker();
		String message = type.getMessage().getContent();
		TokenizedString ts = new TokenizedString(message);
		
		MultipleDependentStacker stacker = new MultipleDependentStacker(type.getMessage().getChannel(),
																		type.getMessage().getAuthorAsMember().flatMap(m -> m.getVoiceState()).flatMap(vs -> vs.getChannel()),
																		type.getGuild()
																		);
		
		if( ts.size() < 2 ) {
			stacker.addTask(a -> (new EmbedSendTask(EmbedBuilders.getHelpConstructor(type.getMessage().getAuthor().get(), this))).apply((MessageChannel) a.get(0)));
		} else {
			switch( ts.get(1).toLowerCase() ) {
				case "setup":
					stacker.addTask(a -> {
						if( (VoiceChannel) a.get(1) == null ) {
							return (new EmbedSendTask(EmbedBuilders.getErrorConstructor("You must be connected to a voice channel!", EmbedBuilders.ERR_USAGE))).apply((MessageChannel) a.get(0));
						} else {
							SharedSaveFile sf = new SharedSaveFile(((Guild) a.get(2)).getId().asLong());
							sf.putString(HUB_CHANNEL_KEY, ((VoiceChannel) a.get(1)).getId().asString());
							return (new MessageSendTask("Room set up successfully!").apply((MessageChannel) a.get(0)));
						}
					});
					break;
				case "takedown":
					stacker.addTask(a -> {
						SharedSaveFile sf = new SharedSaveFile(((Guild) a.get(2)).getId().asLong());
						if( sf.has(HUB_CHANNEL_KEY) ) {
							sf.remove(HUB_CHANNEL_KEY);
							return (new MessageSendTask("Room deactivated successfully!").apply((MessageChannel) a.get(0)));
						} else {
							return (new EmbedSendTask(EmbedBuilders.getErrorConstructor("You do not have a room hub set up in this server!", EmbedBuilders.ERR_USAGE))).apply((MessageChannel) a.get(0));
						}
					});
					break;
				default:
					stacker.addTask(a -> (new EmbedSendTask(EmbedBuilders.getErrorConstructor("Invalid command!", EmbedBuilders.ERR_USAGE))).apply((MessageChannel) a.get(0)));
					break;
			}
		}
		
		response.append(stacker);
		
		return response.toMono();
	}
	
}