package alice.modular.handlers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.MessageHandler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.utilities.EmbedBuilders;
import alice.framework.utilities.EventUtilities;
import alice.modular.actions.MessageCreateAction;
import alice.modular.actions.MessageDeleteAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class BlacklistPassiveHandler extends MessageHandler {

	public BlacklistPassiveHandler() {
		super("Blacklist", false, PermissionProfile.getAnyonePreset());
		aliases.add("bl");
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getGuild().block().getId().asString());
		if( !guildData.has("blacklist_rules") ) {
			guildData.put("blacklist_rules", new JSONArray());
		}
		JSONArray blacklist = (JSONArray) guildData.getJSONArray("blacklist_rules");
		return !violated(event.getMessage().getContent(), blacklist).isEmpty();
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		Action response = new NullAction();
		AtomicSaveFile guildData = Brain.guildIndex.get(EventUtilities.getGuildId(event));
		JSONArray blacklist = (JSONArray) guildData.optJSONArray("blacklist_rules", new JSONArray());
		
		List<Integer> violations = violated(event.getMessage().getContent(), blacklist);
		
		response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getBlacklistConstructor(event.getMessage().getAuthor().get(), violations)));
		response.addAction(new MessageDeleteAction(event.getMessage()));
		response.toMono().block();
	}
	
	private List<Integer> violated( String message, JSONArray blacklist ) {
		List<Integer> violatedRules = new ArrayList<Integer>();
		
		for( int f=0; f<blacklist.length(); f++ ) {
			String rule = blacklist.getString(f);
			if( message.matches(rule) ) {
				violatedRules.add(f+1);
			}
		}
		
		return violatedRules;
	}
}
