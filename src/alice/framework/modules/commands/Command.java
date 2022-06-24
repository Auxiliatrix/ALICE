package alice.framework.modules.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import alice.framework.modules.tasks.Dependency;
import alice.framework.modules.tasks.DependencyFactory;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public class Command<E extends Event> implements Function<E, Mono<?>> {
	
	protected DependencyFactory<E> dependencies;
	
	protected List<Function<E, Mono<?>>> independentEffects;
	protected List<Function<Dependency<E>, Mono<?>>> dependentEffects;
	protected List<Consumer<E>> independentSideEffects;
	protected List<Consumer<Dependency<E>>> dependentSideEffects;
	protected List<Supplier<Mono<?>>> suppliers;
	
	protected List<Function<E, Boolean>> independentConditions;
	protected List<Function<Dependency<E>, Boolean>> dependentConditions;
	protected List<Boolean> conditions;
	
	protected List<Command<E>> subcommands;
	
	protected List<List<?>> checkOrder;
	protected List<List<?>> executeOrder;
	
	public Command(DependencyFactory<E> dependencies) {
		this.dependencies = dependencies;
		this.independentEffects = new ArrayList<Function<E, Mono<?>>>();
		this.dependentEffects = new ArrayList<Function<Dependency<E>, Mono<?>>>();
		this.independentSideEffects = new ArrayList<Consumer<E>>();
		this.dependentSideEffects = new ArrayList<Consumer<Dependency<E>>>();
		this.suppliers = new ArrayList<Supplier<Mono<?>>>();
		this.independentConditions = new ArrayList<Function<E, Boolean>>();
		this.dependentConditions = new ArrayList<Function<Dependency<E>, Boolean>>();
		this.conditions = new ArrayList<Boolean>();
		
		checkOrder = new ArrayList<List<?>>();
		executeOrder = new ArrayList<List<?>>();
		
		subcommands = new ArrayList<Command<E>>();
	}
	
	public Command<E> withSubcommand(Command<E> subcommand) {
		subcommands.add(subcommand);
		return this;
	}
	
	public Command<E> withDependentEffect(Function<Dependency<E>, Mono<?>> dependentEffect) {
		dependentEffects.add(dependentEffect);
		executeOrder.add(dependentEffects);
		return this;
	}
	
	public Command<E> withDependentEffect(Consumer<Dependency<E>> effect) {
		dependentSideEffects.add(effect);
		executeOrder.add(dependentSideEffects);
		return this;
	}
	
	public Command<E> withEffect(Function<E, Mono<?>> effect) {
		independentEffects.add(effect);
		executeOrder.add(independentEffects);
		return this;
	}
	
	public Command<E> withEffect(Consumer<E> effect) {
		independentSideEffects.add(effect);
		executeOrder.add(independentSideEffects);
		return this;
	}
	
	public Command<E> withEffect(Supplier<Mono<?>> effect) {
		suppliers.add(effect);
		executeOrder.add(suppliers);
		return this;
	}
	
	public Command<E> withDependentCondition(Function<Dependency<E>, Boolean> dependentCondition) {
		dependentConditions.add(dependentCondition);
		checkOrder.add(dependentConditions);
		return this;
	}
	
	public Command<E> withCondition(Function<E, Boolean> condition) {
		independentConditions.add(condition);
		checkOrder.add(independentConditions);
		return this;
	}
	
	public Command<E> withCondition(Boolean condition) {
		conditions.add(condition);
		checkOrder.add(conditions);
		return this;
	}
	
	protected boolean checkConditions(E t) {
		Mono<Dependency<E>> dependency = dependencies.getDependency(t);

		Iterator<Boolean> conditionsIterator = conditions.iterator();
		Iterator<Function<E, Boolean>> independentConditionsIterator = independentConditions.iterator();
		Iterator<Function<Dependency<E>, Boolean>> dependentConditionsIterator = dependentConditions.iterator();
		
		
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
		Mono<Dependency<E>> dependency = dependencies.getDependency(t);
		
		Iterator<Function<Dependency<E>, Mono<?>>> dependentEffectsIterator = dependentEffects.iterator();
		Iterator<Function<E, Mono<?>>> independentEffectsIterator = independentEffects.iterator();
		Iterator<Consumer<Dependency<E>>> dependentSideEffectsIterator = dependentSideEffects.iterator();
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
