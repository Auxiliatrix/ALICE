package alice.framework.main;

import reactor.core.publisher.Mono;

public interface Monoable {
	public Mono<?> toMono();
}
