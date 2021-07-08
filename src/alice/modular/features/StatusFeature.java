package alice.modular.features;

import java.util.List;
import java.util.function.Function;

import alice.framework.features.MessageFeature;
import alice.framework.main.Brain;
import alice.framework.tasks.DependentStacker;
import alice.framework.tasks.IndependentStacker;
import alice.modular.tasks.MessageSendTask;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class StatusFeature extends MessageFeature {

	public StatusFeature() {
		super("Status");
		withCheckInvoked();
	}

	@Override
	protected boolean condition(MessageCreateEvent event) {
		return true;
	}

	@Override
	protected Mono<?> respond(MessageCreateEvent type) {
		IndependentStacker response = new IndependentStacker();
		
		Function<List<Guild>, String> statusMessageBuilder = gs -> {
			StringBuilder message = new StringBuilder("Guilds loaded: ");
			for( Guild g : gs ) {
				message.append(g.getName());
				message.append(", ");
			}
			return message.toString();
		};
		
		DependentStacker<List<Guild>> guildsWrapper = new DependentStacker<List<Guild>>(Brain.client.getGuilds().collectList());
		DependentStacker<MessageChannel> channelWrapper = new DependentStacker<MessageChannel>(type.getMessage().getChannel());
		guildsWrapper.addTask(gs -> channelWrapper.addTask(new MessageSendTask( statusMessageBuilder.apply(gs) )));
		
		response.append(guildsWrapper);
		
		return response.toMono();
	}

}
