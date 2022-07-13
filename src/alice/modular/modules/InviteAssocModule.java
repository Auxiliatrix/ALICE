package alice.modular.modules;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.modules.MessageModule;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedFactory;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public class InviteAssocModule extends MessageModule {

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		
		DependencyManager<MessageCreateEvent, MessageChannel> mcdf = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent, PermissionSet> psdf = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getHumanCondition());
		command.withCondition(MessageModule.getGuildCondition());
		command.withDependentCondition(MessageModule.getPermissionCondition(psdf, Permission.ADMINISTRATOR));
		command.withCondition(MessageModule.getInvokedCondition("%assoc"));
		
		Command<MessageCreateEvent> args = command.addSubcommand();
		command.withCondition(MessageModule.getArgumentsCondition(2));
		
		Command<MessageCreateEvent> setup = args.addSubcommand();
		setup.withDependentSideEffect(d -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
			if( !ssf.has("%assoc_lists") ) {
				ssf.putJSONObject("%assoc_lists");
			}
			if( !ssf.has("%assoc_roles") ) {
				ssf.putJSONObject("%assoc_roles");
			}
			if( !ssf.has("%assoc_counts") ) {
				ssf.putJSONObject("%assoc_counts");
			}
		});
		
		// TODO: figure out some way to include Event by default
		
		Command<MessageCreateEvent> add = args.addSubcommand();
		add.withCondition(MessageModule.getArgumentCondition(1, "add"));
		add.withCondition(MessageModule.getArgumentsCondition(3));
		add.withDependentEffect(d -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
			SyncedJSONObject assoc_lists = ssf.getJSONObject("%assoc_lists");
			SyncedJSONObject assoc_roles = ssf.getJSONObject("%assoc_roles");
			SyncedJSONObject assoc_counts = ssf.getJSONObject("%assoc_counts");

			MessageChannel mc = mcdf.requestFrom(d);
			TokenizedString ts = MessageModule.tokenizeMessage(d.getEvent());
			String code = ts.getString(2);
			String roleId = "";
			if( d.getEvent().getMessage().getRoleMentionIds().size() > 0 ) {
				roleId = d.getEvent().getMessage().getRoleMentionIds().get(0).asString();
			}
			if( assoc_lists.has(code) || assoc_roles.has(code) || assoc_counts.has(code) ) {
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("A link association already exists for this invite code!")));
			} else {
				final String roleIdRef = roleId;
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Link associated successfully!")))
						.and(Mono.fromRunnable(() -> {
							assoc_lists.putJSONArray(code);
							if( !roleIdRef.isEmpty() ) {
								assoc_roles.put(code, roleIdRef);
							}
							assoc_counts.put(code, 0);
						})
					);
			}
		});
		
		Command<MessageCreateEvent> remove = args.addSubcommand();
		remove.withCondition(MessageModule.getArgumentCondition(1, "remove"));
		remove.withCondition(MessageModule.getArgumentsCondition(3));
		remove.withDependentEffect(d -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
			SyncedJSONObject assoc_lists = ssf.getJSONObject("%assoc_lists");
			SyncedJSONObject assoc_roles = ssf.getJSONObject("%assoc_roles");
			SyncedJSONObject assoc_counts = ssf.getJSONObject("%assoc_counts");

			MessageChannel mc = mcdf.requestFrom(d);
			TokenizedString ts = MessageModule.tokenizeMessage(d.getEvent());
			String code = ts.getString(2);
			
			if( assoc_lists.has(code) && assoc_roles.has(code) && assoc_counts.has(code) ) {
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("No link association exists for this invite code!")));
			} else {
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Link association removed successfully!")))
					.and(Mono.fromRunnable(() -> {
						if( assoc_lists.has(code) ) {
							assoc_lists.remove(code);
						}
						if( assoc_roles.has(code) ) {
							assoc_lists.remove(code);
						}
						if( assoc_counts.has(code) ) {
							assoc_counts.remove(code);
						}
					})
				);
			}
		});
		
		Command<MessageCreateEvent> check = args.addSubcommand();
		check.withCondition(MessageModule.getArgumentCondition(1, "check"));
		check.withDependentEffect(d -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
			SyncedJSONObject assoc_roles = ssf.getJSONObject("%assoc_roles");
			SyncedJSONObject assoc_counts = ssf.getJSONObject("%assoc_counts");
			
			MessageChannel mc = mcdf.requestFrom(d);
			
			List<SimpleEntry<String,String>> entries = new ArrayList<SimpleEntry<String,String>>();
			
			for( String key : assoc_counts.keySet() ) {
				entries.add(new SimpleEntry<String, String>(key, String.format("Tracked Uses: %d\nAssoc. Role: %s",assoc_counts.get(key),assoc_roles.has(key) ? assoc_roles.get(key) : "N/A")));
			}
			
			return mc.createMessage(EmbedFactory.build(EmbedFactory.modListFormat("Associated Invite Links", Color.CINNABAR, entries, false, true)));
		});
		
		Command<MessageCreateEvent> checkSpecific = check.addSubcommand();
		checkSpecific.withCondition(MessageModule.getArgumentsCondition(3));
		checkSpecific.withDependentEffect(d -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
			SyncedJSONObject assoc_lists = ssf.getJSONObject("%assoc_lists");
			MessageChannel mc = mcdf.requestFrom(d);
			TokenizedString ts = MessageModule.tokenizeMessage(d.getEvent());
			String code = ts.getString(2);
			
			if( assoc_lists.has(code) ) {
				return mc.createMessage(assoc_lists.get(code).toString());
			} else {
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("No association exists for the given invite code!")));
			}
		});
		
		return command;
	}

}
