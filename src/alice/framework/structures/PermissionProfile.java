package alice.framework.structures;

import java.util.function.BiPredicate;

import alice.framework.main.Constants;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Permission;

/**
 * An objective set of restrictions used to prevent certain users from accessing certain Features.
 * @author Auxiliatrix
 *
 */
public class PermissionProfile {
	
	/* Internal Verification BiPredicate */
	/**
	 * A BiPredicate function that is constructed upon to create a function which will check whether a given Member in a given Guild passes the restrictions being enforced.
	 */
	private BiPredicate<Member, Guild> verification; 
	
	/* Constructors */
	/**
	 * Construct a PermissionProfile with no initial restrictions.
	 */
	public PermissionProfile() {
		this(null);
	}
	
	/**
	 * Constructs a PermissionProfile with a given preset.
	 * @param preset Verification function to use as a preset.
	 */
	private PermissionProfile(BiPredicate<Member, Guild> preset) {
		verification = preset;
	}
	
	/**
	 * Verify whether a given Member in a given Guild meets the set restrictions.
	 * @param member Member to test
	 * @param guild Guild to test the Member in
	 * @return whether or not the Member meets the restrictions
	 */
	public boolean verify(Member member, Guild guild) {
		return verification.test(member, guild) || isDeveloper(member);
	}
	
	/* Factory Methods */
	/**
	 * Preset that allows anyone to meet the restrictions.
	 * @return a Permission Profile preset.
	 */
	public static PermissionProfile getAnyonePreset() {
		return new PermissionProfile( (member, guild) -> true );
	}
	
	/**
	 * Preset that allows only admins to meet the restrictions.
	 * @return a Permission Profile preset.
	 */
	public static PermissionProfile getAdminPreset() {
		return new PermissionProfile( (member, guild) -> hasPermission(member, guild, Permission.ADMINISTRATOR) );
	}

	/**
	 * Preset that allows only server owners to meet the restrictions.
	 * @return a Permission Profile preset.
	 */
	public static PermissionProfile getOwnerPreset() {
		return new PermissionProfile( (member, guild) -> isOwner(member, guild) );
	}
	
	/**
	 * Preset that allows only the developers of this bot to meet the restrictions.
	 * @return a Permission Profile preset.
	 */
	public static PermissionProfile getDeveloperPreset() {
		return new PermissionProfile( (member, guild) -> isDeveloper(member) ) ;
	}
	
	/* Builder Methods */
	/**
	 * Adds a Permission that a Member must have in order to meet the restrictions.
	 * @param permission Permission to add
	 * @return the modified PermissionProfile
	 */
	public PermissionProfile and(Permission permission) {
		verification = verification == null 
							? (member, guild) -> hasPermission(member, guild, permission)
							: verification.and( (member, guild) -> hasPermission(member, guild, permission));
		return this;
	}
	
	/**
	 * Adds an alternative Permission a user could have in order to meet the restrictions.
	 * @param permission Permission to add
	 * @return the modified PermissionProfile
	 */
	public PermissionProfile or(Permission permission) {
		verification = verification == null 
							? (member, guild) -> hasPermission(member, guild, permission)
							: verification.or( (member, guild) -> hasPermission(member, guild, permission));
		return this;
	}
	
	/**
	 * Adds that the Member must be a User to meet the restrictions.
	 * @return the modified PermissionProfile
	 */
	public PermissionProfile andFromUser() {
		verification = verification == null
							? (member, guild) -> fromUser(member)
							: verification.and( (member, guild) -> fromUser(member));
		return this;
	}
	
	/**
	 * Adds that the Member can also be a User to meet the restrictions.
	 * @return the modified PermissionProfile
	 */
	public PermissionProfile orFromUser() {
		verification = verification == null
							? (member, guild) -> fromUser(member)
							: verification.or( (member, guild) -> fromUser(member));
		return this;
	}
	
	/**
	 * Adds that the Member must also be a Developer to meet the restrictions.
	 * @return the modified PermissionProfile
	 */
	public PermissionProfile andDeveloper() {
		verification = verification == null
						? (member, guild) -> isDeveloper(member)
						: verification.and( (member, guild) -> isDeveloper(member));
		return this;
	}
	
