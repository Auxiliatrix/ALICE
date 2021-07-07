package alice.framework.tasks;

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
	public void append(Monoable monoable) {
		this.sequence = this.sequence.and(monoable.toMono());
	}
	
	@Override
	public Mono<?> toMono() {
		return sequence;
	}
	
}
