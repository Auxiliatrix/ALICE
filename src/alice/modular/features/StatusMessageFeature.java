package alice.modular.features;

import java.util.List;
import java.util.function.Function;

import alice.framework.features.MessageFeature;
import alice.framework.main.Brain;
import alice.framework.old.tasks.MultipleDependentStacker;
import alice.framework.old.tasks.Stacker;
import alice.framework.structures.PermissionProfile;
import alice.modular.tasks.MessageSendTask;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class StatusMessageFeature extends MessageFeature {

	public StatusMessageFeature() {
		super("Status");
		withCheckInvoked();
		withRestriction(PermissionProfile.getDeveloperPreset());
	}

	@Override
	protected boolean condition(MessageCreateEvent event) {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Mono<?> respond(MessageCreateEvent type) {
		Stacker response = new Stacker();
		
		Function<List<Guild>, String> statusMessageBuilder = gs -> {
			StringBuilder message = new StringBuilder("Guilds loaded: ");
			for( Guild g : gs ) {
				message.append(g.getName());
				message.append(", ");
			}
			return message.toString().substring(0, message.length()-1);
		};
		
//		DependentStacker<List<Guild>> guildsWrapper = new DependentStacker<List<Guild>>(Brain.client.getGuilds().collectList());
//		DependentStacker<MessageChannel> channelWrapper = new DependentStacker<MessageChannel>(type.getMessage().getChannel());
//		guildsWrapper.addTask(gs -> channelWrapper.addTask(new MessageSendTask( statusMessageBuilder.apply(gs) )));
//		
//		response.append(guildsWrapper);
		
		MultipleDependentStacker mds = new MultipleDependentStacker(Brain.client.getGuilds().collectList(), type.getMessage().getChannel());
		mds.addTask(args -> new MessageSendTask(statusMessageBuilder.apply((List<Guild>) args.get(0))).apply((MessageChannel) args.get(1)));
		
		response.append(mds);
		
		
		MultipleDependentStacker tester = new MultipleDependentStacker(
					Mono.empty(),
					Mono.empty()
				);
		tester.addTask(a -> {
			return Mono.fromRunnable(() -> {});
		});
		
		response.append(tester);
		
		return response.toMono();
	}

}
