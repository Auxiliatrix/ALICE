package alice.framework.database;

import discord4j.common.util.Snowflake;

public class DataManager {

	protected Snowflake guildId;
	protected SharedSaveFile saveFile;
	
	public DataManager(Snowflake guildId) {
		this.guildId = guildId;
		this.saveFile = new SharedSaveFile(guildId.asLong());
	}
		
}
