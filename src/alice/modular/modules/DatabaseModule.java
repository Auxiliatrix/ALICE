package alice.modular.modules;

import java.util.ArrayList;
import java.util.List;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.modules.MessageModule;
import alice.framework.utilities.EmbedFactory;
import alice.framework.utilities.SaveFiles;
import alina.structures.SyncedJSONObject;
import alina.structures.TokenizedString;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public class DatabaseModule extends MessageModule {

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdm = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent, PermissionSet> psdm = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getHumanCondition());
		command.withDependentCondition(MessageModule.getPermissionCondition(psdm, Permission.ADMINISTRATOR));
		command.withCondition(MessageModule.getInvokedCondition(">"));
		
		Command<MessageCreateEvent> sideCommand = command.addSubcommand();
		sideCommand.withCondition(mce -> {
			SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
			return !ssf.has("%db_pos");
		});
		sideCommand.withSideEffect(mce -> {
			SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
			ssf.put("%db_pos", "~");
		});
		
		Command<MessageCreateEvent> cdCommand = command.addSubcommand();
		cdCommand.withCondition(MessageModule.getArgumentCondition(1, "cd"));
		cdCommand.withCondition(MessageModule.getArgumentsCondition(3));
		cdCommand.withDependentEffect(mcdm.buildEffect((mce, mc) -> {
			SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
			String pos = ssf.getString("%db_pos");
			String[] paths = pos.split("/");
			TokenizedString ts = MessageModule.tokenizeMessage(mce);
			ts = ts.getSubTokens(2);
			String[] changes = ts.toString().split("/");
			
//					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Invalid position. Resetting to root.")))
//							.and(Mono.fromRunnable(() -> {
//								ssf.put("%db_pos", "~");
//							}));
			
			return Mono.fromRunnable(() -> {});
		}));
		
		return command;
	}

}
