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
		super();
		switch( channelType ) {
			case GUILD_TEXT:
				this.mono = guild.block().createTextChannel( tccs -> constructTC(tccs, category, channelName) );
				break;
			case GUILD_VOICE:
				this.mono = guild.block().createVoiceChannel( vccs -> constructVC(vccs, category, channelName) );
			default:
				this.mono = Mono.fromRunnable(() -> {});
				break;
		}
		this.mono = Mono.fromRunnable(() -> {});
	}
	
	private TextChannelCreateSpec constructTC( TextChannelCreateSpec tccs, Mono<Category> category, String channelName ) {
		if( category.block() != null ) {
			tccs.setParentId(category.block().getId());
		}
		tccs.setName(channelName);
		return tccs;
	}
	
	private VoiceChannelCreateSpec constructVC( VoiceChannelCreateSpec vccs, Mono<Category> category, String channelName ) {
		if( category.block() != null ) {
			vccs.setParentId(category.block().getId());
		}
		vccs.setName(channelName);
		return vccs;
	}
	
}
