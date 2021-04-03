package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Role;
import discord4j.rest.util.Color;

public class RoleEditAction extends Action {

	public RoleEditAction(Role role, Color color) {
		super(role.edit(c -> c.setColor(color)));
	}
	
}
