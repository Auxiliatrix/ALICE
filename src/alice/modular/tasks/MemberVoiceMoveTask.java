package alice.modular.tasks;

import alice.framework.tasks.Task;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.GuildMemberEditSpec;
import reactor.core.publisher.Mono;

public class MemberVoiceMoveTask extends Task<VoiceChannel> {

	private Member member;
	
	public MemberVoiceMoveTask( Member member ) {
		this.member = member;
	}
	
	@Override
	public Mono<?> apply(VoiceChannel t) {
		return member.edit(GuildMemberEditSpec.builder().newVoiceChannelOrNull(t.getId()).build());
	}

}
