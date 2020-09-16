package alice.modular.handlers;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.json.JSONArray;

import alice.configuration.calibration.Constants;
import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.framework.utilities.MessageUtilities;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

public class RoleRuleCommandHandler extends CommandHandler {

	private static final String USAGE = "Proper usage:\n"
			+ "%rr allow <pattern>\n"
			+ "%rr disallow <pattern>\n"
			+ "%rr rules\n"
			+ "%rr remove <index>\n";
	
	public RoleRuleCommandHandler() {
		super("RoleRules", "Admin", false, PermissionProfile.getAdminPreset());
		this.aliases.add("rr");
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return true;
	}
	
	@Override
	protected Action execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		List<String> tokens = ts.getTokens();
		if( tokens.size() == 1 || tokens.size() == 2 && !tokens.get(1).equals("rules") ) {
			response.addAction(new MessageCreateAction(event.getMessage().getChannel(), USAGE));
			return response;
		}
		
		AtomicSaveFile guildData = Brain.guildIndex.get(MessageUtilities.getGuildId(event));
		
		JSONArray allowRules = (JSONArray) guildData.optJSONArray("role_rules_allow", new JSONArray());
		JSONArray disallowRules = (JSONArray) guildData.optJSONArray("role_rules_disallow", new JSONArray());

		Mono<MessageChannel> channel = event.getMessage().getChannel();
		Optional<User> user = event.getMessage().getAuthor();
		
		switch( tokens.get(1) ) {
			case "rules":
				response.addAction(new MessageCreateAction(channel, getRulesConstructor(allowRules, disallowRules)));
				return response;
			case "allow":
				allowRules.put(tokens.get(2));
				guildData.put("role_rules_allow", allowRules);
				response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor(user, "Rule added successfully.")));
				break;
			case "disallow":
				disallowRules.put(tokens.get(2));
				guildData.put("role_rules_disallow", disallowRules);
				response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor(user, "Rule added successfully.")));
				break;
			case "remove":
				if( tokens.get(2).length() < 2 ) {
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "That rule does not exist!")));
					return response;
				}
				
				switch( tokens.get(2).charAt(0) ) {
					case 'A':
						int index = Integer.parseInt(tokens.get(2).substring(1));
						if( index >= allowRules.length() ) {
							response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "That rule does not exist!")));
							return response;
						}
						
						allowRules.remove(index);
						guildData.put("role_rules_allow", allowRules);
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor(user, "Rule removed successfully.")));
						break;
					case 'D':
						index = Integer.parseInt(tokens.get(2).substring(1));
						if( index >= allowRules.length() ) {
							response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "That rule does not exist!")));
							return response;
						}
						
						disallowRules.remove(index);
						guildData.put("role_rules_disallow", disallowRules);
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor(user, "Rule removed successfully.")));
						break;
					default:
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "That rule does not exist!")));
						return response;
				}
				break;
			default:
				response.addAction(new MessageCreateAction(channel, USAGE));
				return response;
		}
		
		return response;
	}
	
	public static synchronized Consumer<EmbedCreateSpec> getRulesConstructor(JSONArray allowRules, JSONArray disallowRules) {
		return c -> rulesConstructor(c, allowRules, disallowRules);
	}
	
	private static synchronized EmbedCreateSpec rulesConstructor( EmbedCreateSpec spec, JSONArray allowRules, JSONArray disallowRules ) {
		StringBuilder allowList = new StringBuilder();
		for( int f=0; f<allowRules.length(); f++ ) {
			allowList.append(String.format(":small_blue_diamond: **A%d:** %s\n", f, MessageUtilities.escapeMarkdown(allowRules.getString(f))));
		}
		
		StringBuilder disallowList = new StringBuilder();
		for( int f=0; f<disallowRules.length(); f++ ) {
			disallowList.append(String.format(":small_orange_diamond: **D%d:** %s\n", f, MessageUtilities.escapeMarkdown(disallowRules.getString(f))));
		}
		
		spec.setAuthor(Constants.NAME, null, Brain.client.getSelf().block().getAvatarUrl());
		spec.setColor(Color.of(253, 185, 200));
		spec.addField("Allowed Patterns", allowList.length() == 0 ? "No rules set!" : allowList.toString(), false);
		spec.addField("Disallowed Patterns", disallowList.length() == 0 ? "No rules set!" : disallowList.toString(), false);
		return spec;
	}
}
