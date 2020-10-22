package alice.framework.actions;

import reactor.core.publisher.Mono;

public class VoidAction extends Action {
	
	public VoidAction(Runnable runnable) {
		super( Mono.fromRunnable(runnable) );
	}

}
