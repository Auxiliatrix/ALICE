package alice.framework.interactives;

import alice.framework.actions.Action;
import discord4j.core.object.reaction.Reaction;

public abstract class Interactive {
	
	public Interactive() {
		
	}
	
	public abstract Action update(Reaction reaction);
	
}
