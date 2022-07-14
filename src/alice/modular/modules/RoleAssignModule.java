package alice.modular.modules;

import java.util.List;

import alice.framework.database.SyncedJSONArray;
import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SaveFiles;
import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyManager;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.modules.MessageModule;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedFactory;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public class RoleAssignModule extends MessageModule {

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent,MessageChannel> mcdm = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent,Member> mdm = dfb.addWrappedDependency(mce -> mce.getMember().get());
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getInvokedCondition("%role"));
		
		Command<MessageCreateEvent> sideCommand = command.addSubcommand();
		sideCommand.withSideEffect(
			mce -> {
				SyncedJSONObject sf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
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
		
		Command<MessageCreateEvent> getCommand = command.addSubcommand(dfs);
		getCommand.withCondition(MessageModule.getArgumentCondition(1, "add"));
		getCommand.withCondition(MessageModule.getArgumentsCondition(3));
		getCommand.withDependentEffect(mcdm.with(lref).with(mdm).buildEffect(
			(mce, mc, lr, m) -> {
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				SyncedJSONObject sf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
				SyncedJSONArray allowed = sf.getJSONArray("%role_allowed");
				SyncedJSONArray disallowed = sf.getJSONArray("%role_disallowed");
							
				for( Object da : disallowed.toList() ) {
					if( da.toString().equalsIgnoreCase(ts.getString(2)) ) {
						return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("You're not allowed to have that role!")));
					}
				}
				for( Object a : allowed.toList() ) {
					if( a.toString().equalsIgnoreCase(ts.getString(2)) ) {
						Role role = findRoleByName(lr, ts.getString(2));
						if( role != null ) {
							return m.addRole(role.getId())
									.and(mce.getMessage().addReaction(ReactionEmoji.unicode("\u2705")));
						} else {
							return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("It looks like that role doesn't exist!")));
						}
					}
				}
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("I don't recognize that as an assignable role!")));
			}
		));
		
		Command<MessageCreateEvent> removeCommand = command.addSubcommand(dfs);
		removeCommand.withCondition(MessageModule.getArgumentCondition(1, "remove"));
		removeCommand.withCondition(MessageModule.getArgumentsCondition(3));
		removeCommand.withDependentEffect(mcdm.with(mdm).with(lref).with(luref).buildEffect(
			(mce, mc, m, lr, lur) -> {
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				
				SyncedJSONObject sf = SaveFiles.ofGuild(m.getGuildId().asLong());
				SyncedJSONArray allowed = sf.getJSONArray("%role_allowed");
				SyncedJSONArray disallowed = sf.getJSONArray("%role_disallowed");
				
				for( Object da : disallowed.toList() ) {
					if( da.toString().equalsIgnoreCase(ts.getString(2)) ) {
						return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("You're not allowed to remove that role!")));
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
								return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("That doesn't seem to be a role that you have!")));
							}
						} else {
							return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Tt looks like that role doesn't exist!")));
						}
					}
				}
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("I don't recognize that as an unassignable role!")));
			}
		));
		
		DependencyManager<MessageCreateEvent,PermissionSet> psef = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		DependencyFactory<MessageCreateEvent> dfa = dfb.build();
		
		Command<MessageCreateEvent> allowCommand = command.addSubcommand(dfa);
		allowCommand.withCondition(MessageModule.getArgumentCondition(1, "allow"));
		allowCommand.withCondition(MessageModule.getArgumentsCondition(4));
		allowCommand.withDependentCondition(MessageModule.getPermissionCondition(psef, Permission.ADMINISTRATOR));
		
		Command<MessageCreateEvent> allowAddCommand = allowCommand.addSubcommand();
		allowAddCommand.withCondition(MessageModule.getArgumentCondition(2, "add"));
		allowAddCommand.withDependentEffect(mcdm.with(mdm).with(lref).buildEffect(
			(mce, mc, m, lr) -> {
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				SyncedJSONObject sf = SaveFiles.ofGuild(m.getGuildId().asLong());
				
				SyncedJSONArray allowed = sf.getJSONArray("%role_allowed");
				SyncedJSONArray disallowed = sf.getJSONArray("%role_disallowed");
				
				Role role = findRoleByName(lr, ts.getString(3));
				if( role == null ) {
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("It looks like that role doesn't exist!")));
				}
				
				for( Object a : allowed.toList() ) {
					if( a.toString().equalsIgnoreCase(ts.getString(3)) ) {
						return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("A role by that name is already allowed.")));
					}
				}
				
				for( Object da : disallowed.toList() ) {
					if( da.toString().equalsIgnoreCase(ts.getString(3)) ) {
						return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("That role is already on the disallowed list.")));
					}
				}
				
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Role added to allowed list successfully."))).and(Mono.fromRunnable(() -> {allowed.put(ts.getString(3).toLowerCase());}));
			}
		));
		
		Command<MessageCreateEvent> allowRemoveCommand = allowCommand.addSubcommand();
		allowRemoveCommand.withCondition(MessageModule.getArgumentCondition(2, "remove"));
		allowRemoveCommand.withDependentEffect(mcdm.with(mdm).with(lref).buildEffect(
			(mce, mc, m, lr) -> {
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				SyncedJSONObject sf = SaveFiles.ofGuild(m.getGuildId().asLong());
				
				SyncedJSONArray allowed = sf.getJSONArray("%role_allowed");
				
				Role role = findRoleByName(lr, ts.getString(3));
				if( role == null ) {
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Sorry, it looks like that role doesn't exist!")));
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
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Role removed from allowed list successfully."))).and(Mono.fromRunnable(() -> {allowed.remove(finalIndex);}));
				} else {
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("That role is not on the allowed list.")));
				}
			}
		));
		
		Command<MessageCreateEvent> disallowCommand = command.addSubcommand(dfa);
		disallowCommand.withCondition(MessageModule.getArgumentCondition(1, "disallow"));
		disallowCommand.withCondition(MessageModule.getArgumentsCondition(4));
		disallowCommand.withDependentCondition(MessageModule.getPermissionCondition(psef, Permission.ADMINISTRATOR));
		
		Command<MessageCreateEvent> disallowAddCommand = disallowCommand.addSubcommand();
		disallowAddCommand.withCondition(MessageModule.getArgumentCondition(2, "add"));
		disallowAddCommand.withDependentEffect(mcdm.with(mdm).with(lref).buildEffect(
			(mce, mc, m, lr) -> {
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				SyncedJSONObject sf = SaveFiles.ofGuild(m.getGuildId().asLong());
				
				SyncedJSONArray allowed = sf.getJSONArray("%role_allowed");
				SyncedJSONArray disallowed = sf.getJSONArray("%role_disallowed");
				
				Role role = findRoleByName(lr, ts.getString(3));
				if( role == null ) {
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Sorry, it looks like that role doesn't exist!")));
				}
				
				for( Object a : allowed.toList() ) {
					if( a.toString().equalsIgnoreCase(ts.getString(3)) ) {
						return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("That role is already on the allowed list.")));
					}
				}
				
				for( Object da : disallowed.toList() ) {
					if( da.toString().equalsIgnoreCase(ts.getString(3)) ) {
						return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("A role by that name is already disallowed.")));
					}
				}
				
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Role added to disallowed list successfully."))).and(Mono.fromRunnable(() -> {disallowed.put(ts.getString(3).toLowerCase());}));
			}
		));
		
		Command<MessageCreateEvent> disallowRemoveCommand = disallowCommand.addSubcommand();
		disallowRemoveCommand.withCondition(MessageModule.getArgumentCondition(2, "remove"));
		disallowRemoveCommand.withDependentEffect(mcdm.with(mdm).with(lref).buildEffect(
			(mce, mc, m, lr) -> {
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				SyncedJSONObject sf = SaveFiles.ofGuild(m.getGuildId().asLong());
				
				SyncedJSONArray disallowed = sf.getJSONArray("%role_disallowed");
				
				Role role = findRoleByName(lr, ts.getString(3));
				if( role == null ) {
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Sorry, it looks like that role doesn't exist!")));
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
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Role removed from disallowed list successfully."))).and(Mono.fromRunnable(() -> {disallowed.remove(finalIndex);}));
				} else {
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("That role is not on the disallowed list.")));
				}
			}
		));
		
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
