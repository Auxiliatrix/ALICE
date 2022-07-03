package alice.modular.modules;

import java.util.List;

import alice.framework.database.SyncedJSONArray;
import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyManager;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.modules.MessageModule;
import alice.framework.structures.TokenizedString;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public class RoleAssignModule extends MessageModule {

	public RoleAssignModule() {
		super();
	}

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent,MessageChannel> mcef = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent,Member> mef = dfb.addWrappedDependency(mce -> mce.getMember().get());
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getInvokedCondition("%role"));
		
		Command<MessageCreateEvent> sideCommand = new Command<MessageCreateEvent>(df);
		sideCommand.withSideEffect(
			mce -> {
				SyncedJSONObject sf = SyncedSaveFile.ofGuild(mce.getGuildId().get().asLong());
				if( !sf.has("%role_allowed") ) {
					sf.putJSONArray("%role_allowed");
				}
				if( !sf.has("%role_disallowed") ) {
					sf.putJSONArray("%role_disallowed");
				}
			}
		);
		
		DependencyManager<MessageCreateEvent,List<Role>> lref = dfb.addDependency(mce -> mce.getGuild().flatMap(g -> g.getRoles().collectList()));
		DependencyManager<MessageCreateEvent,List<Role>> luref = dfb.addDependency(mce -> mce.getMember().get().getRoles().collectList());
		DependencyFactory<MessageCreateEvent> dfs = dfb.build();
		
		Command<MessageCreateEvent> getCommand = new Command<MessageCreateEvent>(dfs);
		getCommand.withCondition(MessageModule.getArgumentCondition(1, "add"));
		getCommand.withCondition(MessageModule.getArgumentsCondition(3));
		getCommand.withDependentEffect(
			d -> {
				MessageChannel mc = mcef.requestFrom(d);
				Member m = mef.requestFrom(d);
				List<Role> lr = lref.requestFrom(d);
				MessageCreateEvent mce = d.getEvent();
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				
				SyncedJSONObject sf = SyncedSaveFile.ofGuild(m.getGuildId().asLong());
				SyncedJSONArray allowed = sf.getJSONArray("%role_allowed");
				SyncedJSONArray disallowed = sf.getJSONArray("%role_disallowed");
							
				for( Object da : disallowed.toList() ) {
					if( da.toString().equalsIgnoreCase(ts.getString(2)) ) {
						return mc.createMessage("Sorry, you're not allowed to have that role!");
					}
				}
				for( Object a : allowed.toList() ) {
					if( a.toString().equalsIgnoreCase(ts.getString(2)) ) {
						Role role = findRoleByName(lr, ts.getString(2));
						if( role != null ) {
							return m.addRole(role.getId())
									.and(mce.getMessage().addReaction(ReactionEmoji.unicode("\u2705")));
						} else {
							return mc.createMessage("Sorry, it looks like that role doesn't exist!");
						}
					}
				}
				return mc.createMessage("Sorry, I don't recognize that as an assignable role!");
			}
		);
		
		Command<MessageCreateEvent> removeCommand = new Command<MessageCreateEvent>(dfs);
		removeCommand.withCondition(MessageModule.getArgumentCondition(1, "remove"));
		removeCommand.withCondition(MessageModule.getArgumentsCondition(3));
		removeCommand.withDependentEffect(
			d -> {
				MessageChannel mc = mcef.requestFrom(d);
				Member m = mef.requestFrom(d);
				List<Role> lr = lref.requestFrom(d);
				List<Role> lur = luref.requestFrom(d);
				MessageCreateEvent mce = d.getEvent();
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				
				SyncedJSONObject sf = SyncedSaveFile.ofGuild(m.getGuildId().asLong());
				SyncedJSONArray allowed = sf.getJSONArray("%role_allowed");
				SyncedJSONArray disallowed = sf.getJSONArray("%role_disallowed");
				
				for( Object da : disallowed.toList() ) {
					if( da.toString().equalsIgnoreCase(ts.getString(2)) ) {
						return mc.createMessage("Sorry, you're not allowed to remove that role!");
					}
				}
				for( Object a : allowed.toList() ) {
					if( a.toString().equalsIgnoreCase(ts.getString(2)) ) {
						Role role = findRoleByName(lr, ts.getString(2));
						if( role != null ) {
							Role urole = findRoleByName(lur, ts.getString(2));
							if( urole != null ) {
								return m.removeRole(role.getId())
										.and(mce.getMessage().addReaction(ReactionEmoji.unicode("\u2705")));
							} else {
								return mc.createMessage("Sorry, that doesn't seem to be a role that you have!");
							}
						} else {
							return mc.createMessage("Sorry, it looks like that role doesn't exist!");
						}
					}
				}
				return mc.createMessage("Sorry, I don't recognize that as an unassignable role!");
			}
		);
		
		DependencyManager<MessageCreateEvent,PermissionSet> psef = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		DependencyFactory<MessageCreateEvent> dfa = dfb.build();
		
		Command<MessageCreateEvent> allowCommand = new Command<MessageCreateEvent>(dfa);
		allowCommand.withCondition(MessageModule.getArgumentCondition(1, "allow"));
		allowCommand.withCondition(MessageModule.getArgumentsCondition(4));
		allowCommand.withDependentCondition(MessageModule.getPermissionCondition(psef, Permission.ADMINISTRATOR));
		
		Command<MessageCreateEvent> allowAddCommand = new Command<MessageCreateEvent>(dfa);
		allowAddCommand.withCondition(MessageModule.getArgumentCondition(2, "add"));
		allowAddCommand.withDependentEffect(
			d -> {
				MessageChannel mc = mcef.requestFrom(d);
				Member m = mef.requestFrom(d);
				List<Role> lr = lref.requestFrom(d);
	
				MessageCreateEvent mce = d.getEvent();
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				SyncedJSONObject sf = SyncedSaveFile.ofGuild(m.getGuildId().asLong());
				
				SyncedJSONArray allowed = sf.getJSONArray("%role_allowed");
				SyncedJSONArray disallowed = sf.getJSONArray("%role_disallowed");
				
				Role role = findRoleByName(lr, ts.getString(3));
				if( role == null ) {
					return mc.createMessage("Sorry, it looks like that role doesn't exist!");
				}
				
				for( Object a : allowed.toList() ) {
					if( a.toString().equalsIgnoreCase(ts.getString(3)) ) {
						return mc.createMessage("A role by that name is already allowed.");
					}
				}
				
				for( Object da : disallowed.toList() ) {
					if( da.toString().equalsIgnoreCase(ts.getString(3)) ) {
						return mc.createMessage("That role is already on the disallowed list.");
					}
				}
				
				return mc.createMessage("Role added to allowed list successfully.").and(Mono.fromRunnable(() -> {allowed.put(ts.getString(3).toLowerCase());}));
			}
		);
		
		Command<MessageCreateEvent> allowRemoveCommand = new Command<MessageCreateEvent>(dfa);
		allowRemoveCommand.withCondition(MessageModule.getArgumentCondition(2, "remove"));
		allowRemoveCommand.withDependentEffect(
			d -> {
				MessageChannel mc = mcef.requestFrom(d);
				Member m = mef.requestFrom(d);
				List<Role> lr = lref.requestFrom(d);
	
				MessageCreateEvent mce = d.getEvent();
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				SyncedJSONObject sf = SyncedSaveFile.ofGuild(m.getGuildId().asLong());
				
				SyncedJSONArray allowed = sf.getJSONArray("%role_allowed");
				
				Role role = findRoleByName(lr, ts.getString(3));
				if( role == null ) {
					return mc.createMessage("Sorry, it looks like that role doesn't exist!");
				}
				
				int index = -1;
				for( int f=0; f<allowed.length(); f++ ) {
					if( allowed.get(f).toString().equalsIgnoreCase(ts.getString(3)) ) {
						index = f;
						break;
					}
				}
	
				final int finalIndex = index;
				if( index != -1 ) {
					return mc.createMessage("Role removed from allowed list successfully.").and(Mono.fromRunnable(() -> {allowed.remove(finalIndex);}));
				} else {
					return mc.createMessage("That role is not on the allowed list.");
				}
			}
		);
		
		allowCommand.withSubcommand(allowAddCommand);
		allowCommand.withSubcommand(allowRemoveCommand);
		
		Command<MessageCreateEvent> disallowCommand = new Command<MessageCreateEvent>(dfa);
		disallowCommand.withCondition(MessageModule.getArgumentCondition(1, "disallow"));
		disallowCommand.withCondition(MessageModule.getArgumentsCondition(4));
		disallowCommand.withDependentCondition(MessageModule.getPermissionCondition(psef, Permission.ADMINISTRATOR));
		
		Command<MessageCreateEvent> disallowAddCommand = new Command<MessageCreateEvent>(dfa);
		disallowAddCommand.withCondition(MessageModule.getArgumentCondition(2, "add"));
		disallowAddCommand.withDependentEffect(
			d -> {
				MessageChannel mc = mcef.requestFrom(d);
				Member m = mef.requestFrom(d);
				List<Role> lr = lref.requestFrom(d);
	
				MessageCreateEvent mce = d.getEvent();
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				SyncedJSONObject sf = SyncedSaveFile.ofGuild(m.getGuildId().asLong());
				
				SyncedJSONArray allowed = sf.getJSONArray("%role_allowed");
				SyncedJSONArray disallowed = sf.getJSONArray("%role_disallowed");
				
				Role role = findRoleByName(lr, ts.getString(3));
				if( role == null ) {
					return mc.createMessage("Sorry, it looks like that role doesn't exist!");
				}
				
				for( Object a : allowed.toList() ) {
					if( a.toString().equalsIgnoreCase(ts.getString(3)) ) {
						return mc.createMessage("That role is already on the allowed list.");
					}
				}
				
				for( Object da : disallowed.toList() ) {
					if( da.toString().equalsIgnoreCase(ts.getString(3)) ) {
						return mc.createMessage("A role by that name is already disallowed.");
					}
				}
				
				return mc.createMessage("Role added to disallowed list successfully.").and(Mono.fromRunnable(() -> {disallowed.put(ts.getString(3).toLowerCase());}));
			}
		);
		
		Command<MessageCreateEvent> disallowRemoveCommand = new Command<MessageCreateEvent>(dfa);
		disallowRemoveCommand.withCondition(MessageModule.getArgumentCondition(2, "remove"));
		disallowRemoveCommand.withDependentEffect(
			d -> {
				MessageChannel mc = mcef.requestFrom(d);
				Member m = mef.requestFrom(d);
				List<Role> lr = lref.requestFrom(d);
	
				MessageCreateEvent mce = d.getEvent();
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				SyncedJSONObject sf = SyncedSaveFile.ofGuild(m.getGuildId().asLong());
				
				SyncedJSONArray disallowed = sf.getJSONArray("%role_disallowed");
				
				Role role = findRoleByName(lr, ts.getString(3));
				if( role == null ) {
					return mc.createMessage("Sorry, it looks like that role doesn't exist!");
				}
				
				int index = -1;
				for( int f=0; f<disallowed.length(); f++ ) {
					if( disallowed.get(f).toString().equalsIgnoreCase(ts.getString(3)) ) {
						index = f;
						break;
					}
				}
	
				final int finalIndex = index;
				if( index != -1 ) {
					return mc.createMessage("Role removed from disallowed list successfully.").and(Mono.fromRunnable(() -> {disallowed.remove(finalIndex);}));
				} else {
					return mc.createMessage("That role is not on the disallowed list.");
				}
			}
		);
		
		disallowCommand.withSubcommand(disallowAddCommand);
		disallowCommand.withSubcommand(disallowRemoveCommand);
		
		command.withSubcommand(sideCommand);
		command.withSubcommand(getCommand);
		command.withSubcommand(removeCommand);
		command.withSubcommand(allowCommand);
		command.withSubcommand(disallowCommand);
		
		return command;
	}
	
	public static Role findRoleByName(List<Role> roles, String name) {
		for( Role role : roles ) {
			if( role.getName().equalsIgnoreCase(name) ) {
				return role;
			}
		}
		return null;
	}
	
}
