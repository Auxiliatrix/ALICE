package alice.modular.features;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import alice.framework.database.SharedJSONArray;
import alice.framework.database.SharedSaveFile;
import alice.framework.features.MessageFeature;
import alice.framework.structures.TokenizedString;
import alice.framework.structures.TokenizedString.Token;
import alice.framework.tasks.DependentStacker;
import alice.framework.tasks.MultipleDependentStacker;
import alice.framework.tasks.Stacker;
import alice.framework.utilities.EmbedBuilders;
import alice.framework.utilities.EventUtilities;
import alice.modular.tasks.EmbedSendTask;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

public class RoleAssignFeature extends MessageFeature {

	public static final String ALLOW_ROLES_KEY = ".role_allow_roles";
	public static final String ALLOW_RULES_KEY = ".role_allow_rules";
	public static final String DENY_ROLES_KEY = ".role_deny_roles";
	public static final String DENY_RULES_KEY = ".role_deny_rules";
	
	
	public RoleAssignFeature() {
		super("Role");
		withCheckInvoked();
	}

	@Override
	protected boolean condition(MessageCreateEvent event) {
		return true;
	}

	private boolean allowed(String roleName, Set<String> allowedRoles, Set<String> deniedRoles, List<Matcher> allowedRules, List<Matcher> deniedRules) {
		if( deniedRoles.contains(roleName.toLowerCase()) ) {
			return false;
		}
		for( Matcher matcher : deniedRules ) {
			matcher.reset(roleName);
			if( matcher.matches() ) {
				return false;
			}
		}
		if( allowedRoles.contains(roleName.toLowerCase()) ) {
			return true;
		}
		for( Matcher matcher : allowedRules ) {
			matcher.reset(roleName);
			if( matcher.matches() ) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Mono<?> respond(MessageCreateEvent type) {
		Stacker response = new Stacker();
		
		String message = type.getMessage().getContent();
		TokenizedString ts = new TokenizedString(message);
		
		DependentStacker<MessageChannel> channelStacker = new DependentStacker<MessageChannel>(type.getMessage().getChannel());
		DependentStacker<Member> memberStacker = new DependentStacker<Member>(type.getMessage().getAuthorAsMember());
		MultipleDependentStacker roleStacker = new MultipleDependentStacker(type.getMember().get().getRoles().collectList(), type.getMessage().getChannel());
		MultipleDependentStacker guildRoleStacker = new MultipleDependentStacker(type.getGuild().flatMap(g -> g.getRoles().collectList()), type.getMessage().getChannel());		
		
		SharedSaveFile ssf = new SharedSaveFile(type.getGuildId().get().asLong());
		SharedJSONArray allowRules = ssf.getOrDefaultSharedJSONArray(ALLOW_RULES_KEY);
		SharedJSONArray allowRoles = ssf.getOrDefaultSharedJSONArray(ALLOW_ROLES_KEY);
		SharedJSONArray denyRules = ssf.getOrDefaultSharedJSONArray(DENY_RULES_KEY);
		SharedJSONArray denyRoles = ssf.getOrDefaultSharedJSONArray(DENY_ROLES_KEY);
				
		Set<String> allowedRoles = new HashSet<String>();
		Set<String> deniedRoles = new HashSet<String>();
		
		List<Matcher> allowedRules = new ArrayList<Matcher>();
		List<Matcher> deniedRules = new ArrayList<Matcher>();
		
		for( Object o : allowRoles ) {
			allowedRoles.add(((String) o).toLowerCase());
		}
		for( Object o : denyRoles ) {
			deniedRoles.add(((String) o).toLowerCase());
		}
		
		for( Object o : allowRules ) {
			allowedRules.add(Pattern.compile((String) o).matcher(""));
		}
		for( Object o : denyRules ) {
			deniedRules.add(Pattern.compile((String) o).matcher(""));
		}
		
		if( ts.size() > 1 ) {
			switch( ts.getString(1).toLowerCase() ) {
				case "get":
					if( ts.size() > 2 ) {
						List<String> addedRoles = new ArrayList<String>();
						List<String> blockedRoles = new ArrayList<String>();
						TokenizedString sub = ts.getSubTokens(2);
						for( Token token : sub.getTokens() ) {
							if( allowed(token.getContent(), allowedRoles, deniedRoles, allowedRules, deniedRules) ) {
								addedRoles.add(token.getContent().toLowerCase());
							} else {
								blockedRoles.add(token.getContent().toLowerCase());
							}
						}
						if( blockedRoles.isEmpty() ) {
							guildRoleStacker.addTask(a -> {
								Set<String> originalAddedRolesSet = new HashSet<String>(addedRoles);
								Set<String> addedRolesSet = new HashSet<String>(addedRoles);
								List<Role> rolesToAdd = new ArrayList<Role>();
								for( Role role : (List<Role>) a.get(0) ) {
									if( originalAddedRolesSet.contains(role.getName().toLowerCase()) ) {
										rolesToAdd.add(role);
										addedRolesSet.remove(role.getName().toLowerCase());
									}
								}
								if( addedRolesSet.size() > 0 ) {
									String missed = String.join(", ", addedRolesSet);
									return (new EmbedSendTask(spec -> EmbedBuilders.applyErrorFormat(spec, String.format("I could not find the following roles: `%s`!", missed)))).apply((MessageChannel) a.get(1));
								} else {
									Stacker tasks = new Stacker();
									List<String> addedRoleNames = new ArrayList<String>();
									for( Role role : rolesToAdd ) {
										addedRoleNames.add(role.getName());
										tasks.append(type.getMember().get().addRole(role.getId()));
									}
									String added = String.join(", ", addedRoleNames);
									tasks.append((new EmbedSendTask(spec -> EmbedBuilders.applySuccessFormat(spec, String.format("I've given you the following roles: `%s`!", added)))).apply((MessageChannel) a.get(1)));
									return tasks.toMono();
								}
							});
						} else {
							String blocked = String.join(", ", blockedRoles);
							channelStacker.addTask(new EmbedSendTask(spec -> EmbedBuilders.applyErrorFormat(spec, String.format("You cannot manipulate the following roles: `%s`!", blocked))));
						}
					} else {
						channelStacker.addTask(new EmbedSendTask(spec -> EmbedBuilders.applyErrorFormat(spec, "You have to tell me which roles you want! You can do `%role list` or `%role rules` for some ideas.", EmbedBuilders.ERR_USAGE)));
					}
					break;
				case "rem":
					if( ts.size() > 2 ) {
						List<String> removedRoles = new ArrayList<String>();
						List<String> blockedRoles = new ArrayList<String>();
						TokenizedString sub = ts.getSubTokens(2);
						for( Token token : sub.getTokens() ) {
							if( allowed(token.getContent(), allowedRoles, deniedRoles, allowedRules, deniedRules) ) {
								removedRoles.add(token.getContent().toLowerCase());
							} else {
								blockedRoles.add(token.getContent());
							}
						}
						if( blockedRoles.isEmpty() ) {
							roleStacker.addTask(a -> {
								Set<String> originalRemovedRolesSet = new HashSet<String>(removedRoles);
								Set<String> removedRolesSet = new HashSet<String>(removedRoles);
								List<Role> rolesToRemove = new ArrayList<Role>();
								for( Role role : (List<Role>) a.get(0) ) {
									if( originalRemovedRolesSet.contains(role.getName().toLowerCase()) ) {
										rolesToRemove.add(role);
										removedRolesSet.remove(role.getName().toLowerCase());
									}
								}
								if( removedRolesSet.size() > 0 ) {
									String missed = String.join(", ", removedRolesSet);
									return (new EmbedSendTask(spec -> EmbedBuilders.applyErrorFormat(spec, String.format("You don't have the following roles: `%s`!", missed)))).apply((MessageChannel) a.get(1));
								} else {
									Stacker tasks = new Stacker();
									List<String> removedRoleNames = new ArrayList<String>();
									for( Role role : rolesToRemove ) {
										removedRoleNames.add(role.getName());
										tasks.append(type.getMember().get().removeRole(role.getId()));
									}
									String added = String.join(", ", removedRoleNames);
									tasks.append((new EmbedSendTask(spec -> EmbedBuilders.applySuccessFormat(spec, String.format("I've taken away the following roles: `%s`!", added)))).apply((MessageChannel) a.get(1)));
									return tasks.toMono();
								}
							});
						} else {
							String blocked = String.join(", ", blockedRoles);
							channelStacker.addTask(new EmbedSendTask(spec -> EmbedBuilders.applyErrorFormat(spec, String.format("You cannot manipulate the following roles: `%s`!", blocked))));
						}
					} else {
						channelStacker.addTask(new EmbedSendTask(spec -> EmbedBuilders.applyErrorFormat(spec, "You have to tell me which roles you want me to get rid of!", EmbedBuilders.ERR_USAGE)));
					}
					break;
				case "getall":
					break;
				case "remall":
					break;
				case "give":
					if( ts.size() > 3 ) {
						
					} else {
						channelStacker.addTask(new EmbedSendTask(spec -> EmbedBuilders.applyErrorFormat(spec, "You have to tell me who you want to give the roles to, and which roles to give them!", EmbedBuilders.ERR_USAGE)));
					}
					break;
				case "take":
					if( ts.size() > 3 ) {
						
					} else {
						channelStacker.addTask(new EmbedSendTask(spec -> EmbedBuilders.applyErrorFormat(spec, "You have to tell me who you want to take the roles from, and which roles to take!", EmbedBuilders.ERR_USAGE)));
					}
					break;
				case "clear":
					break;
				case "list":
					List<SimpleEntry<String, String>> roleEntries = new ArrayList<SimpleEntry<String, String>>();
					
					StringBuilder allowRolesList = new StringBuilder();
					for( int f=0; f<allowRoles.length(); f++ ) {
						allowRolesList.append(String.format(":small_blue_diamond: **AR%d:** %s\n", f, EventUtilities.escapeMarkdown(allowRoles.getString(f))));
					}
					
					StringBuilder denyRolesList = new StringBuilder();
					for( int f=0; f<denyRoles.length(); f++ ) {
						denyRolesList.append(String.format(":small_orange_diamond: **DR%d:** %s\n", f, EventUtilities.escapeMarkdown(denyRoles.getString(f))));
					}
					
					roleEntries.add(new SimpleEntry<String, String>("Allowed Roles", allowRolesList.toString()));
					roleEntries.add(new SimpleEntry<String, String>("Denied Roles", denyRolesList.toString()));

					channelStacker.addTask(new EmbedSendTask(spec -> EmbedBuilders.applyListFormat(spec, "Role List", Color.of(253, 185, 200), roleEntries, false, true)));
					break;
				case "rules":
					if( ts.size() > 2 ) {
						switch( ts.getString(2).toLowerCase() ) {
							case "allow":
								if( ts.size() > 3 ) {
									TokenizedString sub = ts.getSubTokens(3);
									for( Token token : sub.getTokens() ) {
										if( token.isCoded() ) {
											response.append(() -> allowRules.put(token.getContent()));
										} else {
											response.append(() -> allowRoles.put(token.getContent()));
										}
									}
									channelStacker.addTask(m -> new EmbedSendTask(c -> EmbedBuilders.applySuccessFormat(c, "Role rules added succesfully!")).apply(m));
								} else {
									channelStacker.addTask(new EmbedSendTask(spec -> EmbedBuilders.applyErrorFormat(spec, "You have to specify at least one rule or regex pattern!", EmbedBuilders.ERR_USAGE)));
								}
								break;
							case "deny":
								if( ts.size() > 3 ) {
									TokenizedString sub = ts.getSubTokens(3);
									for( Token token : sub.getTokens() ) {
										if( token.isCoded() ) {
											response.append(() -> denyRules.put(token.getContent()));
										} else {
											response.append(() -> denyRoles.put(token.getContent()));
										}
									}
									channelStacker.addTask(m -> new EmbedSendTask(c -> EmbedBuilders.applySuccessFormat(c, "Role rules added succesfully!")).apply(m));
								} else {
									channelStacker.addTask(new EmbedSendTask(spec -> EmbedBuilders.applyErrorFormat(spec, "You have to specify at least one rule or regex pattern!", EmbedBuilders.ERR_USAGE)));
								}
								break;
							case "delete":
								if( ts.size() > 3 ) {
									TokenizedString sub = ts.getSubTokens(4);

								} else {
									//  TODO: syntax error
								}
								break;
							default:
								channelStacker.addTask(new EmbedSendTask(spec -> EmbedBuilders.applyErrorFormat(spec, "I don't recognize that subcommand!", EmbedBuilders.ERR_USAGE)));
								break;
						}
					} else {
						List<SimpleEntry<String, String>> entries = new ArrayList<SimpleEntry<String, String>>();
						
						StringBuilder allowRulesList = new StringBuilder();
						for( int f=0; f<allowRules.length(); f++ ) {
							allowRulesList.append(String.format(":small_blue_diamond: **AP%d:** %s\n", f, EventUtilities.escapeMarkdown(allowRules.getString(f))));
						}
						
						StringBuilder denyRulesList = new StringBuilder();
						for( int f=0; f<denyRules.length(); f++ ) {
							denyRulesList.append(String.format(":small_orange_diamond: **DP%d:** %s\n", f, EventUtilities.escapeMarkdown(denyRules.getString(f))));
						}
						
						entries.add(new SimpleEntry<String, String>("Allowed Patterns", allowRulesList.toString()));
						entries.add(new SimpleEntry<String, String>("Denied Patterns", denyRulesList.toString()));
						
						channelStacker.addTask(new EmbedSendTask(spec -> EmbedBuilders.applyListFormat(spec, "Role Patterns", Color.of(253, 185, 200), entries, false, true)));
					}
					break;
				default:
					channelStacker.addTask(new EmbedSendTask(spec -> EmbedBuilders.applyErrorFormat(spec, "I don't recognize that subcommand!", EmbedBuilders.ERR_USAGE)));
					break;
			}
		} else {
			// TODO: help message
		}
				
		response.append(channelStacker);
		response.append(memberStacker);
		response.append(roleStacker);
		response.append(guildRoleStacker);
		
		return response.toMono();
	}
	
}
