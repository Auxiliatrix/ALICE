package alice.framework.interactives.builders;

import org.json.JSONObject;

import alice.framework.interactives.Interactive;

public abstract class InteractiveBuilder<I extends Interactive> {

	protected Class<I> type;
	
	// For reflective use only
	protected InteractiveBuilder(Class<I> type) {
		this.type = type;
	}
	
	public abstract I ofJSONObject(JSONObject object);
	public abstract void constructJSONObject(JSONObject object, Interactive interactive);
	
	public final JSONObject toJSONObject(Interactive interactive) {
		JSONObject object = new JSONObject();
		object.put("type", interactive.getClass().getName());
		object.put("channelId", interactive.channelId.asString());
		object.put("messageId", interactive.messageId.asString());
		constructJSONObject(object, interactive);
		return object;
	}
	
	public final String getBuilderType() {
		return type.getName();
	}
	
}
