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
	
	public static PermissionProfile getNotBotPreset() {
		return new PermissionProfile( (user, guild) -> !isBot(user, guild) );
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
	
	public PermissionProfile andNotBot() {
		verification = verification == null
							? (user, guild) -> isBot(user, guild)
							: (user, guild) -> verification.test(user, guild) && isBot(user, guild);
		return this;
	}
	
	public PermissionProfile orNotBot() {
		verification = verification == null
							? (user, guild) -> isBot(user, guild)
							: (user, guild) -> verification.test(user, guild) || isBot(user, guild);
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
	
	/* Helper Functions */
	private static boolean hasPermission( Optional<User> user, Mono<Guild> guild, Permission permission ) {
		if( user.isEmpty() ) {
			return false;
		}
		if( guild.block() == null ) {
			return true;
		}
		return user.get().asMember(guild.block().getId()).block().getBasePermissions().block().contains(permission);
	}
	
	private static boolean isBot( Optional<User> user, Mono<Guild> guild ) {
		if( user.isEmpty() ) {
			return false;
		}
		return user.get().isBot();
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
	
}
