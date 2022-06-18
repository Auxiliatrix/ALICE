package alice.modular.modules;

import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.modules.commands.Command;
import alice.framework.modules.commands.Module;
import alice.framework.modules.tasks.DependencyFactory;
import alice.framework.modules.tasks.EffectFactory;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.InviteCreateSpec;
import discord4j.discordjson.json.InviteData;
import reactor.core.publisher.Mono;

public class InviteGeneratorModule extends Module<MessageCreateEvent> {

	public InviteGeneratorModule() {
		super(MessageCreateEvent.class);
	}

	@Override
	public Command<MessageCreateEvent> buildCommand() {
		SyncedJSONObject sfi = SyncedSaveFile.of("lab/invite_user.csv");

		DependencyFactory.Builder<MessageCreateEvent> dfb = DependencyFactory.builder();
		EffectFactory<MessageCreateEvent, InviteData> idef = dfb.addDependency(mce -> mce.getMessage().getRestChannel().createInvite(InviteCreateSpec.builder().maxUses(1).temporary(false).unique(true).build().asRequest(), "Generating"));
		EffectFactory<MessageCreateEvent, MessageChannel> mcef = dfb.addDependency(mce -> mce.getMessage().getChannel());

		DependencyFactory<MessageCreateEvent> df = dfb.buildDependencyFactory();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		
		command.withCondition(mce -> mce.getMessage().getContent().startsWith("%gen"));
		
		command.withDependentEffect(idef.getEffect(id -> {
			return Mono.fromRunnable(() -> {
				System.out.println(id.code());
			});
		}));
		
		command.withDependentEffect(d -> {
			InviteData id = d.<InviteData>request(idef.getRetriever());
			return d.<MessageChannel>request(mcef.getRetriever()).createMessage("discord.gg/" + id.code());
		});
		
		return command;
	}

}
