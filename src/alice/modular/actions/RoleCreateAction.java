package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Guild;
import discord4j.rest.util.Color;

public class RoleCreateAction extends Action {

	public RoleCreateAction(Guild guild, String roleName, Color color) {
		super(guild.createRole(c -> c.setName(roleName).setColor(color)));
	}
	
	public RoleCreateAction(Guild guild, String roleName, boolean mentionable) {
		super(guild.createRole(c -> c.setName(roleName).setMentionable(mentionable)));
	}
	
	public RoleCreateAction(Guild guild, String roleName, Color color, boolean mentionable) {
		super(guild.createRole(c -> c.setName(roleName).setColor(color).setMentionable(mentionable)));
	}

}
