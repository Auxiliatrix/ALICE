package alice.framework.tasks;

import reactor.core.publisher.Mono;

public interface Monoable {
	public Mono<?> toMono();
}
