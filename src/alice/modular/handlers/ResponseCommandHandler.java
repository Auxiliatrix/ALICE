package alice.modular.handlers;

import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;

import alice.configuration.calibration.Constants;
import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.handlers.Documentable;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.framework.utilities.EventUtilities;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

public class ResponseCommandHandler extends CommandHandler implements Documentable {

	public ResponseCommandHandler() {
		super("Response", false, PermissionProfile.getAdminPreset().andNotDM());
		aliases.add("resp");
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getGuild().block().getId().asString());
		
		if( !guildData.has("responses") ) {
			guildData.put("responses", new JSONArray());
		}
		if( ts.size() < 2 ) {
			response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getHelpConstructor(event.getMessage().getAuthor(), this)));
		} else {
			switch( ts.get(1) ) {
				case "list":
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), getResponsesConstructor(guildData.getJSONArray("responses"))));
					break;
				case "add":
					if( ts.quotedOnly().size() < 2 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify a trigger and a response in quotes!", EmbedBuilders.ERR_USAGE)));
						break;
					}
					JSONObject item = new JSONObject();
					item.put("trigger", ts.quotedOnly().get(0));
					item.put("response", ts.quotedOnly().get(1));
					guildData.modifyJSONArray("responses", array -> array.put(item));
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Response added successfully!")));
					break;
				case "remove":
					if( ts.getNumbers().size() == 0 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify a response to remove!", EmbedBuilders.ERR_USAGE)));
						break;
					}
					if( guildData.getJSONArray("responses").length() < ts.getNumbers().get(0) ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("That response doesn't exist!", EmbedBuilders.ERR_USAGE)));
						break;
					}
					guildData.modifyJSONArray("responses", array -> array.remove(ts.getNumbers().get(0)));
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Response removed successfully!")));
					break;
				default:
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getHelpConstructor(event.getMessage().getAuthor(), this)));
					break;
			}
		}
		response.toMono().block();
	}

	@Override
	public String getCategory() {
		return ADMIN.name();
	}

	@Override
	public String getDescription() {
		return String.format("Programs %s to say a stock response to certain patterns.", Constants.NAME);
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
				new DocumentationPair(String.format("%s list", invocation), "Displays the current responses programmed."),
				new DocumentationPair(String.format("%s add \"<pattern>\", \"response\"", invocation), "Adds the given pattern and response to the list of responses."),
				new DocumentationPair(String.format("%s remove <index>", invocation), "Removes the given response from the list of responses, if possible.")
			};
	}
	
	public static synchronized Consumer<EmbedCreateSpec> getResponsesConstructor(JSONArray responses) {
		return c -> responsesConstructor(c, responses);
	}
	
	private static synchronized EmbedCreateSpec responsesConstructor( EmbedCreateSpec spec, JSONArray responses) {
		spec.setAuthor(Constants.NAME, null, Brain.client.getSelf().block().getAvatarUrl());
		spec.setColor(Color.of(253, 185, 200));
		spec.setTitle("Response Patterns");
		for( int f=0; f<responses.length(); f++ ) {
			JSONObject item = responses.getJSONObject(f);
			spec.addField(String.format(":small_blue_diamond: **[%d]:** `%s`", f, EventUtilities.escapeMarkdown(item.getString("trigger"))), String.format("%s: %s", Constants.NAME, item.getString("response")), false);
		}
		if( responses.length() == 0 ) {
			spec.setDescription("No response patterns specified.");
		}
		return spec;
	}
	
}
