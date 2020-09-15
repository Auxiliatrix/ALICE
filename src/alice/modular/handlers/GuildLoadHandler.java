package alice.modular.handlers;

import java.io.File;

import org.json.JSONObject;

import alice.framework.actions.Action;
import alice.framework.actions.VoidAction;
import alice.framework.handlers.Handler;
import alice.framework.main.Brain;
import alice.framework.utilities.AliceLogger;
import alice.framework.utilities.FileIO;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.GuildCreateEvent;

public class GuildLoadHandler extends Handler<GuildCreateEvent> {
	
	public GuildLoadHandler() {
		super("GuildLoader", "Root", false, GuildCreateEvent.class);
	}
	
	protected boolean trigger(GuildCreateEvent event) {
		return true;
	}
	
	protected Action execute(GuildCreateEvent event) {
		return new VoidAction( () -> loadGuildData(event) );
	}
	
	private void loadGuildData(GuildCreateEvent event) {
		Snowflake guildId = event.getGuild().getId();
		String loadData = FileIO.readFromFile(String.format("%s%s%s%s%s.json", "tmp", File.separator, "guilds", File.separator, guildId.asString()), "{}");
		JSONObject guildData = new JSONObject(loadData);
		Brain.guildIndex.updateAndGet( (gd) -> { gd.put(guildId.asString(), guildData); return gd; } );
		AliceLogger.info(String.format("Loaded guild data for %s.", event.getGuild().getName()));
	}
	
}
