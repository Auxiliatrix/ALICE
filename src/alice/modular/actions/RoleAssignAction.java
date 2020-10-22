package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import reactor.core.publisher.Mono;

public class RoleAssignAction extends Action {
	
	public RoleAssignAction(Mono<Member> target, Role role) {
		super(target.block().addRole(role.getId()));
	}
	
}
