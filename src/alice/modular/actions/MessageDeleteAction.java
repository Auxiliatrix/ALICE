package alice.modular.actions;

import alice.framework.actions.Action;
import discord4j.core.object.entity.Message;

@Deprecated
public class MessageDeleteAction extends Action {
	
	public MessageDeleteAction(Message message) {
		super(message.delete());
	}
	
}
