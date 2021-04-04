package alice.framework.tasks;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import reactor.core.publisher.Mono;

public class Wrapper<T> {

	private Mono<T> mono;
	private List<Function<T, Mono<Void>>> tasks;
	private List<Consumer<T>> effects;
	
	public Wrapper(Mono<T> mono) {
		this.mono = mono;
	}
	
	public void addTask(Function<T, Mono<Void>> task) {
		tasks.add(task);
	}
	
	public void addEffect(Consumer<T> consumer) {
		effects.add(consumer);
	}
	
	public Mono<Void> toMono() {
		return mono.flatMap(t -> {
			Mono<Void> process = Mono.fromRunnable(() -> {});
			for( Function<T, Mono<Void>> task : tasks ) {
				process.and(task.apply(t));
			}
			
			for( Consumer<T> effect : effects ) {
				effect.accept(t);
			}
			
			return process;
		});
	}
	
}
