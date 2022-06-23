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
		executeOrder.add(subcommands);
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
		checkOrder.add(independentConditions);
		independentConditions.add(condition);
		return this;
	}
	
	public Command<E> withCondition(Boolean condition) {
		checkOrder.add(conditions);
		conditions.add(condition);
		return this;
	}
	
	protected boolean checkConditions(E t) {
		Mono<Dependency<E>> dependency = dependencies.getDependency(t);

		Iterator<Boolean> conditionsIterator = conditions.iterator();
		Iterator<Function<E, Boolean>> independentConditionsIterator = independentConditions.iterator();
		Iterator<Function<Dependency<E>, Boolean>> dependentConditionsIterator = dependentConditions.iterator();
		
		
		for( List<?> conditionList : checkOrder ) {
			if( conditionList.equals(conditions) ) {
				if( !conditionsIterator.next() ) {
					return false;
				}
			}
			if( conditionList.equals(independentConditions) ) {
				if( !independentConditionsIterator.next().apply(t) ) {
					return false;
				}
			}
			if( conditionList.equals(dependentConditions) ) {
				if( !dependentConditionsIterator.next().apply(dependency.block()) ) {
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
				result = result.and(dependentEffectsIterator.next().apply(dependency.block()));
			}
			if( effectList.equals(independentEffects) ) {
				result = result.and(independentEffectsIterator.next().apply(t));

			}
			if( effectList.equals(dependentSideEffects) ) {
				result = result.and(Mono.fromRunnable(() -> {dependentSideEffectsIterator.next().accept(dependency.block());}));
			}
			if( effectList.equals(independentSideEffects) ) {
				result = result.and(Mono.fromRunnable(() -> {independentSideEffectsIterator.next().accept(t);}));
			}
			if( effectList.equals(suppliers) ) {
				result = result.and(suppliersIterator.next().get());
			}
		}
		
//		for( Function<Dependency<E>, Mono<?>> effect : dependentEffects ) {
//			result = result.and(effect.apply(dependency.block()));
//		}
//		
//		for( Function<E, Mono<?>> effect : independentEffects ) {
//			result = result.and(effect.apply(t));
//		}
//		
//		for( Consumer<Dependency<E>> effect : dependentSideEffects ) {
//			result = result.and(Mono.fromRunnable(() -> {effect.accept(dependency.block());}));
//		}
//		
//		for( Consumer<E> effect : independentSideEffects ) {
//			result = result.and(Mono.fromRunnable(() -> {effect.accept(t);}));
//		}
//		
//		for( Supplier<Mono<?>> effect : suppliers ) {
//			result = result.and(effect.get());
//		}
		
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
