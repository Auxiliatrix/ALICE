package alice.modular.handlers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.main.Brain;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.modular.actions.AssignRoleAction;
import alice.modular.actions.UnassignRoleAction;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;

public class RoleAssignHandler extends CommandHandler {

	private static final String USAGE = "Proper usage:\n"
			+ "%role allow <pattern>\n"
			+ "%role disallow <pattern>\n"
			+ "%role rules\n"
			+ "%role removeRule <index>\n"
			+ "%role get <role>\n"
			+ "%role unget <role>";
	
	public RoleAssignHandler() {
		super("Role", "Default", false);
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return true;
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
	
	@Override
	protected Action execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		List<String> tokens = ts.getTokens();
		if( tokens.size() == 1 || tokens.size() == 2 && !tokens.get(1).equals("rules") ) {
			response.addCreateMessageAction(event.getMessage().getChannel(), USAGE);
			return response;
		}
		
		JSONArray allowRules = (JSONArray) Brain.guildIndex.get().get(event.getGuild().block().getId().asString()).getOrDefault("role_rules_allow", new JSONArray());
		JSONArray disallowRules = (JSONArray) Brain.guildIndex.get().get(event.getGuild().block().getId().asString()).getOrDefault("role_rules_disallow", new JSONArray());

		switch( tokens.get(1) ) {
			case "get":
				if( !roleAllowed(tokens.get(2), allowRules, disallowRules) ) {
					response.addCreateMessageAction(event.getMessage().getChannel(), "You do not have permission to modify that role.");
					return response;
				}
				
				Role foundRole = getRoleByName(event.getGuild().block(), tokens.get(2));
				if( foundRole == null ) {
					response.addCreateMessageAction(event.getMessage().getChannel(), "That role does not exist!");
					return response;
				}
				
				response.addAction(new AssignRoleAction(event.getMessage().getAuthorAsMember(), foundRole));
				response.addCreateMessageAction(event.getMessage().getChannel(), "Role assigned successfully!");
				break;
			case "unget":
				if( !roleAllowed(tokens.get(2), allowRules, disallowRules) ) {
					response.addCreateMessageAction(event.getMessage().getChannel(), "You do not have permission to modify that role.");
					return response;
				}
				
				foundRole = getRoleByName(event.getGuild().block(), tokens.get(2));
				if( foundRole == null ) {
					response.addCreateMessageAction(event.getMessage().getChannel(), "That role does not exist!");
					return response;
				}
				
				response.addAction(new UnassignRoleAction(event.getMessage().getAuthorAsMember(), foundRole));
				response.addCreateMessageAction(event.getMessage().getChannel(), "Role unassigned successfully!");
				break;
			case "rules":
				String rulesString = "";
				rulesString += "Allowed Rules:\n";
				for( int f=0; f<allowRules.length(); f++ ) {
					rulesString += "A" + f + ": " + (String) allowRules.get(f) + "\n";
				}
				
				rulesString += "Disallowed Rules:\n";
				for( int f=0; f<disallowRules.length(); f++ ) {
					rulesString += "D" + f + ": " + (String) disallowRules.get(f) + "\n";
				}
				
				response.addCreateMessageAction(event.getMessage().getChannel(), rulesString);
				return response;
			case "allow":
				if( !PermissionProfile.getAdminPreset().verify(event.getMessage().getAuthorAsMember()) ) {
					response.addCreateMessageAction(event.getMessage().getChannel(), "You must be an administrator to perform this action.");
					return response;
				}
				
				allowRules.put(tokens.get(2));
				Brain.guildIndex.get().get(event.getGuild().block().getId().asString()).put("role_rules_allow", allowRules);
				response.addCreateMessageAction(event.getMessage().getChannel(), "Rule added successfully.");
				break;
			case "disallow":
				if( !PermissionProfile.getAdminPreset().verify(event.getMessage().getAuthorAsMember()) ) {
					response.addCreateMessageAction(event.getMessage().getChannel(), "You must be an administrator to perform this action.");
					return response;
				}
				
				disallowRules.put(tokens.get(2));
				Brain.guildIndex.get().get(event.getGuild().block().getId().asString()).put("role_rules_disallow", disallowRules);
				response.addCreateMessageAction(event.getMessage().getChannel(), "Rule added successfully.");
				break;
			case "removeRule":
				if( !PermissionProfile.getAdminPreset().verify(event.getMessage().getAuthorAsMember()) ) {
					response.addCreateMessageAction(event.getMessage().getChannel(), "You must be an administrator to perform this action.");
					return response;
				}
				
				if( tokens.get(2).length() < 2 ) {
					response.addCreateMessageAction(event.getMessage().getChannel(), "That rule doesn't exist.");
					return response;
				}
				
				switch( tokens.get(2).charAt(0) ) {
					case 'A':
						int index = Integer.parseInt(tokens.get(2).substring(1));
						if( index >= allowRules.length() ) {
							response.addCreateMessageAction(event.getMessage().getChannel(), "That rule doesn't exist.");
							return response;
						}
						
						allowRules.remove(index);
						Brain.guildIndex.get().get(event.getGuild().block().getId().asString()).put("role_rules_allow", allowRules);
						response.addCreateMessageAction(event.getMessage().getChannel(), "Rule removed successfully.");
						break;
					case 'D':
						index = Integer.parseInt(tokens.get(2).substring(1));
						if( index >= allowRules.length() ) {
							response.addCreateMessageAction(event.getMessage().getChannel(), "That rule doesn't exist.");
							return response;
						}
						
						disallowRules.remove(index);
						Brain.guildIndex.get().get(event.getGuild().block().getId().asString()).put("role_rules_disallow", disallowRules);
						response.addCreateMessageAction(event.getMessage().getChannel(), "Rule removed successfully.");
						break;
					default:
						response.addCreateMessageAction(event.getMessage().getChannel(), "That rule doesn't exist.");
						return response;
				}
				break;
			default:
				response.addCreateMessageAction(event.getMessage().getChannel(), USAGE);
				return response;
		}
		
		return response;
	}

}
