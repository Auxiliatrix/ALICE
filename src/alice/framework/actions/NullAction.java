package alice.framework.actions;

import reactor.core.publisher.Mono;

public class NullAction extends Action {

	public NullAction() {
		super( Mono.fromRunnable( () -> {} ) );
	}
	
}
