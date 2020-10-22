package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.Channel.Type;
import discord4j.core.spec.TextChannelCreateSpec;
import discord4j.core.spec.VoiceChannelCreateSpec;
import reactor.core.publisher.Mono;

public class ChannelCreateAction extends Action {
	
	public ChannelCreateAction(Mono<Guild> guild, Mono<Category> category, String channelName, Type channelType) {
		super(
			channelType == Type.GUILD_TEXT ? guild.block().createTextChannel(tccs -> constructTC(tccs, category, channelName) ) : 
				channelType == Type.GUILD_VOICE ? guild.block().createVoiceChannel( vccs -> constructVC(vccs, category, channelName) ) :
					Mono.fromRunnable( () -> {} )
		);
	}
	
	private static TextChannelCreateSpec constructTC( TextChannelCreateSpec tccs, Mono<Category> category, String channelName ) {
		if( category.block() != null ) {
			tccs.setParentId(category.block().getId());
		}
		tccs.setName(channelName);
		return tccs;
	}
	
	private static VoiceChannelCreateSpec constructVC( VoiceChannelCreateSpec vccs, Mono<Category> category, String channelName ) {
		if( category.block() != null ) {
			vccs.setParentId(category.block().getId());
		}
		vccs.setName(channelName);
		return vccs;
	}
	
}
