package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import reactor.core.publisher.Mono;

public class AssignRoleAction extends Action {
	
	public AssignRoleAction(Mono<Member> target, Role role) {
		super();
		this.mono = target.block().addRole(role.getId());
	}
	
}
