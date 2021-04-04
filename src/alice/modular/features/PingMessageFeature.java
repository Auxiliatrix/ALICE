package alice.modular.features;

import alice.framework.features.MessageFeature;
import alice.framework.tasks.Wrapper;
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
	protected Mono<Void> respond(MessageCreateEvent type) {
		Mono<Void> response = Mono.fromRunnable(() -> {});
		
		Wrapper<MessageChannel> channelWrapper = new Wrapper<MessageChannel>(type.getMessage().getChannel());
		channelWrapper.addTask(new MessageSendTask("Pong!"));
		response = response.and(channelWrapper.toMono());
		return response;
	}
	
}
