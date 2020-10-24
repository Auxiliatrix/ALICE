package alice.framework.interactives;

import discord4j.core.object.reaction.Reaction;

public abstract class Interactive {
	
	public Interactive() {
		
	}
	
	public abstract void update(Reaction reaction);
	
}
