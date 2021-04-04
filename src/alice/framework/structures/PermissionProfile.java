package alice.framework.structures;

import java.util.function.BiPredicate;

import alice.framework.main.Constants;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Permission;

public class PermissionProfile {
	
	/* Internal Verification BiPredicate */
	private BiPredicate<Member, Guild> verification; 
	
	/* Constructors */
	public PermissionProfile() {
		this(null);
	}
	
	private PermissionProfile(BiPredicate<Member, Guild> preset) {
		verification = preset;
	}
	
	/* Primary Use Function */
	public boolean verify(Member member, Guild guild) {
		return verification.test(member, guild) || isDeveloper(member);
	}
	
	/* Factory Methods */
	public static PermissionProfile getAnyonePreset() {
		return new PermissionProfile( (member, guild) -> true );
	}
	
	public static PermissionProfile getAdminPreset() {
		return new PermissionProfile( (member, guild) -> hasPermission(member, guild, Permission.ADMINISTRATOR) );
	}

	public static PermissionProfile getOwnerPreset() {
		return new PermissionProfile( (member, guild) -> isOwner(member, guild) );
	}
	
	public static PermissionProfile getDeveloperPreset() {
		return new PermissionProfile( (member, guild) -> isDeveloper(member) ) ;
	}
	
	/* Builder Methods */
	public PermissionProfile and(Permission permission) {
		verification = verification == null 
							? (member, guild) -> hasPermission(member, guild, permission)
							: verification.and( (member, guild) -> hasPermission(member, guild, permission));
		return this;
	}
	
	public PermissionProfile or(Permission permission) {
		verification = verification == null 
							? (member, guild) -> hasPermission(member, guild, permission)
							: verification.or( (member, guild) -> hasPermission(member, guild, permission));
		return this;
	}
	
	public PermissionProfile andFromUser() {
		verification = verification == null
							? (member, guild) -> fromUser(member)
							: verification.and( (member, guild) -> fromUser(member));
		return this;
	}
	
	public PermissionProfile orFromUser() {
		verification = verification == null
							? (member, guild) -> fromUser(member)
							: verification.or( (member, guild) -> fromUser(member));
		return this;
	}
	
	public PermissionProfile andDeveloper() {
		verification = verification == null
						? (member, guild) -> isDeveloper(member)
						: verification.and( (member, guild) -> isDeveloper(member));
		return this;
	}
	
	public PermissionProfile orDeveloper() {
		verification = verification == null
						? (member, guild) -> isDeveloper(member)
						: verification.or( (member, guild) -> isDeveloper(member));
		return this;
	}
	
	public PermissionProfile andNotDM() {
		verification = verification == null
					? (member, guild) -> isNotDM(guild)
					: verification.and( (member, guild) -> isNotDM(guild));
		return this;
	}
	
	public PermissionProfile orNotDM() {
		verification = verification == null
					? (member, guild) -> isNotDM(guild)
					: verification.or( (member, guild) -> isNotDM(guild));
		return this;
	}
	
	public PermissionProfile andNotGuild() {
		verification = verification == null
					? (member, guild) -> isNotGuild(guild)
					: verification.and( (member, guild) -> isNotGuild(guild));
		return this;
	}
	
	public PermissionProfile orNotGuild() {
		verification = verification == null
					? (member, guild) -> isNotGuild(guild)
					: verification.or( (member, guild) -> isNotGuild(guild));
		return this;
	}
	
	public PermissionProfile andOwner() {
		verification = verification == null
					? (member, guild) -> isOwner(member, guild)
					: verification.and( (member, guild) -> isOwner(member, guild));
		return this;
	}
	
	public PermissionProfile orOwner() {
		verification = verification == null
					? (member, guild) -> isOwner(member, guild)
					: verification.or( (member, guild) -> isOwner(member, guild));
		return this;
	}
	
	/* Helper Functions */
	public static synchronized boolean hasPermission( Member member, Guild guild, Permission permission ) {
		if( guild == null ) {
			return true;
		}
		return member.getBasePermissions().block().contains(permission);
	}
	
	public static synchronized boolean fromUser( Member member ) {
		return !member.isBot();
	}
	
	public static synchronized boolean isDeveloper( Member member ) {
		for( long id : Constants.DEVELOPERS ) {
			if( id == member.getId().asLong() ) {
				return true;
			}
		}
		return false;
	}
	
	
	public static synchronized boolean isOwner( Member member, Guild guild ) {
		if( guild == null ) {
			return true;
		}
		return guild.getOwner().block().getId().equals(member.getId());
	}
	
	public static synchronized boolean isNotDM( Guild guild ) {
		return guild != null;
	}
	
	public static synchronized boolean isNotGuild( Guild guild ) {
		return guild == null;
	}
	
}
