package alice.modular.handlers;

import org.json.JSONArray;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.MessageHandler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class ResponsePassiveHandler extends MessageHandler {

	public ResponsePassiveHandler() {
		super("Response", false, PermissionProfile.getAnyonePreset().andNotDM());
		aliases.add("resp");
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getGuild().block().getId().asString());
		if( !guildData.has("responses") ) {
			guildData.put("responses", new JSONArray());
		}
		JSONArray responses = guildData.getJSONArray("responses");
		return responses.length() > 0;
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		Action response = new NullAction();
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getGuild().block().getId().asString());
		JSONArray responses = guildData.getJSONArray("responses");
		for( int f=0; f<responses.length(); f++ ) {
			if( event.getMessage().getContent().matches(responses.getJSONObject(f).getString("trigger")) ) {
				response.addAction(new MessageCreateAction(event.getMessage().getChannel(), responses.getJSONObject(f).getString("response")));
			}
		}
		
		response.toMono().block();
	}

}
