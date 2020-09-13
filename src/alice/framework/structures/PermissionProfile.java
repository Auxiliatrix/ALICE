package alice.framework.structures;

import java.util.function.Predicate;

import discord4j.core.object.entity.Member;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class PermissionProfile {
	
	private Predicate<Mono<Member>> verification; 
	
	public PermissionProfile() {
		verification = null;
	}
	
	public boolean verify(Mono<Member> member) {
		return verification.test(member);
	}
	
	public PermissionProfile and(Permission permission) {
		verification = verification == null 
							? member -> { return member.block().getBasePermissions().block().contains(permission); } 
							: member -> { return verification.test(member) && member.block().getBasePermissions().block().contains(permission); };
		return this;
	}
	
	public PermissionProfile or(Permission permission) {
		verification = verification == null 
							? member -> { return member.block().getBasePermissions().block().contains(permission); } 
							: member -> { return verification.test(member) || member.block().getBasePermissions().block().contains(permission); };
		return this;
	}
	
}
