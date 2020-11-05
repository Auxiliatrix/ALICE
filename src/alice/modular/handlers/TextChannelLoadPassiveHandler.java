package alice.modular.handlers;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import alice.framework.handlers.Handler;
import alice.framework.interactives.builders.InteractiveBuilder;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.channel.TextChannelCreateEvent;

public class TextChannelLoadPassiveHandler extends Handler<TextChannelCreateEvent> {
	
	public TextChannelLoadPassiveHandler() {
		super("TextChannelLoader", TextChannelCreateEvent.class);
	}
	
	protected boolean trigger(TextChannelCreateEvent event) {
		return true;
	}
	
	protected void execute(TextChannelCreateEvent event) {
		loadTextChannelData(event);
	}
	
	private void loadTextChannelData(TextChannelCreateEvent event) {
		Snowflake channelId = event.getChannel().getId();
		String channelFileName = String.format("%s%s%s%s%s.json", "tmp", File.separator, "channels", File.separator, channelId.asString());
		Brain.channelIndex.put(channelId.asString(), new AtomicSaveFile(channelFileName));
		AtomicSaveFile channelFile = Brain.channelIndex.get(channelId.asString());
		try {
			JSONObject interactives = channelFile.getJSONObject("interactives");
			for( String key : interactives.keySet() ) {
				JSONObject interactiveObject = interactives.getJSONObject(key);
				String type = interactiveObject.getString("interactive_type");
				for( InteractiveBuilder<?> i : Brain.interactiveTypes.get() ) {
					if( i.getBuilderType().equals(type) ) {
						Brain.interactives.put(String.format("%s_%s", event.getChannel().getId().asString(), key), i.ofJSONObject(interactiveObject));
					}
				}
			}
		} catch(JSONException e) {
			// Do nothing
		}
	}
	
}