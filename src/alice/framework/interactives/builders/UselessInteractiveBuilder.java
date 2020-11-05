package alice.framework.interactives.builders;

import org.json.JSONObject;

import alice.framework.interactives.Interactive;
import alice.framework.interactives.UselessInteractive;
import discord4j.common.util.Snowflake;

public class UselessInteractiveBuilder extends InteractiveBuilder<UselessInteractive> {

	public UselessInteractiveBuilder() {
		super(UselessInteractive.class);
	}

	@Override
	public UselessInteractive ofJSONObject(JSONObject object) {
		return new UselessInteractive(
				Snowflake.of(object.getString("channelId")), 
				Snowflake.of(object.getString("messageId")),
				object.getInt("count")
				);
	}

	@Override
	public void constructJSONObject(JSONObject object, Interactive interactive) {
		object.put("count", ((UselessInteractive) interactive).getCount());
	}

}
