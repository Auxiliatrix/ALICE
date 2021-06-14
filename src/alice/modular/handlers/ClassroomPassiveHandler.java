package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.Handler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.utilities.EventUtilities;
import alice.modular.actions.ChannelDeleteAction;
import alice.modular.actions.MemberMoveChannelAction;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.VoiceChannelCreateSpec;
import reactor.core.publisher.Mono;

public class ClassroomPassiveHandler extends Handler<VoiceStateUpdateEvent> {

	public ClassroomPassiveHandler() {
		super("ClassroomPassive", VoiceStateUpdateEvent.class);
		aliases.add("classp");
	}

	@Override
	protected boolean trigger(VoiceStateUpdateEvent event) {
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getCurrent().getGuild().block().getId().asString());
		if( !isEnabled(true, event.getCurrent().getGuild()) ) {
			return false;
		}
		boolean result = false;
		if( EventUtilities.getConnected(event) ) {
			result |= guildData.has("classroom_hub_channel") && guildData.getString("classroom_hub_channel").equals(event.getCurrent().getChannel().block().getId().asString());
		}
		if( EventUtilities.getDisconnected(event) ) {
			result |= guildData.has("classroom_hub_channel") && guildData.has(String.format("%s_classroom", event.getOld().get().getChannel().block().getId().asString()));
		}
		return result;
	}

	@Override
	protected void execute(VoiceStateUpdateEvent event) {
		Action response = new NullAction();
		
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getCurrent().getGuild().block().getId().asString());
		
		if( EventUtilities.getConnected(event) ) {
			System.out.println("Connected");
			VoiceState validState = event.getCurrent();
			String id = validState.getChannel().block().getId().asString();
			Mono<Member> member = validState.getMember();
			if( guildData.getString("classroom_hub_channel").equals(id) ) {
				String channelName = String.format("%s#%s's Classroom", member.block().getUsername(), member.block().getDiscriminator());
				Mono<VoiceChannel> channel = validState.getGuild().block().createVoiceChannel( c -> constructVC( c, validState.getChannel().block().getCategory(), channelName) );
				response.addMono(channel.flatMap(c -> {
						guildData.put(String.format("%s_classroom", c.getId().asString()), true);
						return new MemberMoveChannelAction(member, c).toMono();
					}));
			}
		}
		if( EventUtilities.getDisconnected(event) ){
			System.out.println("Disconnected");
			VoiceState validState = event.getOld().get();
			String id = validState.getChannel().block().getId().asString();
			if( !guildData.getString("classroom_hub_channel").equals(id) ) {
				if( validState.getChannel().block().getVoiceStates().collectList().block().size() == 0 ) {
					response.addAction(new ChannelDeleteAction(validState.getGuild(), validState.getChannel().block().getId()));
					guildData.remove(String.format("%s_classroom", validState.getChannel().block().getId().asString()));
				}
			}
		}
		
		response.toMono().block();
	}
	
	private VoiceChannelCreateSpec constructVC( VoiceChannelCreateSpec vccs, Mono<Category> category, String channelName ) {
		if( category.block() != null ) {
			vccs.setParentId(category.block().getId());
		}
		vccs.setName(channelName);
		return vccs;
	}
	
}
