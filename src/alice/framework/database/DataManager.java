package alice.framework.database;

import discord4j.core.object.entity.Guild;

public class DataManager {

	protected Guild guild;
	protected SharedSaveFile saveFile;
	
	public DataManager(Guild guild) {
		this.guild = guild;
		this.saveFile = new SharedSaveFile(guild.getId().asLong());
	}
		
}
