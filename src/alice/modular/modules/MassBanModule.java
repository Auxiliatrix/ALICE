package alice.modular.modules;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.modules.MessageModule;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedFactory;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public class MassBanModule extends MessageModule {
	
	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdm = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent, PermissionSet> psdm = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		DependencyManager<MessageCreateEvent, Guild> gdm = dfb.addDependency(mce -> mce.getGuild());
		
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getHumanCondition());
		command.withDependentCondition(MessageModule.getPermissionCondition(psdm, Permission.ADMINISTRATOR));
		command.withCondition(MessageModule.getArgumentsCondition(2));
		command.withCondition(MessageModule.getInvokedCondition("%ban"));
		command.withDependentEffect(mcdm.with(gdm).buildEffect(
			(mce, mc, g) -> {
				Mono<?> ret = mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Initiating automatic bans.")));
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				TokenizedString ids = ts.getSubTokens(1);
				for( int f=0; f<ids.size(); f++ ) {
					try {
						long id = Long.parseLong(ids.getToken(f).toString());
						ret = ret.and(g.ban(Snowflake.of(id)));
					} catch( NumberFormatException e ) {}
				}
				return ret;
			}
		));
		
		return command;
	}

}
