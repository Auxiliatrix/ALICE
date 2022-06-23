package alice.modular.modules;

import alice.framework.modules.commands.Command;
import alice.framework.modules.commands.MessageModule;
import alice.framework.modules.tasks.DependencyFactory;
import alice.framework.modules.tasks.EffectFactory;
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
		EffectFactory<MessageCreateEvent, InviteData> idef = dfb.addDependency(mce -> mce.getMessage().getRestChannel().createInvite(InviteCreateSpec.builder().maxUses(1).temporary(false).unique(true).build().asRequest(), "Generating"));
		EffectFactory<MessageCreateEvent, MessageChannel> mcef = dfb.addDependency(mce -> mce.getMessage().getChannel());

		DependencyFactory<MessageCreateEvent> df = dfb.buildDependencyFactory();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		
		command.withCondition(getInvokedCondition("%gen"));
		
		command.withDependentEffect(idef.getEffect(id -> {
			return Mono.fromRunnable(() -> {
				System.out.println(id.code());
			});
		}));
		
		command.withDependentEffect(idef.with(mcef).getEffect((id,mc) -> {
			return mc.createMessage("discord.gg/" + id.code());
		}));
		
		return command;
	}

}
