package alice.framework.structures;

import java.util.Optional;
import java.util.function.BiPredicate;

import alice.configuration.calibration.Constants;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class PermissionProfile {
	
	/* Internal Verification BiPredicate */
	private BiPredicate<Optional<User>, Mono<Guild>> verification; 
	
	/* Constructors */
	public PermissionProfile() {
		this(null);
	}
	
	private PermissionProfile(BiPredicate<Optional<User>, Mono<Guild>> preset) {
		verification = preset;
	}
	
	/* Primary Use Function */
	public boolean verify(Optional<User> user, Mono<Guild> guild) {
		if( user.isEmpty() ) {
			return false;
		}
		return verification.test(user, guild) || isDeveloper(user);
	}
	
	/* Factory Methods */
	public static PermissionProfile getAnyonePreset() {
		return new PermissionProfile( (user, guild) -> true );
	}
	
	public static PermissionProfile getAdminPreset() {
		return new PermissionProfile( (user, guild) -> hasPermission(user, guild, Permission.ADMINISTRATOR) );
	}

	public static PermissionProfile getOwnerPreset() {
		return new PermissionProfile( (user, guild) -> isOwner(user, guild) );
	}
	
	public static PermissionProfile getDeveloperPreset() {
		return new PermissionProfile( (user, guild) -> isDeveloper(user) ) ;
	}
	
	/* Builder Methods */
	public PermissionProfile and(Permission permission) {
		verification = verification == null 
							? (user, guild) -> hasPermission(user, guild, permission)
							: (user, guild) -> verification.test(user, guild) && hasPermission(user, guild, permission);
		return this;
	}
	
	public PermissionProfile or(Permission permission) {
		verification = verification == null 
							? (user, guild) -> hasPermission(user, guild, permission)
							: (user, guild) -> verification.test(user, guild) || hasPermission(user, guild, permission);
		return this;
	}
	
	public PermissionProfile andFromUser() {
		verification = verification == null
							? (user, guild) -> fromUser(user)
							: (user, guild) -> verification.test(user, guild) && fromUser(user);
		return this;
	}
	
	public PermissionProfile orFromUser() {
		verification = verification == null
							? (user, guild) -> fromUser(user)
							: (user, guild) -> verification.test(user, guild) || fromUser(user);
		return this;
	}
	
	public PermissionProfile andDeveloper() {
		verification = verification == null
						? (user, guild) -> isDeveloper(user)
						: (user, guild) -> verification.test(user, guild) && isDeveloper(user);
		return this;
	}
	
	public PermissionProfile orDeveloper() {
		verification = verification == null
						? (user, guild) -> isDeveloper(user)
						: (user, guild) -> verification.test(user, guild) || isDeveloper(user);
		return this;
	}
	
	public PermissionProfile andNotDM() {
		verification = verification == null
					? (user, guild) -> isNotDM(user, guild)
					: (user, guild) -> verification.test(user, guild) && isNotDM(user, guild);
		return this;
	}
	
	public PermissionProfile orNotDM() {
		verification = verification == null
					? (user, guild) -> isNotDM(user, guild)
					: (user, guild) -> verification.test(user, guild) || isNotDM(user, guild);
		return this;
	}
	
	public PermissionProfile andNotGuild() {
		verification = verification == null
					? (user, guild) -> isNotGuild(user, guild)
					: (user, guild) -> verification.test(user, guild) && isNotGuild(user, guild);
		return this;
	}
	
	public PermissionProfile orNotGuild() {
		verification = verification == null
					? (user, guild) -> isNotGuild(user, guild)
					: (user, guild) -> verification.test(user, guild) || isNotGuild(user, guild);
		return this;
	}
	
	public PermissionProfile andOwner() {
		verification = verification == null
					? (user, guild) -> isOwner(user, guild)
					: (user, guild) -> verification.test(user, guild) && isOwner(user, guild);
		return this;
	}
	
	public PermissionProfile orOwner() {
		verification = verification == null
					? (user, guild) -> isOwner(user, guild)
					: (user, guild) -> verification.test(user, guild) || isOwner(user, guild);
		return this;
	}
	
	/* Helper Functions */
	public static synchronized boolean hasPermission( Optional<User> user, Mono<Guild> guild, Permission permission ) {
		if( user.isEmpty() ) {
			return false;
		}
		if( guild.block() == null ) {
			return true;
		}
		return user.get().asMember(guild.block().getId()).block().getBasePermissions().block().contains(permission);
	}
	
	public static synchronized boolean fromUser( Optional<User> user ) {
		if( user.isEmpty() ) {
			return false;
		}
		return !user.get().isBot();
	}
	
	public static synchronized boolean isDeveloper( Optional<User> user ) {
		if( user.isEmpty() ) {
			return false;
		}
		for( long id : Constants.DEVELOPER_IDS ) {
			if( id == user.get().getId().asLong() ) {
				return true;
			}
		}
		return false;
	}
	
	public static synchronized boolean isOwner( Optional<User> user, Mono<Guild> guild ) {
		if( user.isEmpty() ) {
			return false;
		}
		if( guild.block() == null ) {
			return true;
		}
		return guild.block().getOwner().block().getId().equals(user.get().getId());
	}
	
	public static synchronized boolean isNotDM( Optional<User> user, Mono<Guild> guild ) {
		return guild.block() != null;
	}
	
	public static synchronized boolean isNotGuild( Optional<User> user, Mono<Guild> guild ) {
		return guild.block() == null;
	}
	
}
