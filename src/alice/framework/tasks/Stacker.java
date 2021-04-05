package alice.framework.tasks;

import alice.framework.main.Monoable;
import reactor.core.publisher.Mono;

public class Stacker implements Monoable {

	protected Mono<?> sequence;
	
	public Stacker() {
		sequence = Mono.fromRunnable(() -> {});
	}

	public void append(Monoable monoable) {
		this.sequence = this.sequence.and(monoable.toMono());
	}
	
	@Override
	public Mono<?> toMono() {
		return sequence;
	}
	
}
