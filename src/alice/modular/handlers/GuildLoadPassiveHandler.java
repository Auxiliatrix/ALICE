package alice.modular.handlers;

import java.io.File;

import alice.framework.actions.Action;
import alice.framework.actions.VoidAction;
import alice.framework.handlers.Handler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.utilities.AliceLogger;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.GuildCreateEvent;

public class GuildLoadPassiveHandler extends Handler<GuildCreateEvent> {
	
	public GuildLoadPassiveHandler() {
		super("GuildLoader", false, GuildCreateEvent.class);
	}
	
	protected boolean trigger(GuildCreateEvent event) {
		return true;
	}
	
	protected Action execute(GuildCreateEvent event) {
		return new VoidAction( () -> loadGuildData(event) );
	}
	
	private void loadGuildData(GuildCreateEvent event) {
		Snowflake guildId = event.getGuild().getId();
		String guildFile = String.format("%s%s%s%s%s.json", "tmp", File.separator, "guilds", File.separator, guildId.asString());
		Brain.guildIndex.put(guildId.asString(), new AtomicSaveFile(guildFile));
		AliceLogger.info(String.format("Loaded guild data for %s.", event.getGuild().getName()));
	}
	
}
