package alice.modular.modules;

import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.modules.MessageModule;
import alice.framework.utilities.EmbedFactory;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public class BlackboxModule extends MessageModule {

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdf = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent, PermissionSet> psdf = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getHumanCondition());
		command.withDependentCondition(MessageModule.getPermissionCondition(psdf, Permission.ADMINISTRATOR));
		command.withCondition(MessageModule.getInvokedCondition("%blackbox"));
		command.withDependentEffect(d -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
			MessageChannel mc = mcdf.requestFrom(d);
			if( ssf.has("%blackbox") ) {
				SyncedJSONObject blackbox = ssf.getJSONObject("%blackbox");
				if( blackbox.has(mc.getId().asString()) ) {
					return mc.createMessage("A blackbox is currently open. You can type `%blackbox close` to close it!");
				}
			}
			return mc.createMessage("No blackbox is currently open. You can type `%blackbox open` to open one!");
		});
		
		Command<MessageCreateEvent> args = command.addSubcommand();
		args.withCondition(MessageModule.getArgumentsCondition(2));
		
		Command<MessageCreateEvent> setup = args.addSubcommand();
		setup.withSideEffect(mce -> {
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(mce.getGuildId().get().asLong());
			if( !ssf.has("%blackbox") ) {
				ssf.putJSONObject("%blackbox");
			}
		});
				
		Command<MessageCreateEvent> open = args.addSubcommand();
		open.withCondition(MessageModule.getArgumentCondition(1, "open"));
		open.withDependentEffect(d -> {
			MessageChannel mc = mcdf.requestFrom(d);
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
			SyncedJSONObject blackbox = ssf.getJSONObject("%blackbox");
			if( blackbox.has(mc.getId().asString()) ) {
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("A blackbox in this channel is already open!")));
			} else {
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Blackbox opened successfully!\nAll messages sent after this will be deleted when the blackbox is closed.")))
						.flatMap(m -> Mono.fromRunnable(() -> {blackbox.put(mc.getId().asString(), m.getId().asString());}));
			}
		});
		
		Command<MessageCreateEvent> close = args.addSubcommand();
		close.withCondition(MessageModule.getArgumentCondition(1, "close"));
		close.withDependentEffect(d -> {
			MessageChannel mc = mcdf.requestFrom(d);
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
			SyncedJSONObject blackbox = ssf.getJSONObject("%blackbox");
			if( blackbox.has(mc.getId().asString()) ) {
				return mc.getMessagesAfter(Snowflake.of(blackbox.getString(mc.getId().asString()))).takeWhile(m -> m.getId().compareTo(d.getEvent().getMessage().getId()) <= 0)
						.flatMap(m -> m.delete()).count().flatMap(l -> mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Blackbox closed successfully."))))
						.then(Mono.fromRunnable(() -> blackbox.remove(mc.getId().asString())));
			} else {
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("No blackbox is currently open!")));
			}
		});
		
		Command<MessageCreateEvent> cancel = args.addSubcommand();
		cancel.withCondition(MessageModule.getArgumentCondition(1, "cancel"));
		cancel.withDependentEffect(d -> {
			MessageChannel mc = mcdf.requestFrom(d);
			SyncedJSONObject ssf = SyncedSaveFile.ofGuild(d.getEvent().getGuildId().get().asLong());
			SyncedJSONObject blackbox = ssf.getJSONObject("%blackbox");
			if( blackbox.has(mc.getId().asString()) ) {
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Blackbox cancelled successfully!")))
						.flatMap(m -> Mono.fromRunnable(() -> {blackbox.remove(mc.getId().asString());}));
			} else {
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("No blackbox is currently open!")));
			}
		});
		
		return command;
	}
	
}
