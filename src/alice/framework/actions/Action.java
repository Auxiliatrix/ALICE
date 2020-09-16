package alice.framework.actions;

import reactor.core.publisher.Mono;

public abstract class Action {
	
	protected Mono<?> mono;
	
	public Action() {
		this.mono = null;
	}
	
	public final Mono<?> toMono() {
		return mono;
	}
	
	public Action addAction(Action action) {
		mono = (mono == null) ? action.toMono() : mono.and(action.toMono());
		return this;
	}
	
	public Action addMono(Mono<?> newMono) {
		mono = (mono == null) ? newMono : mono.and(newMono);
		return this;
	}
	
}
