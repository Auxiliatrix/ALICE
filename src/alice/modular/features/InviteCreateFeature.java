package alice.modular.features;

import alice.framework.database.SyncedJSONArray;
import alice.framework.database.SyncedSaveFile;
import alice.framework.features.Feature;
import alice.framework.main.Brain;
import alice.framework.tasks.Stacker;
import alice.framework.utilities.FileIO;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.InviteCreateEvent;
import reactor.core.publisher.Mono;

public class InviteCreateFeature extends Feature<InviteCreateEvent> {

	public InviteCreateFeature() {
		super("invcrt", InviteCreateEvent.class);
	}

	@Override
	protected boolean listen(InviteCreateEvent type) {
		SyncedSaveFile sfi = SyncedSaveFile.of("lab/invite_user.csv");
		if( sfi.has("roleID") && sfi.has("guildID") && sfi.has("invite_map") && sfi.has("self_invites")) {
			System.out.println("New invite link used.");
			return sfi.getLong("guildID") == type.getGuildId().get().asLong();
		} else {
			System.err.println("InviteTrackerFeature conditions not met.");
			return false;
		}
	}

	@Override
	protected Mono<?> respond(InviteCreateEvent type) {
		Stacker stacker = new Stacker();
		SyncedSaveFile sfi = SyncedSaveFile.of("lab/invite_user.csv");
		
		String code = type.getCode();
		if( Snowflake.of("367437754034028545").equals(type.getInviter().get().getId())
				|| Brain.client.getSelfId().equals(type.getInviter().get().getId()) ) {
			stacker.append(() -> {
				SyncedJSONArray inviteMap = sfi.getJSONArray("self_invites");
				inviteMap.put(code);
				FileIO.appendToFile("tmp/codes.csv", String.format("%s\n", code));
				System.out.println(code);
			});
		}
		
		return stacker.toMono();
	}
	
}
