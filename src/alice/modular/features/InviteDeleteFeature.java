package alice.modular.features;

import java.util.Comparator;
import java.util.List;

import alice.framework.features.Feature;
import alice.framework.main.Brain;
import alice.framework.tasks.Stacker;
import alice.framework.utilities.FileIO;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.InviteDeleteEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

public class InviteDeleteFeature extends Feature<InviteDeleteEvent> {

	public static long guildID = -1;
	public static long roleID = -1;
	public static List<String> uniqueCodes = null;
	
	public InviteDeleteFeature() {
		super("invdel", InviteDeleteEvent.class);
	}

	@Override
	protected boolean listen(InviteDeleteEvent type) {
		if( uniqueCodes == null || guildID < 1 || roleID < 1 ) {
			System.err.println("InviteTrackerFeature conditions not met.");
			return false;
		}
		System.out.println("Join detected");
		return guildID == type.getGuildId().get().asLong();
	}

	@Override
	protected Mono<?> respond(InviteDeleteEvent type) {
		Stacker stacker = new Stacker();
		
		Member target = Brain.getMembers(type.getGuildId().get()).sort(new Comparator<Member>() {
			@Override
			public int compare(Member o1, Member o2) {
				return -o1.getJoinTime().get().compareTo(o2.getJoinTime().get());
			}}).blockFirst();
		String code = type.getCode();
		stacker.append(target.addRole(Snowflake.of(roleID)));
		stacker.append(() -> {
			FileIO.appendToFile("user_associations.csv", String.format("%s,%s#%s,%s\n", code, target.getUsername(), target.getDiscriminator(), target.getId().asString()));
			System.out.println(target.getUsername() + "#" + target.getDiscriminator());
		});
		
		return stacker.toMono();
	}
	
	public static void trackCustomInvites(Guild guild) {
//		List<String> invites = guild.getInvites().filter(ei -> Brain.client.getSelfId().equals(ei.getInviterId().get())).map(ei -> ei.getCode()).collectList().block();
		List<String> invites = guild.getInvites().filter(ei -> 
			Snowflake.of("367437754034028545").equals(ei.getInviterId().get())
			|| Brain.client.getSelfId().equals(ei.getInviterId().get())
			).map(ei -> ei.getCode()).collectList().block();
		System.out.println(invites.size());
		uniqueCodes = invites;
	}

}
