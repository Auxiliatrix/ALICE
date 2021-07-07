package alice.framework.tasks;

import java.util.ArrayList;
import java.util.List;

import reactor.core.publisher.Mono;

/**
 * A Mono Stacker which accepts Runnables.
 * This structure is used to group the execution of Runnables together, but it primarily intended to stack Monos/Monoables.
 * @author Auxiliatrix
 *
 */
public class IndependentStacker extends Stacker {

	/**
	 * Runnables to execute.
	 */
	private List<Runnable> runnables;
	
	/**
	 * Construct an IndependentStacker with no Runnables to begin with.
	 */
	public IndependentStacker() {
		super();
		
		runnables = new ArrayList<Runnable>();
	}

	/**
	 * Stack a Runnable.
	 * @param runnable Runnable to stack.
	 */
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
