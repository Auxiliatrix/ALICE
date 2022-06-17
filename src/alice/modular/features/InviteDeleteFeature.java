package alice.modular.features;

import java.util.Comparator;

import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.features.Feature;
import alice.framework.main.Brain;
import alice.framework.old.tasks.Stacker;
import alice.framework.utilities.FileIO;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.InviteDeleteEvent;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

public class InviteDeleteFeature extends Feature<InviteDeleteEvent> {
	
	public InviteDeleteFeature() {
		super("invdel", InviteDeleteEvent.class);
	}

	@Override
	protected boolean listen(InviteDeleteEvent type) {
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
	protected Mono<?> respond(InviteDeleteEvent type) {
		Stacker stacker = new Stacker();
		SyncedSaveFile sfi = SyncedSaveFile.of("lab/invite_user.csv");

		if( sfi.getJSONArray("self_invites").toList().contains(type.getCode()) ) {
			Member target = Brain.getMembers(type.getGuildId().get()).sort(new Comparator<Member>() {
				@Override
				public int compare(Member o1, Member o2) {
					return -o1.getJoinTime().get().compareTo(o2.getJoinTime().get());
				}}).blockFirst();
			String code = type.getCode();
			stacker.append(target.addRole(Snowflake.of(sfi.getLong("roleID"))));
			stacker.append(() -> {
				SyncedJSONObject inviteMap = sfi.getJSONObject("invite_map");
				inviteMap.put(target.getId().asString(), code);
				FileIO.appendToFile("tmp/user_associations.csv", String.format("%s,%s#%s,%s\n", code, target.getUsername(), target.getDiscriminator(), target.getId().asString()));
				System.out.println(target.getUsername() + "#" + target.getDiscriminator());
			});
		}
		
		return stacker.toMono();
	}

}
