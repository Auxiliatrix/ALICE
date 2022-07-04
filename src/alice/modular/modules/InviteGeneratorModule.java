package alice.modular.modules;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyManager;
import alice.framework.modules.MessageModule;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.InviteCreateSpec;
import discord4j.discordjson.json.InviteData;
import reactor.core.publisher.Mono;

public class InviteGeneratorModule extends MessageModule {

	public InviteGeneratorModule() {
		super();
	}

	@Override
	public Command<MessageCreateEvent> buildCommand(DependencyFactory.Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, InviteData> idef = dfb.addDependency(mce -> mce.getMessage().getRestChannel().createInvite(InviteCreateSpec.builder().maxUses(1).temporary(false).unique(true).build().asRequest(), "Generating"));
		DependencyManager<MessageCreateEvent, MessageChannel> mcef = dfb.addDependency(mce -> mce.getMessage().getChannel());

		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(getInvokedCondition("%gen"));
		command.withDependentEffect(idef.buildEffect(
			id -> Mono.fromRunnable(() -> {System.out.println(id.code());}
		)));
		command.withDependentEffect(idef.with(mcef).buildEffect(
			(id,mc) -> mc.createMessage("discord.gg/" + id.code())
		));
		
		return command;
	}

}
