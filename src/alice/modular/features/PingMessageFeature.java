package alice.modular.features;

import alice.framework.features.MessageFeature;
import alice.framework.tasks.DependentStacker;
import alice.framework.tasks.IndependentStacker;
import alice.modular.tasks.MessageSendTask;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class PingMessageFeature extends MessageFeature {

	public PingMessageFeature() {
		super("Ping");
		withCheckInvoked();
	}

	@Override
	protected boolean condition(MessageCreateEvent event) {
		return true;
	}

	@Override
	protected Mono<?> respond(MessageCreateEvent type) {
		IndependentStacker response = new IndependentStacker();
		
		DependentStacker<MessageChannel> channelWrapper = new DependentStacker<MessageChannel>(type.getMessage().getChannel());		
		channelWrapper.addTask(new MessageSendTask("Pong!"));
		
		response.append(channelWrapper);
		
		return response.toMono();
	}
	
}
