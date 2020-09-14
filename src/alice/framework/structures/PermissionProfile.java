package alice.framework.structures;

import java.util.function.Predicate;

import discord4j.core.object.entity.Member;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class PermissionProfile {
	
	/* Internal Verification Predicate */
	private Predicate<Mono<Member>> verification; 
	
	/* Constructors */
	public PermissionProfile() {
		this(null);
	}
	
	private PermissionProfile(Predicate<Mono<Member>> preset) {
		verification = preset;
	}
	
	/* Primary Use Function */
	public boolean verify(Mono<Member> member) {
		return verification.test(member);
	}
	
	/* Factory Methods */
	public static PermissionProfile getAnyonePreset() {
		return new PermissionProfile(member -> true);
	}
	
	public static PermissionProfile getAdminPreset() {
		return new PermissionProfile(member -> hasPermission(member, Permission.ADMINISTRATOR));
	}
	
	public static PermissionProfile getNotBotPreset() {
		return new PermissionProfile(member -> !isBot(member));
	}
	
	/* Builder Methods */
	public PermissionProfile and(Permission permission) {
		verification = verification == null 
							? member -> hasPermission(member, permission)
							: member -> verification.test(member) && hasPermission(member, permission);
		return this;
	}
	
	public PermissionProfile or(Permission permission) {
		verification = verification == null 
							? member -> hasPermission(member, permission)
							: member -> verification.test(member) || hasPermission(member, permission);
		return this;
	}
	
	public PermissionProfile andNotBot() {
		verification = verification == null
							? member -> isBot(member)
							: member -> verification.test(member) && isBot(member);
		return this;
	}
	
	public PermissionProfile orNotBot() {
		verification = verification == null
							? member -> isBot(member)
							: member -> verification.test(member) || isBot(member);
		return this;
	}
	
	public PermissionProfile invert() {
		verification = member -> !verification.test(member);
		return this;
	}
	
	/* Helper Functions */
	private static boolean hasPermission( Mono<Member> member, Permission permission ) {
		return member.block().getBasePermissions().block().contains(permission);
	}
	
	private static boolean isBot( Mono<Member> member ) {
		return member.block().isBot();
	}
	
}