	/**
	 * Adds that the Member can also be a Developer to meet the restrictions.
	 * @return the modified PermissionProfile
	 */
	public PermissionProfile orDeveloper() {
		verification = verification == null
						? (member, guild) -> isDeveloper(member)
						: verification.or( (member, guild) -> isDeveloper(member));
		return this;
	}
	
	/**
	 * Adds that the Member must not be in a Direct Message to meet the restrictions.
	 * @return the modified PermissionProfile
	 */
	public PermissionProfile andNotDM() {
		verification = verification == null
					? (member, guild) -> isNotDM(guild)
					: verification.and( (member, guild) -> isNotDM(guild));
		return this;
	}
	
	/**
	 * Adds that the Member can also not be in a Direct Message to meet the restrictions.
	 * @return the modified PermissionProfile
	 */
	public PermissionProfile orNotDM() {
		verification = verification == null
					? (member, guild) -> isNotDM(guild)
					: verification.or( (member, guild) -> isNotDM(guild));
		return this;
	}
	
	/**
	 * Adds that the Member must not be in a Guild to meet the restrictions.
	 * @return the modified PermissionProfile
	 */
	public PermissionProfile andNotGuild() {
		verification = verification == null
					? (member, guild) -> isNotGuild(guild)
					: verification.and( (member, guild) -> isNotGuild(guild));
		return this;
	}
	
	/**
	 * Adds that the Member must not be in a Guild to meet the restrictions.
	 * @return the modified PermissionProfile
	 */
	public PermissionProfile orNotGuild() {
		verification = verification == null
					? (member, guild) -> isNotGuild(guild)
					: verification.or( (member, guild) -> isNotGuild(guild));
		return this;
	}
	
	/**
	 * Adds that the Member must also be the Guild Owner to meet the restrictions.
	 * @return the modified PermissionProfile
	 */
	public PermissionProfile andOwner() {
		verification = verification == null
					? (member, guild) -> isOwner(member, guild)
					: verification.and( (member, guild) -> isOwner(member, guild));
		return this;
	}
	
	/**
	 * Adds that the Member can also be the Guild Owner to meet the restrictions.
	 * @return the modified PermissionProfile
	 */
	public PermissionProfile orOwner() {
		verification = verification == null
					? (member, guild) -> isOwner(member, guild)
					: verification.or( (member, guild) -> isOwner(member, guild));
		return this;
	}
	
	/* Helper Functions */
	/**
	 * Checks whether the given Member has the given Permission in the given Guild
	 * @param member Member to check
	 * @param guild Guild to check in
	 * @param permission Permission to check for
	 * @return whether the Member has this Permission
	 */
	public static synchronized boolean hasPermission( Member member, Guild guild, Permission permission ) {
		if( guild == null ) {
			return true;
		}
		return member.getBasePermissions().block().contains(permission);
	}
	
	/**
	 * Checks whether a given Member is a User.
	 * @param member Member to check
	 * @return whether or not the Member is a User
	 */
	public static synchronized boolean fromUser( Member member ) {
		return !member.isBot();
	}
	
	/**
	 * Checks whether a given User is one of the bot's developers.
	 * @param user User to check
	 * @return whether or not the User is one of the bot's developers
	 */
	public static synchronized boolean isDeveloper( User user ) {
		for( long id : Constants.DEVELOPERS ) {
			if( id == user.getId().asLong() ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks whether the given Member is the owner of the given Guild
	 * @param member Member to check
	 * @param guild Guild to check in
	 * @return whether the Member is the Guild owner
	 */
	public static synchronized boolean isOwner( Member member, Guild guild ) {
		if( guild == null ) {
			return true;
		}
		return guild.getOwner().block().getId().equals(member.getId());
	}
	
	/**
	 * Checks whether the given Guild is not a Direct Message
	 * @param guild Guild to check in
	 * @return whether the given Guild is not a Direct Message
	 */
	public static synchronized boolean isNotDM( Guild guild ) {
		return guild != null;
	}
	
	/**
	 * Checks whether the given Guild is a Direct Message
	 * @param guild Guid the check in
	 * @return whether the given Guild is a Direct Message
	 */
	public static synchronized boolean isNotGuild( Guild guild ) {
		return guild == null;
	}
	
}
