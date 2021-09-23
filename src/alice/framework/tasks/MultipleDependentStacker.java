package alice.framework.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A DependentStacker implementation that allows for Tasks and Effects to depend on the results of multiple Monos.
 * This structure allows all the Monos used in the construction of an instance of this object to be executed at once before the production of a Mono response.
 * 
 * Consumers added to this stack can access the individual Monos' resolutions by doing .get(index) on the argument of the functional interface and casting appropriately.
 * @author Auxiliatrix
 *
 */
public class MultipleDependentStacker implements Monoable {

	//  TODO: use map instead of list
	
	/**
	 * The Mono being stacked upon. This is returned when toMono() is called.
	 */
	protected Mono<?> sequence;
	
	/**
	 * The Monos that the stacked Monoables depend on.
	 */
	private Flux<Mono<?>> dependencies;
	
	/**
	 * Tasks that will produce an executable response based on the results of the dependency Mono.
	 */
	private List<Function<List<?>, Mono<?>>> tasks;
	
	/**
	 * Consumer functions that will execute based on the results of the dependency Mono.
	 */
	private List<Consumer<List<?>>> effects;
		
	/**
	 * Constructs a DependentStacker based on a Mono whos execution is being depended on.
	 * @param dependency Mono to depend on
	 */
	public MultipleDependentStacker(Mono<?>... dependencies) {
		sequence = Mono.fromRunnable(() -> {});
		
		this.dependencies = Flux.fromArray(dependencies);
		this.tasks = new ArrayList<Function<List<?>, Mono<?>>>();
		this.effects = new ArrayList<Consumer<List<?>>>();		
	}
	
	/**
	 * Stacks a Task whose executable response depends on the dependency mono.
	 * @param task Task to stack
	 * @return Mono<?> cumulated executable response so far
	 */
	public Mono<?> addTask(Function<List<?>, Mono<?>> task) {
		tasks.add(task);
		return toMono();
	}
	
	/**
	 * Stacks an effect which depends on the dependency Mono
	 * @param consumer Consumer effect to stack
	 * @return Mono<?> cumulated executable response so far
	 */
	public Mono<?> addEffect(Consumer<List<?>> consumer) {
		effects.add(consumer);
		return toMono();
	}
	
	@Override
	public Mono<?> toMono() {
		
		return sequence.and(dependencies.collectList().flatMap(t -> {
			List<Object> converted = new ArrayList<Object>();
			for( Mono<?> o : t ) {
				try {
					converted.add(o.block());
				} catch( Exception e) {
					converted.add(null);
				}
			}
			
			Mono<Void> process = Mono.fromRunnable(() -> {
				for( Consumer<List<?>> effect : effects ) {
					effect.accept(converted);
				}
			});
			for( Function<List<?>, Mono<?>> task : tasks ) {
				process = process.and(task.apply(converted));
			}
			
			return process;
		}));
		
//		return sequence.and(dependencies.map(m -> Optional.ofNullable(m.block())).collectList().flatMap(t -> {
//			List<Object> converted = new ArrayList<Object>();
//			for( Optional<?> o : t ) {
//				if( o.isEmpty() ) {
//					converted.add(null);
//				} else {
//					converted.add(o.get());
//				}
//			}
//			
//			Mono<Void> process = Mono.fromRunnable(() -> {
//				for( Consumer<List<?>> effect : effects ) {
//					effect.accept(converted);
//				}
//			});
//			for( Function<List<?>, Mono<?>> task : tasks ) {
//				process = process.and(task.apply(converted));
//			}
//			
//			return process;
//		}));
	}
	
}
