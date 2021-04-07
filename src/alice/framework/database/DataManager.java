package alice.framework.database;

import alice.framework.main.Brain;
import discord4j.core.object.entity.Guild;

public class DataManager {

	protected Guild guild;
	
	public DataManager(Guild guild) {
		this.guild = guild;		
	}
	
	protected SharedSaveFile getGuildData() {
		return Brain.guildIndex.get(guild.getId());
	}
	
}
