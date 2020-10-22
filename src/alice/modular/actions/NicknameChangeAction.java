package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Member;

public class NicknameChangeAction extends Action {
		
	public NicknameChangeAction(Member member, String nickname) {
		super(member.edit(spec -> spec.setNickname(nickname)));
	}
	
}
