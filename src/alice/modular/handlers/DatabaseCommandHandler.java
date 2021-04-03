package alice.modular.handlers;

import org.json.JSONObject;

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

public class DatabaseCommandHandler extends CommandHandler implements Documentable {

	public DatabaseCommandHandler() {
		super("Database", false, PermissionProfile.getDeveloperPreset());
		aliases.add("db");
	}
	
	@Override
	protected void execute(MessageCreateEvent event) {
		Action response = new NullAction();
		
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		AtomicSaveFile guildData = Brain.guildIndex.get(EventUtilities.getGuildId(event));
		
		int size = ts.size();
		if( size == 1 ) {
			response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getHelpConstructor(event.getMessage().getAuthor(), this)));
		} else if( size == 2 ) {
			if( guildData.has(ts.get(1)) ) {
				response.addAction(new MessageCreateAction(event.getMessage().getChannel(), JSONObject.valueToString(guildData.get(ts.get(1)))));
			} else {
				response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor(String.format("Key `%s` not found!", ts.get(1)), EmbedBuilders.ERR_USAGE)));
			}
		} else {
			if( guildData.has(ts.get(1)) ) {
				JSONObject current = guildData.getJSONObject(ts.get(1));
				for( int f=2; f<size-1; f++ ) {
					if( current.has(ts.get(f)) ) {
						current = current.getJSONObject(ts.get(f));
					} else {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor(String.format("Key `%s` not found!", ts.get(f)), EmbedBuilders.ERR_USAGE)));
						break;
					}
				}
				if( current.has(ts.get(size-1)) ) {
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), JSONObject.valueToString(current.get(ts.get(size-1)))));
				} else {
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor(String.format("Key `%s` not found!", ts.get(size-1)), EmbedBuilders.ERR_USAGE)));
				}
			} else {
				response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor(String.format("Key `%s` not found!", ts.get(1)), EmbedBuilders.ERR_USAGE)));
			}
		}
		
		response.toMono().block();
	}
	
	@Override
	public String getCategory() {
		return "DEVELOPER";
	}

	@Override
	public String getDescription() {
		return "Allows developers to navigate AL|CE's database through discord.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
				new DocumentationPair(String.format("%s <key...>", invocation), "Returns the value associated with the keys specified.")
		};
	}

}
