package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Role;

public class RoleDeleteAction extends Action {

	public RoleDeleteAction(Role role) {
		super(role.delete());
	}

}
