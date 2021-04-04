package alice.modular.handlers;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.json.JSONArray;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.features.Documentable;
import alice.framework.handlers.CommandHandler;
import alice.framework.main.Brain;
import alice.framework.main.Constants;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.framework.utilities.EventUtilities;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

public class BlacklistCommandHandler extends CommandHandler implements Documentable {
	
	public BlacklistCommandHandler() {
		super("Blacklist", false, PermissionProfile.getAdminPreset().andNotDM());
		this.aliases.add("bl");
	}
	
	@Override
	protected void execute(MessageCreateEvent event) {
		Action response = new NullAction();
		Mono<MessageChannel> channel = event.getMessage().getChannel();
		Optional<User> user = event.getMessage().getAuthor();
		
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		List<String> tokens = ts.getTokens();
		
		
		if( tokens.size() == 1 || tokens.size() == 2 && !tokens.get(1).equalsIgnoreCase("rules") ) {
			response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getHelpConstructor(user, this)));
		} else {
			AtomicSaveFile guildData = Brain.guildIndex.get(EventUtilities.getGuildId(event));
			
			if( !guildData.has("blacklist_rules") ) {
				guildData.put("blacklist_rules", new JSONArray());
			}
			JSONArray blacklist = (JSONArray) guildData.getJSONArray("blacklist_rules");
			
			switch( tokens.get(1).toLowerCase() ) {
				case "rules":
					response.addAction(new MessageCreateAction(channel, getRulesConstructor(blacklist)));
					break;
				case "add":
					blacklist = guildData.modifyJSONArray("blacklist_rules", ja -> ja.put(tokens.get(2)));
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor("Rule added successfully.")));
					break;
				case "remove":
					int index = Integer.parseInt(tokens.get(2));
					if( index >= blacklist.length() ) {
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor("That rule does not exist!")));
						break;
					}
					blacklist = guildData.modifyJSONArray("blacklist_rules", ja -> ja.remove(index));
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor("Rule removed successfully.")));
					break;
				default:
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getHelpConstructor(user, this)));
					break;
			}
			
			response.toMono().block();
		}
	}
	
	@Override
	public String getCategory() {
		return Documentable.ADMIN.name();
	}

	@Override
	public String getDescription() {
		return "Allows server admins to blacklist certain messages in the server.\nThe rules are regex friendly, and can be done as regex patterns, if you know how!";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair(String.format("%s rules", invocation), "Displays the current banned patterns."),
			new DocumentationPair(String.format("%s add \"<pattern>\"", invocation), "Adds the given pattern to the list of banned patterns."),
			new DocumentationPair(String.format("%s remove <index>", invocation), "Removes the given rule from the list of rules, if possible.")
		};
	}
	
	@Override
	public DocumentationPair[] getExamples() {
		return new DocumentationPair[] {
			new DocumentationPair(String.format("%s add \"discord.gg/.*\"", invocation), "Bans messages containing discord server invitation links."),
			new DocumentationPair(String.format("%s remove 2", invocation), "Removes the rule indexed as 2 from the list of rules.")
		};
	}
	
	public static synchronized Consumer<EmbedCreateSpec> getRulesConstructor(JSONArray blacklist) {
		return c -> rulesConstructor(c, blacklist);
	}
	
	private static synchronized EmbedCreateSpec rulesConstructor( EmbedCreateSpec spec, JSONArray blacklist) {
		StringBuilder rules = new StringBuilder();
		for( int f=0; f<blacklist.length(); f++ ) {
			rules.append(String.format(":small_blue_diamond: **%d:** %s\n", f, EventUtilities.escapeMarkdown(blacklist.getString(f))));
		}
		
		spec.setAuthor(Constants.NAME, null, Brain.client.getSelf().block().getAvatarUrl());
		spec.setColor(Color.of(0, 0, 0));
		spec.addField("Blacklisted Patterns", rules.length() == 0 ? "No rules set!" : rules.toString(), false);
		return spec;
	}
	
}
