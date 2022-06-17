package alice.modular.features;

import alice.framework.features.MessageFeature;
import alice.framework.old.tasks.MultipleDependentStacker;
import alice.framework.old.tasks.Stacker;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.structures.TokenizedString.Token;
import alice.modular.tasks.MessageSendTask;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class GermanHighwayMessageFeature extends MessageFeature {

	public GermanHighwayMessageFeature() {
		super("germanhighway");
		withCheckInvoked();
		withRestriction(PermissionProfile.getDeveloperPreset());
	}
	
	@Override
	protected boolean condition(MessageCreateEvent event) {
		return true;
	}
	
	@Override
	protected Mono<?> respond(MessageCreateEvent type) {
		Stacker response = new Stacker();
		TokenizedString ts = new TokenizedString(type.getMessage().getContent());
		ts = ts.getSubTokens(1);
		
		MultipleDependentStacker channelWrapper = new MultipleDependentStacker(type.getMessage().getChannel(), type.getMessage().getGuild());		
		channelWrapper.addTask(list -> (new MessageSendTask("Initiating autobanh. Please stand by.")).apply((MessageChannel) list.get(0)));
		for( Token token : ts.getTokens() ) {
			channelWrapper.addTask(list -> ((Guild) list.get(1)).ban(Snowflake.of(token.toString())));
		}
		
		response.append(channelWrapper);
	
		return response.toMono();
	}
	
//	@Override
//	protected Mono<?> respond(MessageCreateEvent type) {
//		Stacker response = new Stacker();
//
//		MultipleDependentStacker channelWrapper = new MultipleDependentStacker(type.getMessage().getChannel(), type.getMessage().getGuild());		
//		channelWrapper.addTask(list -> (new MessageSendTask("Initiating autobanh. Please stand by.")).apply((MessageChannel) list.get(0)));
//		channelWrapper.addTask(list -> (new MessageSendTask("Now beginning count.")).apply((MessageChannel) list.get(0)));
//		channelWrapper.addTask(list -> ((Guild) list.get(1))
//				.getMembers().filter(m -> Instant.now().getEpochSecond() - m.getJoinTime().getEpochSecond() > 5400)
//				.collectList().as(m -> (new MessageSendTask(m.block().size() + " members collected.")).apply((MessageChannel) list.get(0)))
//			);
//		channelWrapper.addTask(list -> (new MessageSendTask("Counting completed. That, or you have a threading issue again.")).apply((MessageChannel) list.get(0)));
//		
//		response.append(channelWrapper);
//	
//		return response.toMono();
//	}
	
}
