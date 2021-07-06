package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.GuildMemberEditSpec;
import reactor.core.publisher.Mono;

@Deprecated
public class MemberMoveChannelAction extends Action {

	public MemberMoveChannelAction( Mono<Member> member, VoiceChannel channel ) {
		super( member.block().edit( gmes -> {
				editUserVC(gmes, member, channel);
			})
		);
	}
	
	private static GuildMemberEditSpec editUserVC(GuildMemberEditSpec gmes, Mono<Member> member, VoiceChannel channel) {
		gmes.setNewVoiceChannel(channel.getId());
		return gmes;
	}
	
}
