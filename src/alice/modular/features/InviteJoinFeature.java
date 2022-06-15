package alice.modular.features;

import alice.framework.features.Feature;
import alice.framework.main.Brain;
import alice.framework.tasks.DependentStacker;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;

public class InviteJoinFeature extends Feature<MemberJoinEvent> {

	// MessageCreateEvent{message=Message{data=MessageData{id=986426746926690324, channelId=921105963610697780, guildId=Possible{921105962511773799}, author=UserData{id=494424533370142721, username=??, discriminator=7185, avatar=3c45f1f8fc49903e78f32160d2ac4224, banner=Possible.absent, accentColor=Possible.absent, bot=Possible.absent, system=Possible.absent, mfaEnabled=Possible.absent, locale=Possible.absent, verified=Possible.absent, email=Possible.absent, flags=Possible.absent, premiumType=Possible.absent, publicFlags=Possible{0}}, member=Possible{PartialMemberData{nick=Possible{Optional.empty}, roles=[J@32d0112c, joinedAt=2022-06-15T00:27:26.507000+00:00, premiumSince=Possible{Optional.empty}, deaf=false, mute=false, communicationDisabledUntil=Possible{Optional.empty}}}, content=, timestamp=2022-06-15T00:27:43.390000+00:00, tts=false, mentionEveryone=false, mentions=[], mentionRoles=[], mentionChannels=null, attachments=[], embeds=[], reactions=null, nonce=Possible.absent, pinned=false, webhookId=Possible.absent, type=7, activity=Possible.absent, application=Possible.absent, applicationId=Possible.absent, messageReference=Possible.absent, flags=Possible{0}, stickers=null, referencedMessage=Possible.absent, interaction=Possible.absent, components=[]}}, guildId=921105962511773799, member=Member{} PartialMember{data=MemberData{nick=Possible{Optional.empty}, roles=[J@3c17e906, joinedAt=2022-06-15T00:27:26.507000+00:00, premiumSince=Possible{Optional.empty}, deaf=false, mute=false, communicationDisabledUntil=Possible.absent, user=UserData{id=494424533370142721, username=??, discriminator=7185, avatar=3c45f1f8fc49903e78f32160d2ac4224, banner=Possible.absent, accentColor=Possible.absent, bot=Possible.absent, system=Possible.absent, mfaEnabled=Possible.absent, locale=Possible.absent, verified=Possible.absent, email=Possible.absent, flags=Possible.absent, premiumType=Possible.absent, publicFlags=Possible{0}}, pending=Possible.absent, permissions=Possible.absent}, guildId=921105962511773799} User{data=UserData{id=494424533370142721, username=??, discriminator=7185, avatar=3c45f1f8fc49903e78f32160d2ac4224, banner=Possible.absent, accentColor=Possible.absent, bot=Possible.absent, system=Possible.absent, mfaEnabled=Possible.absent, locale=Possible.absent, verified=Possible.absent, email=Possible.absent, flags=Possible.absent, premiumType=Possible.absent, publicFlags=Possible{0}}}}
	
	public static long inviteCounter = -1;
	public static long guildID = -1;
	public static long roleID = -1;
	
	public InviteJoinFeature() {
		super("InviteTracker", MemberJoinEvent.class);
		withExclusionClass(ExclusionClass.STANDARD);
	}

	@Override
	protected boolean listen(MemberJoinEvent type) {
		return false;
//		if( inviteCounter < 1 || guildID < 1 || roleID < 1 ) {
//			System.err.println("InviteTrackerFeature conditions not met.");
//			return false;
//		}
//		System.out.println("Join detected");
//		return guildID == type.getGuildId().asLong();
	}
	
	@Override
	protected Mono<?> respond(MemberJoinEvent type) {
		System.out.println("Responding");
		DependentStacker<Guild> stacker = new DependentStacker<Guild>(type.getGuild());
		stacker.addTask(g -> {
			long invites = countInvites(g);
			if( invites == inviteCounter-1 ) {
				System.out.println("Conditions met");
				inviteCounter = invites;
				return type.getMember().addRole(Snowflake.of(roleID));
			} else if( invites < inviteCounter ) {
				System.err.println("Uh oh. One slipped through the cracks.");
			} else {
				System.out.println("Conditions not met.");
			}
			return Mono.fromRunnable(() -> {});
		});
		return null;
	}
	
	public static long countInvites(Guild guild) {
		long count = guild.getInvites().filter(ei -> Brain.client.getSelfId().equals(ei.getInviterId().get())).count().block();
		System.out.println(count);
		return count;
	}

}
