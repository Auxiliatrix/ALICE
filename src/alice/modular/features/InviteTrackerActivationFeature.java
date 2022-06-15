package alice.modular.features;

import alice.framework.features.MessageFeature;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.tasks.DependentStacker;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;

public class InviteTrackerActivationFeature extends MessageFeature {

	public InviteTrackerActivationFeature() {
		super("invact");
		withRestriction(PermissionProfile.getDeveloperPreset());
		withCheckInvoked();
	}

	@Override
	protected boolean condition(MessageCreateEvent event) {
		return true;
	}

	@Override
	protected Mono<?> respond(MessageCreateEvent type) {
		TokenizedString ts = new TokenizedString(type.getMessage().getContent());
		DependentStacker<Guild> stacker = new DependentStacker<Guild>(type.getGuild());
		stacker.addEffect(g -> {
			if( ts.size() < 2 ) {
				System.err.println("No role ID provided.");
			} else {
				InviteDeleteFeature.trackCustomInvites(g);
				InviteDeleteFeature.guildID = g.getId().asLong();
				InviteDeleteFeature.roleID = Long.parseLong(ts.getToken(1).getContent());
			}
		});
		return stacker.toMono();
	}
	
}
