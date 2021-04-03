package alice.modular.handlers;

import alice.framework.handlers.Handler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import discord4j.core.event.domain.guild.MemberJoinEvent;

public class UserJoinPassiveHandler extends Handler<MemberJoinEvent> {
		
	public UserJoinPassiveHandler() {
		super("GuildLoader", MemberJoinEvent.class);
	}
	
	protected boolean trigger(MemberJoinEvent event) {
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getGuild().block().getId().asString());
		return guildData.has(String.format("ipban_", event.getMember().getUsername()));
	}

	@Override
	protected void execute(MemberJoinEvent event) {
		event.getMember().ban(c -> c.setReason("IP Ban")).block();
		
	}
}
