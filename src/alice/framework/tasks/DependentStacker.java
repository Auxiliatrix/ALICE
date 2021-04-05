package alice.framework.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import reactor.core.publisher.Mono;

public class DependentStacker<T> extends Stacker {

	private Mono<T> mono;
	private List<Function<T, Mono<?>>> tasks;
	private List<Consumer<T>> effects;
		
	public DependentStacker(Mono<T> mono) {
		super();
		
		this.mono = mono;
		this.tasks = new ArrayList<Function<T, Mono<?>>>();
		this.effects = new ArrayList<Consumer<T>>();		
	}
	
	public void addTask(Function<T, Mono<?>> task) {
		tasks.add(task);
	}
	
	public void addEffect(Consumer<T> consumer) {
		effects.add(consumer);
	}
	
	@Override
	public Mono<?> toMono() {
		return super.toMono().and(mono.flatMap(t -> {
			Mono<Void> process = Mono.fromRunnable(() -> {
				for( Consumer<T> effect : effects ) {
					effect.accept(t);
				}
			});
			
			for( Function<T, Mono<?>> task : tasks ) {
				process = process.and(task.apply(t));
			}
			
			return process;
		}));
	}
	
}
