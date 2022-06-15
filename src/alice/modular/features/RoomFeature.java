package alice.modular.features;

import java.util.List;

import alice.framework.database.SharedSaveFile;
import alice.framework.features.Feature;
import alice.framework.features.MessageFeature;
import alice.framework.structures.TokenizedString;
import alice.framework.tasks.DependentStacker;
import alice.framework.tasks.MultipleDependentStacker;
import alice.framework.tasks.Stacker;
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
import discord4j.core.spec.VoiceChannelCreateSpec;
import reactor.core.publisher.Mono;


public class RoomFeature extends MessageFeature {

	@ManuallyInitialized
	private class RoomPassiveFeature extends Feature<VoiceStateUpdateEvent> {

		private RoomPassiveFeature() {
			super("room", VoiceStateUpdateEvent.class);
		}

		@Override
		protected boolean listen(VoiceStateUpdateEvent type) {
			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Mono<?> respond(VoiceStateUpdateEvent type) {
			
			Stacker response = new Stacker();
			
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
						DependentStacker<VoiceChannel> vcStacker = new DependentStacker<VoiceChannel>(((Guild) a.get(2)).createVoiceChannel(VoiceChannelCreateSpec.builder().name(String.format("%s#%s's Room", ((Member) a.get(3)).getUsername(), ((Member) a.get(3)).getDiscriminator())).parentId(((VoiceChannel) a.get(0)).getCategoryId().get()).build()));
						vcStacker.addEffect(vc -> sf.putBoolean(String.format("%s%s", ROOM_CHANNEL_PREFIX, vc.getId().asString()), true));
						return vcStacker.addTask(new MemberVoiceMoveTask((Member) a.get(3)));
					} else {
						return Mono.fromRunnable(() -> {});
					}
				} else if( (VoiceChannel) a.get(0) != null && (VoiceChannel) a.get(0) != (VoiceChannel) a.get(1) ) {	// If moved from one voice channel to a different one
					Mono<Void> subResponse = Mono.fromRunnable(() -> {});
					
					if( sf.has(HUB_CHANNEL_KEY) && sf.getString(HUB_CHANNEL_KEY).equals(((VoiceChannel) a.get(0)).getId().asString()) ) {
						DependentStacker<VoiceChannel> vcStacker = new DependentStacker<VoiceChannel>(((Guild) a.get(2)).createVoiceChannel(VoiceChannelCreateSpec.builder().name(String.format("%s#%s's Room", ((Member) a.get(3)).getUsername(), ((Member) a.get(3)).getDiscriminator())).parentId(((VoiceChannel) a.get(0)).getCategoryId().get()).build()));
						vcStacker.addEffect(vc -> sf.putBoolean(String.format("%s%s", ROOM_CHANNEL_PREFIX, vc.getId().asString()), true));
						subResponse = subResponse.and(vcStacker.addTask(new MemberVoiceMoveTask((Member) a.get(3))));
					}
					
					if( sf.has(String.format("%s%s", ROOM_CHANNEL_PREFIX, ((VoiceChannel) a.get(1)).getId().asLong())) ) {
						if( ((List<VoiceState>) a.get(4)).size() == 0 ) {
							subResponse = subResponse.and(((VoiceChannel) a.get(1)).delete())
									.and(Mono.fromRunnable(() -> {
										sf.remove(String.format("%s%s", ROOM_CHANNEL_PREFIX, ((VoiceChannel) a.get(1)).getId().asString()));
									}));
						}
					}
					
					return subResponse;
				} else if( (VoiceChannel) a.get(0) == null && (VoiceChannel) a.get(1) != null ) {	// If disconnected from a voice channel
					if( sf.has(String.format("%s%s", ROOM_CHANNEL_PREFIX, ((VoiceChannel) a.get(1)).getId().asLong())) ) {
						if( ((List<VoiceState>) a.get(4)).size() == 0 ) {
							return ((VoiceChannel) a.get(1)).delete()
									.and(Mono.fromRunnable(() -> {
										sf.remove(String.format("%s%s", ROOM_CHANNEL_PREFIX, ((VoiceChannel) a.get(1)).getId().asString()));
									}));
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
		@SuppressWarnings("unused")
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
		Stacker response = new Stacker();
		String message = type.getMessage().getContent();
		TokenizedString ts = new TokenizedString(message);
		
		MultipleDependentStacker stacker = new MultipleDependentStacker(type.getMessage().getChannel(),
																		type.getMessage().getAuthorAsMember().flatMap(m -> m.getVoiceState()).flatMap(vs -> vs.getChannel()),
																		type.getGuild()
																		);
		
		if( ts.size() < 2 ) {
			stacker.addTask(a -> (new EmbedSendTask(EmbedBuilders.applyErrorFormat("You must specify an argument.", EmbedBuilders.ERR_USAGE))).apply((MessageChannel) a.get(0)));
//			stacker.addTask(a -> (new EmbedSendTask(EmbedBuilders.getHelpConstructor(type.getMessage().getAuthor().get(), this))).apply((MessageChannel) a.get(0)));
		} else {
			switch( ts.getString(1).toLowerCase() ) {
				case "setup":
					stacker.addTask(a -> {
						if( (VoiceChannel) a.get(1) == null ) {
							return (new EmbedSendTask(EmbedBuilders.applyErrorFormat("You must be connected to a voice channel!", EmbedBuilders.ERR_USAGE))).apply((MessageChannel) a.get(0));
						} else {
							if( !((VoiceChannel) a.get(1)).getCategoryId().isPresent() ) {
								return (new EmbedSendTask(EmbedBuilders.applyErrorFormat("Voice channel must be inside a channel category!", EmbedBuilders.ERR_USAGE))).apply((MessageChannel) a.get(0));
							} else {
								SharedSaveFile sf = new SharedSaveFile(((Guild) a.get(2)).getId().asLong());
								sf.putString(HUB_CHANNEL_KEY, ((VoiceChannel) a.get(1)).getId().asString());
								return (new MessageSendTask("Room set up successfully!").apply((MessageChannel) a.get(0)));
							}
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
							return (new EmbedSendTask(EmbedBuilders.applyErrorFormat("You do not have a room hub set up in this server!", EmbedBuilders.ERR_USAGE))).apply((MessageChannel) a.get(0));
						}
					});
					break;
				default:
					stacker.addTask(a -> (new EmbedSendTask(EmbedBuilders.applyErrorFormat("Invalid command!", EmbedBuilders.ERR_USAGE))).apply((MessageChannel) a.get(0)));
					break;
			}
		}
		
		response.append(stacker);
		
		return response.toMono();
	}
	
}
