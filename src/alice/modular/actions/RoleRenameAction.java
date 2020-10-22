package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Role;

public class RoleRenameAction extends Action {
	
	public RoleRenameAction( Role role, String newName ) {
		super(role.edit(res -> res.setName(newName)));
	}
	
}
