package alice.framework.dependencies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public class Command<E extends Event> implements Function<E, Mono<?>> {
	
	protected DependencyFactory<E> dependencies;
	
	protected List<Function<E, Mono<?>>> independentEffects;
	protected List<Function<DependencyMap<E>, Mono<?>>> dependentEffects;
	protected List<Consumer<E>> independentSideEffects;
	protected List<Consumer<DependencyMap<E>>> dependentSideEffects;
	protected List<Supplier<Mono<?>>> suppliers;
	
	protected List<Function<E, Boolean>> independentConditions;
	protected List<Function<DependencyMap<E>, Boolean>> dependentConditions;
	protected List<Boolean> conditions;
	
	protected List<Command<E>> subcommands;
	
	protected List<List<?>> checkOrder;
	protected List<List<?>> executeOrder;
	
	
	public Command(DependencyFactory<E> dependencies, @SuppressWarnings("unchecked") Command<E>...subcommands) {
		this(dependencies);
		for( Command<E> subcommand : subcommands ) {
			withSubcommand(subcommand);
		}
	}
	
	public Command(DependencyFactory<E> dependencies) {
		this.dependencies = dependencies;
		this.independentEffects = new ArrayList<Function<E, Mono<?>>>();
		this.dependentEffects = new ArrayList<Function<DependencyMap<E>, Mono<?>>>();
		this.independentSideEffects = new ArrayList<Consumer<E>>();
		this.dependentSideEffects = new ArrayList<Consumer<DependencyMap<E>>>();
		this.suppliers = new ArrayList<Supplier<Mono<?>>>();
		this.independentConditions = new ArrayList<Function<E, Boolean>>();
		this.dependentConditions = new ArrayList<Function<DependencyMap<E>, Boolean>>();
		this.conditions = new ArrayList<Boolean>();
		
		checkOrder = new ArrayList<List<?>>();
		executeOrder = new ArrayList<List<?>>();
		
		subcommands = new ArrayList<Command<E>>();
	}
	
	// TODO: conditions with things to execute on a failure
	
	public Command<E> addSubcommand() {
		Command<E> subcommand = new Command<E>(dependencies);
		withSubcommand(subcommand);
		return subcommand;
	}
	
	public Command<E> addSubcommand(DependencyFactory<E> dependencies) {
		Command<E> subcommand = new Command<E>(dependencies);
		withSubcommand(subcommand);
		return subcommand;
	}
	
	public Command<E> withSubcommand(Command<E> subcommand) {
		subcommands.add(subcommand);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public Command<E> withSubcommands(Command<E>... subcommands) {
		for( Command<E> subcommand : subcommands ) {
			withSubcommand(subcommand);
		}
		return this;
	}
	
	public Command<E> withDependentEffect(Function<DependencyMap<E>, Mono<?>> dependentEffect) {
		dependentEffects.add(dependentEffect);
		executeOrder.add(dependentEffects);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public Command<E> withDependentEffects(Function<DependencyMap<E>, Mono<?>>... dependentEffects) {
		for( Function<DependencyMap<E>, Mono<?>> dependentEffect : dependentEffects ) {
			withDependentEffect(dependentEffect);
		}
		return this;
	}
	
	public Command<E> withDependentSideEffect(Consumer<DependencyMap<E>> effect) {
		dependentSideEffects.add(effect);
		executeOrder.add(dependentSideEffects);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public Command<E> withDependentSideEffects(Consumer<DependencyMap<E>>... effects) {
		for( Consumer<DependencyMap<E>> effect : effects ) {
			withDependentSideEffect(effect);
		}
		return this;
	}
	
	public Command<E> withEffect(Function<E, Mono<?>> effect) {
		independentEffects.add(effect);
		executeOrder.add(independentEffects);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public Command<E> withEffects(Function<E, Mono<?>>... effects) {
		for( Function<E, Mono<?>> effect : effects ) {
			withEffect(effect);
		}
		return this;
	}
	
	public Command<E> withSideEffect(Consumer<E> effect) {
		independentSideEffects.add(effect);
		executeOrder.add(independentSideEffects);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public Command<E> withSideEffect(Consumer<E>... effects) {
		for( Consumer<E> effect : effects ) {
			withSideEffect(effect);
		}
		return this;
	}
	
	public Command<E> withEffect(Supplier<Mono<?>> effect) {
		suppliers.add(effect);
		executeOrder.add(suppliers);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public Command<E> withEffects(Supplier<Mono<?>>... effects) {
		for( Supplier<Mono<?>> effect : effects ) {
			withEffect(effect);
		}
		return this;
	}
	
	public Command<E> withDependentCondition(Function<DependencyMap<E>, Boolean> dependentCondition) {
		dependentConditions.add(dependentCondition);
		checkOrder.add(dependentConditions);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public Command<E> withDependentConditions(Function<DependencyMap<E>, Boolean>... dependentConditions) {
		for( Function<DependencyMap<E>, Boolean> dependentCondition : dependentConditions ) {
			withDependentCondition(dependentCondition);
		}
		return this;
	}
	
	public Command<E> withCondition(Function<E, Boolean> condition) {
		independentConditions.add(condition);
		checkOrder.add(independentConditions);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public Command<E> withConditions(Function<E, Boolean>... conditions) {
		for( Function<E, Boolean> condition : conditions ) {
			withCondition(condition);
		}
		return this;
	}
	
	public Command<E> withCondition(Boolean condition) {
		conditions.add(condition);
		checkOrder.add(conditions);
		return this;
	}
	
	public Command<E> withConditions(Boolean... conditions) {
		for( Boolean condition : conditions ) {
			withCondition(condition);
		}
		return this;
	}
	
	protected boolean checkConditions(E t) {
		Mono<DependencyMap<E>> dependency = dependencies.buildDependencyMap(t);

		Iterator<Boolean> conditionsIterator = conditions.iterator();
		Iterator<Function<E, Boolean>> independentConditionsIterator = independentConditions.iterator();
		Iterator<Function<DependencyMap<E>, Boolean>> dependentConditionsIterator = dependentConditions.iterator();
		
		
		for( List<?> conditionList : checkOrder ) {
			if( conditionList.equals(conditions) ) {
				try {
					if( !conditionsIterator.next() ) {
						return false;
					}
				} catch (Exception e) {
					return false;
				}
			}
			if( conditionList.equals(independentConditions) ) {
				try {
					if( !independentConditionsIterator.next().apply(t) ) {
						return false;
					}
				} catch (Exception e) {
					return false;
				}
			}
			if( conditionList.equals(dependentConditions) ) {
				try {
					if( !dependentConditionsIterator.next().apply(dependency.block()) ) {
						return false;
					}
				} catch (Exception e) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	protected Mono<?> executeEffects(E t) {	
		Mono<?> result = Mono.fromRunnable(() -> {});
		Mono<DependencyMap<E>> dependency = dependencies.buildDependencyMap(t);
		
		Iterator<Function<DependencyMap<E>, Mono<?>>> dependentEffectsIterator = dependentEffects.iterator();
		Iterator<Function<E, Mono<?>>> independentEffectsIterator = independentEffects.iterator();
		Iterator<Consumer<DependencyMap<E>>> dependentSideEffectsIterator = dependentSideEffects.iterator();
		Iterator<Consumer<E>> independentSideEffectsIterator = independentSideEffects.iterator();
		Iterator<Supplier<Mono<?>>> suppliersIterator = suppliers.iterator();
		
		for( List<?> effectList : executeOrder ) {
			if( effectList.equals(dependentEffects) ) {
				try {
					result = result.and(dependentEffectsIterator.next().apply(dependency.block()));
				} catch (Exception e) {
					result = Mono.fromRunnable(() -> {System.err.println("Fatal error while executing for " + t.getClass());});
					break;
				}
			}
			if( effectList.equals(independentEffects) ) {
				try {
					result = result.and(independentEffectsIterator.next().apply(t));
				} catch (Exception e) {
					result = Mono.fromRunnable(() -> {System.err.println("Fatal error while executing for " + t.getClass());});
					break;
				}
			}
			if( effectList.equals(dependentSideEffects) ) {
				try {
					result = result.and(Mono.fromRunnable(() -> {dependentSideEffectsIterator.next().accept(dependency.block());}));
				} catch (Exception e) {
					result = Mono.fromRunnable(() -> {System.err.println("Fatal error while executing for " + t.getClass());});
					break;
				}
			}
			if( effectList.equals(independentSideEffects) ) {
				try {
					result = result.and(Mono.fromRunnable(() -> {independentSideEffectsIterator.next().accept(t);}));
				} catch (Exception e) {
					result = Mono.fromRunnable(() -> {System.err.println("Fatal error while executing for " + t.getClass());});
					break;
				}
			}
			if( effectList.equals(suppliers) ) {
				result = result.and(suppliersIterator.next().get());
			}
		}
		
		return result;
	}
	
	@Override
	public Mono<?> apply(E t) {
		Mono<?> result = Mono.fromRunnable(() -> {});
		if( checkConditions(t) ) {
			boolean overidden = false;
			for( Command<E> subcommand : subcommands ) {
				if( subcommand.checkConditions(t) ) {
					overidden = true;
					result = result.and(subcommand.apply(t));
				}
			}
			
			if( !overidden ) {
				result = result.and(executeEffects(t));
			}
		}
		
		return result;
	}
	
}
