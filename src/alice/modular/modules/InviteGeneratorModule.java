package alice.modular.modules;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyManager;
import alice.framework.modules.MessageModule;
import alice.framework.utilities.EmbedFactory;
import alina.structures.TokenizedString;
import alina.structures.TokenizedString.Token;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.InviteCreateSpec;
import discord4j.discordjson.json.InviteData;
import reactor.core.publisher.Flux;

public class InviteGeneratorModule extends MessageModule {

	@Override
	public Command<MessageCreateEvent> buildCommand(DependencyFactory.Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, InviteData> iddm = dfb.addDependency(mce -> mce.getMessage().getRestChannel().createInvite(InviteCreateSpec.builder().maxUses(1).maxAge(0).temporary(false).unique(true).build().asRequest(), "Generating"));
		DependencyManager<MessageCreateEvent, MessageChannel> mcdm = dfb.addDependency(mce -> mce.getMessage().getChannel());

		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(getInvokedCondition("gen"));
		command.withDependentEffect(iddm.with(mcdm).buildEffect(
			(id,mc) -> mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("discord.gg/" + id.code())))
		));
		
		Command<MessageCreateEvent> multiple = command.addSubcommand();
		multiple.withCondition(MessageModule.getArgumentsCondition(2));
		multiple.withCondition(mce -> {
			TokenizedString ts = new TokenizedString(mce.getMessage().getContent());
			Token t = ts.getToken(1);
			return t.isInteger() && t.asInteger() >= 1;
		});
		multiple.withDependentEffect(iddm.with(mcdm).buildEffect((mce, id, mc) -> {
			TokenizedString ts = new TokenizedString(mce.getMessage().getContent());
			Token t = ts.getToken(1);
			int count = t.asInteger();
			
			if( count > 1 ) {
				Flux<String> s = mce.getMessage().getRestChannel().createInvite(InviteCreateSpec.builder().maxUses(1).maxAge(0).temporary(false).unique(true).build().asRequest(), "Generating").repeat(count-2).map(inviteData -> inviteData.code());
				return s.collectList().flatMap(l -> {
					l.add(id.code());
					return mc.createMessage(String.join(",", l));
				});
			} else {
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("discord.gg/" + id.code())));
			}
		}));
		
		return command;
	}
	
}
