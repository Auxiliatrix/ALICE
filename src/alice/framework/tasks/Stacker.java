package alice.framework.tasks;

import alice.framework.main.Brain;
import reactor.core.publisher.Mono;

/**
 * A wrapper class for Mono that allows for easy and dynamic stacking of Monos.
 * @author Auxiliatrix
 *
 */
public class Stacker implements Monoable {

	/**
	 * The Mono being stacked upon.
	 */
	protected Mono<?> sequence;
	
	/**
	 * Construct a Stacker object with a Mono that initially does nothing.
	 */
	public Stacker() {
		sequence = Mono.fromRunnable(() -> {});
	}

	/**
	 * Add a Monoable object to the stack of Monos.
	 * @param monoable Monoable object to add
	 */
	public void append(Monoable mono) {
		this.sequence = this.sequence.and(mono.toMono());
	}
	
	public void append(Mono<?> mono) {
		this.sequence = this.sequence.and(mono);
	}
	
	public void append(Runnable runnable) {
		this.sequence = this.sequence.and(Mono.fromRunnable(runnable));
	}
	
	@Override
	public Mono<?> toMono() {
		return sequence
				.doOnError(e -> {
					System.err.println("Propagated error detected. Reconnecting client.");
					Brain.client.logout().block();
				});
//				.onErrorContinue((e, o) -> {
//						e.printStackTrace(); System.err.println("Unhandled error caught during execution of Stacker.");
//					});
	}
	
}
