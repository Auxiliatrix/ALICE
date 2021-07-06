package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.Handler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.modular.actions.NicknameChangeAction;
import discord4j.core.event.domain.guild.MemberUpdateEvent;

@Deprecated
public class NicknamePassiveHandler extends Handler<MemberUpdateEvent> {
	
	public NicknamePassiveHandler() {
		super("Nickname", MemberUpdateEvent.class);
		aliases.add("nick");
	}

	@Override
	protected boolean trigger(MemberUpdateEvent event) {
		return isEnabled(false, event.getGuild()) && !event.getMember().block().getDisplayName().equals(event.getOld().get().getDisplayName());
	}

	@Override
	protected void execute(MemberUpdateEvent event) {
		Action response = new NullAction();
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getGuild().block().getId().asString());
		String key = String.format("nick_locked_%s", event.getMember().block().getId().asString());
		if( guildData.has(key) ) {
			response.addAction(new NicknameChangeAction(event.getMember().block(), guildData.getString(key)));
		}
		response.toMono().block();
	}
	
}
