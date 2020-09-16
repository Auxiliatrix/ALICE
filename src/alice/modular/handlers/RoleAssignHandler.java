package alice.modular.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.MentionHandler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.framework.utilities.MessageUtilities;
import alice.modular.actions.MessageCreateAction;
import alice.modular.actions.RoleAssignAction;
import alice.modular.actions.RoleUnassignAction;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class RoleAssignHandler extends MentionHandler {

	public static final String[] ADD_REQUEST = new String[] {
			"add",
			"give",
			"get",
			"request",
			"acquire",
			"obtain",
			"receive",
			"send",
			"grant",
			"bestow",
			"hand",
			"borrow",
			"lend"
	};
	
	public static final String[] REMOVE_REQUEST = new String[] {
			"take",
			"remove",
			"rid",
			"unget",
			"unacquire",
			"unobtain",
			"unreceive",
			"return",
			"unbestow",
			"unhand",
	};
	
	public RoleAssignHandler() {
		super("RoleAssign", "Default", false, PermissionProfile.getAnyonePreset());
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return new TokenizedString(event.getMessage().getContent()).containsTokenIgnoreCase("role");
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		
		AtomicSaveFile guildData = Brain.guildIndex.get(MessageUtilities.getGuildId(event));
		
		JSONArray allowRules = (JSONArray) guildData.optJSONArray("role_rules_allow", new JSONArray());
		JSONArray disallowRules = (JSONArray) guildData.optJSONArray("role_rules_disallow", new JSONArray());

		Mono<MessageChannel> channel = event.getMessage().getChannel();
		Optional<User> user = event.getMessage().getAuthor();
		
		if( ts.quotedOnly().size() == 0 ) {
			response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "You must specify a role in quotes!")));
		} else {
			String role = ts.quotedOnly().getTokens().get(0);
		
			if( ts.containsAnyTokensIgnoreCase(ADD_REQUEST) ) {
				if( !roleAllowed(role, allowRules, disallowRules) ) {
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "You do not have permission to modify that role!", "Permission Denied")));
				} else {
					Role foundRole = getRoleByName(event.getGuild().block(), role);
					if( foundRole == null ) {
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "That role doesn't exist!")));
					} else {
						response.addAction(new RoleAssignAction(event.getMessage().getAuthorAsMember(), foundRole));
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor(user, "Role assigned successfully!")));
					}
				}
			} else if( ts.containsAnyTokensIgnoreCase(REMOVE_REQUEST) ) {
				if( !roleAllowed(role, allowRules, disallowRules) ) {
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "You don't have permission to modify that role!", "Permission Denied")));
				} else {
					Role foundRole = getRoleByName(event.getGuild().block(), role);
					if( foundRole == null ) {
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "That role does not exist!")));
					} else {
						response.addAction(new RoleUnassignAction(event.getMessage().getAuthorAsMember(), foundRole));
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor(user, "Role unassigned successfully!")));
					}
				}
			}
		}
		return response;
	}
	
	private Role getRoleByName(Guild guild, String roleName) {
		Role foundRole = null;
		List<Snowflake> ids = new ArrayList<Snowflake>(guild.getRoleIds());
		for( Snowflake id : ids ) {
			Role role = guild.getRoleById(id).block();
			if( role.getName().equalsIgnoreCase(roleName) ) {
				foundRole = role;
			}
		}
		
		return foundRole;
	}
	
	private boolean roleAllowed( String roleName, JSONArray allowRules, JSONArray disallowRules ) {
		boolean allowed = false;
		for( Object allowRule : allowRules ) {
			if( roleName.matches((String) allowRule) ) {
				allowed = true;
				break;
			}
		}
		for( Object disallowRule : disallowRules ) {
			if( roleName.matches((String) disallowRule) ) {
				allowed = false;
				break;
			}
		}
		
		return allowed;
	}
	
}
