package alice.modular.features;

import java.util.List;

import alice.framework.database.SaveArrayInterface;
import alice.framework.database.SaveFileInterface;
import alice.framework.database.SaveSyncProxy;
import alice.framework.features.MessageFeature;
import alice.framework.main.Brain;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.tasks.DependentStacker;
import discord4j.common.util.Snowflake;
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
				SaveFileInterface sfi = SaveSyncProxy.of("lab/invite_user.csv");
				if( !sfi.has("invite_map") ) {
					sfi.putJSONObject("invite_map");
				}
				if( !sfi.has("guildID") ) {
					sfi.put("guildID", g.getId().asLong());
				}
				if( !sfi.has("roleID") ) {
					sfi.put("roleID", Long.parseLong(ts.getToken(1).getContent()));
				}
				trackCustomInvites(g);
			}
		});
		return stacker.toMono();
	}
	
	public static void trackCustomInvites(Guild guild) {
		SaveFileInterface sfi = SaveSyncProxy.of("lab/invite_user.csv");
		if( !sfi.has("self_invites") ) {
			sfi.putJSONArray("self_invites");
		}
		SaveArrayInterface sai = sfi.getJSONArray("self_invites");
		List<String> invites = guild.getInvites().filter(ei -> 
			Snowflake.of("367437754034028545").equals(ei.getInviterId().get())
			|| Brain.client.getSelfId().equals(ei.getInviterId().get())
			).map(ei -> ei.getCode()).collectList().block();
		System.out.println(invites.size());
		for( String invite : invites ) {
			if( !sai.toList().contains(invite) ) {
				sai.put(invite);
			}
		}
	}
	
}
