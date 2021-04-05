package alice.framework.tasks;

import java.util.ArrayList;
import java.util.List;

import reactor.core.publisher.Mono;

public class IndependentStacker extends Stacker {

	private List<Runnable> runnables;
	
	public IndependentStacker() {
		super();
		
		runnables = new ArrayList<Runnable>();
	}

	public void addRunnable(Runnable runnable) {
		runnables.add(runnable);
	}
	
	@Override
	public Mono<?> toMono() {
		return super.toMono().and(Mono.fromRunnable(() -> {
			for( Runnable runnable : runnables ) {
				runnable.run();
			}
		}));
	}
	
}
