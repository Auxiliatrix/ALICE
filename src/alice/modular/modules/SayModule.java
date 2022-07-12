package alice.modular.modules;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.modules.MessageModule;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;

public class SayModule extends MessageModule {

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdf = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent, PermissionSet> psdf = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getHumanCondition());
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getInvokedCondition("%say"));
		command.withCondition(MessageModule.getArgumentsCondition(2));
		command.withDependentCondition(MessageModule.getPermissionCondition(psdf, Permission.ADMINISTRATOR));
		command.withDependentEffect(d -> {
			return d.getEvent().getMessage().delete().and(mcdf.requestFrom(d).createMessage(MessageModule.tokenizeMessage(d.getEvent()).getSubTokens(1).toString()));
		});
		
		return command;
	}

}
