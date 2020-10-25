package alice.framework.interactives;

import org.json.JSONObject;

import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.Reaction;

public abstract class Interactive {
	
	protected Snowflake id;
	
	public Interactive(Snowflake id) {
		this.id = id;
	}
	
	public Interactive(JSONObject object) {}
	
	public abstract void update(Reaction reaction);
	
	public abstract void enable();
	
	public abstract void disable();
	
}
