package alice.modular.handlers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import alice.configuration.references.Keywords;
import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.Documentable;
import alice.framework.handlers.MentionHandler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.framework.utilities.EventUtilities;
import alice.framework.utilities.StringUtilities;
import alice.modular.actions.MessageCreateAction;
import alice.modular.actions.MessageDeleteAction;
import alice.modular.actions.RoleAssignAction;
import alice.modular.actions.RoleUnassignAction;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class RoleAssignHandler extends MentionHandler implements Documentable {
	
	public RoleAssignHandler() {
		super("RoleAssign", false, PermissionProfile.getAnyonePreset().andNotDM());
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return new TokenizedString(event.getMessage().getContent()).containsIgnoreCase("role");
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		
		AtomicSaveFile guildData = Brain.guildIndex.get(EventUtilities.getGuildId(event));
		
		JSONArray allowRules = (JSONArray) guildData.optJSONArray("role_rules_allow", new JSONArray());
		JSONArray disallowRules = (JSONArray) guildData.optJSONArray("role_rules_disallow", new JSONArray());

		Mono<MessageChannel> channel = event.getMessage().getChannel();
		
		if( ts.quotedOnly().size() == 0 ) {
			response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor("You must specify a role in quotes!")));
		} else {
			String role = ts.quotedOnly().getTokens().get(0);
			if( ts.containsAnyTokensIgnoreCase(Keywords.REMOVE_REQUEST) ) {
				Role foundRole = getRoleByName(event.getGuild().block(), role);
				if( foundRole == null ) {
					Role closestRole = getClosestRoleByName(event.getGuild().block(), role, Integer.MAX_VALUE, allowRules, disallowRules);
					if( closestRole == null ) {
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor("That role does not exist!")));
					} else {
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(String.format("Did you mean: `%s`?", closestRole.getName()), "Role not found")));
					}
				} else {
					if( !roleAllowed(role, allowRules, disallowRules) ) {
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor("You don't have permission to modify that role!", EmbedBuilders.ERR_PERMISSION)));
					} else {
						if( event.getMessage().getUserMentions().blockFirst() != null && PermissionProfile.hasPermission(event.getMessage().getAuthor(), event.getGuild(), Permission.ADMINISTRATOR) ) {
							for( User user : event.getMessage().getUserMentions().collectList().block() ) {
								response.addAction(new RoleUnassignAction(user.asMember(event.getGuildId().get()), foundRole));
							}
							response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor(String.format("Role \"%s\" forcefully unassigned!", foundRole.getName()))));
							response.addAction(new MessageDeleteAction(event.getMessage()));
						} else {
							response.addAction(new RoleUnassignAction(event.getMessage().getAuthorAsMember(), foundRole));
							response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor(String.format("Role \"%s\" unassigned from @%s successfully!", foundRole.getName(), event.getMember().get().getDisplayName()))));
							response.addAction(new MessageDeleteAction(event.getMessage()));
						}
					}
				}
			} else if( ts.containsAnyTokensIgnoreCase(Keywords.ADD_REQUEST) ) {
				Role foundRole = getRoleByName(event.getGuild().block(), role);
				if( foundRole == null ) {
					Role closestRole = getClosestRoleByName(event.getGuild().block(), role, Integer.MAX_VALUE, allowRules, disallowRules);
					if( closestRole == null ) {
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor("That role does not exist!", "Role not found")));
					} else {
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(String.format("Did you mean: `%s`?", closestRole.getName()), "Role not found")));
					}				} else {
					if( !roleAllowed(role, allowRules, disallowRules) ) {
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor("You do not have permission to modify that role!", EmbedBuilders.ERR_PERMISSION)));
					} else {
						if( event.getMessage().getUserMentions().blockFirst() != null && PermissionProfile.hasPermission(event.getMessage().getAuthor(), event.getGuild(), Permission.ADMINISTRATOR) ) {
							for( User user : event.getMessage().getUserMentions().collectList().block() ) {
								response.addAction(new RoleAssignAction(user.asMember(event.getGuildId().get()), foundRole));
							}
							response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor(String.format("Role \"%s\" forcefully assigned!", foundRole.getName()))));
							response.addAction(new MessageDeleteAction(event.getMessage()));
						} else {
							response.addAction(new RoleAssignAction(event.getMessage().getAuthorAsMember(), foundRole));
							response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor(String.format("Role \"%s\" assigned to @%s successfully!", foundRole.getName(), event.getMember().get().getDisplayName()))));
							response.addAction(new MessageDeleteAction(event.getMessage()));
						}
					}
				}
			}
		}
		response.toMono().block();
	}
	
	@Override
	public String getCategory() {
		return Documentable.DEFAULT.name();
	}

	@Override
	public String getDescription() {
		return "Allows users to assign and unassign themselves roles that have been preapproved by the server admins.\n"
				+ "This is a smart module, and will still work even if your message doesn't look exactly like it does in the help documentation.\n"
				+ "Make sure to put this bot's name somewhere in the message, so she knows you're talking to her!";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair("Alice, give me the \"Gamer\" role.", "Assigns the user the role specified in quotes, if possible and/or allowed."),
			new DocumentationPair("Hey alice, could you remove my \"Student\" role?", "Removes the role specified in quotes from the user, if possible and/or allowed.")
		};
	}
	
	public static Role getRoleByName(Guild guild, String roleName) {
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
	
	public static Role getClosestRoleByName(Guild guild, String roleName, int distance, JSONArray allowRules, JSONArray disallowRules) {
		Role foundRole = null;
		List<Snowflake> ids = new ArrayList<Snowflake>(guild.getRoleIds());
		int minDist = Integer.MAX_VALUE;
		for( Snowflake id : ids ) {
			Role role = guild.getRoleById(id).block();
			int newMinDist = StringUtilities.levenshteinDistance(role.getName().toLowerCase(), roleName.toLowerCase());
			if( newMinDist < minDist && newMinDist < distance && roleAllowed(roleName, allowRules, disallowRules)) {
				foundRole = role;
				minDist = newMinDist;
			}
		}
		
		return foundRole;
	}
	
	public static boolean roleAllowed( String roleName, JSONArray allowRules, JSONArray disallowRules ) {
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
