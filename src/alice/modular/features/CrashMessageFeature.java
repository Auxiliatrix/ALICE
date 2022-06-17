package alice.modular.features;

import alice.framework.features.MessageFeature;
import alice.framework.old.tasks.DependentStacker;
import alice.framework.old.tasks.MultipleDependentStacker;
import alice.framework.old.tasks.Stacker;
import alice.framework.structures.PermissionProfile;
import alice.modular.tasks.MessageSendTask;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class CrashMessageFeature extends MessageFeature {

	@SuppressWarnings("serial")
	public class CrashException extends Exception {
		public CrashException() {
			super();
		}
	}
	
	public CrashMessageFeature() {
		super("Crash");
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
		
		DependentStacker<MessageChannel> mcs = new DependentStacker<MessageChannel>(type.getMessage().getChannel());
		mcs.addTask(new MessageSendTask("Crashing!"));
		mcs.addEffect(mc -> {
				System.out.println("Crashing!");
				throw new RuntimeException("Crashing!");
			});
		
		response.append(mcs);
		
		
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
