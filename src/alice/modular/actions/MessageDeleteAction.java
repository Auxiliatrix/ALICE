package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Message;

public class MessageDeleteAction extends Action {
	
	public MessageDeleteAction(Message message) {
		super();
		this.mono = message.delete();
	}
	
}
