package alice.modular.modules;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.modules.MessageModule;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public class MassBanModule extends MessageModule {

	public MassBanModule() {
		super();
	}
	
	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdf = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent, PermissionSet> psdf = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		DependencyManager<MessageCreateEvent, Guild> gdf = dfb.addDependency(mce -> mce.getGuild());
		
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getHumanCondition());
		command.withDependentCondition(MessageModule.getPermissionCondition(psdf, Permission.ADMINISTRATOR));
		command.withCondition(MessageModule.getArgumentsCondition(2));
		command.withCondition(MessageModule.getInvokedCondition("%ban"));
		command.withDependentEffect(d -> {
			MessageChannel mc = mcdf.requestFrom(d);
			Guild g = gdf.requestFrom(d);
			Mono<?> ret = mc.createMessage(EmbedBuilders.applySuccessFormat("Initiating automatic bans."));
			TokenizedString ts = MessageModule.tokenizeMessage(d.getEvent());
			TokenizedString ids = ts.getSubTokens(1);
			for( int f=0; f<ids.size(); f++ ) {
				try {
					long id = Long.parseLong(ids.getToken(f).toString());
					ret = ret.and(g.ban(Snowflake.of(id)));
				} catch( NumberFormatException e ) {}
			}
			return ret;
		});
		
		return command;
	}

}
