package alice.modular.features;

import java.util.List;
import java.util.function.Function;

import alice.framework.database.SharedSaveFile;
import alice.framework.features.MessageFeature;
import alice.framework.main.Brain;
import alice.framework.tasks.IndependentStacker;
import alice.framework.tasks.MultipleDependentStacker;
import alice.modular.tasks.MessageSendTask;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class StatusMessageFeature extends MessageFeature {

	public StatusMessageFeature() {
		super("Status");
		withCheckInvoked();
	}

	@Override
	protected boolean condition(MessageCreateEvent event) {
		return true;
	}

	@SuppressWarnings("unchecked")
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
		
//		DependentStacker<List<Guild>> guildsWrapper = new DependentStacker<List<Guild>>(Brain.client.getGuilds().collectList());
//		DependentStacker<MessageChannel> channelWrapper = new DependentStacker<MessageChannel>(type.getMessage().getChannel());
//		guildsWrapper.addTask(gs -> channelWrapper.addTask(new MessageSendTask( statusMessageBuilder.apply(gs) )));
//		
//		response.append(guildsWrapper);
		
		MultipleDependentStacker mds = new MultipleDependentStacker(Brain.client.getGuilds().collectList(), type.getMessage().getChannel());
		mds.addTask(args -> new MessageSendTask(statusMessageBuilder.apply((List<Guild>) args.get(0))).apply((MessageChannel) args.get(1)));
		mds.addEffect(c -> {
			SharedSaveFile sf = new SharedSaveFile(Long.valueOf("266668506932576256"));
			sf.test();
		});
		
		response.append(mds);
		
		return response.toMono();
	}

}
