package alice.framework.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import reactor.core.publisher.Mono;

/**
 * A Monoable Stacker that stacks Tasks that will generate a different response depending on the result of a given dependency Mono.
 * This structure is used to ensure that a Mono does not have to be executed before returning a Mono from a function designed to return a single Mono.
 * @author Auxiliatrix
 *
 * @param <T> Return type of the Mono being depended on
 */
public class DependentStacker<T> extends Stacker {

	/**
	 * The Mono that the stacked Monoables depend on.
	 */
	private Mono<T> dependency;
	
	/**
	 * Tasks that will produce an executable response based on the results of the dependency Mono.
	 */
	private List<Function<T, Mono<?>>> tasks;
	
	/**
	 * Consumer functions that will execute based on the results of the dependency Mono.
	 */
	private List<Consumer<T>> effects;
		
	/**
	 * Constructs a DependentStacker based on a Mono whos execution is being depended on.
	 * @param dependency Mono to depend on
	 */
	public DependentStacker(Mono<T> dependency) {
		super();
		
		this.dependency = dependency;
		this.tasks = new ArrayList<Function<T, Mono<?>>>();
		this.effects = new ArrayList<Consumer<T>>();		
	}
	
	/**
	 * Stacks a Task whose executable response depends on the dependency mono.
	 * @param task Task to stack
	 */
	public void addTask(Function<T, Mono<?>> task) {
		tasks.add(task);
	}
	
	/**
	 * Stacks an effect which depends on the dependency Mono
	 * @param consumer Consumer effect to stack
	 */
	public void addEffect(Consumer<T> consumer) {
		effects.add(consumer);
	}
	
	@Override
	public Mono<?> toMono() {
		return super.toMono().and(dependency.flatMap(t -> {
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
