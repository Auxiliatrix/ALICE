package alice.framework.modules.commands;

import java.util.ArrayList;
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
	
	protected List<Command<E>> subcommands;
	
	public Command(DependencyFactory<E> dependencies) {
		this.dependencies = dependencies;
		this.independentEffects = new ArrayList<Function<E, Mono<?>>>();
		this.dependentEffects = new ArrayList<Function<Dependency<E>, Mono<?>>>();
		this.independentSideEffects = new ArrayList<Consumer<E>>();
		this.dependentSideEffects = new ArrayList<Consumer<Dependency<E>>>();
		this.suppliers = new ArrayList<Supplier<Mono<?>>>();
		this.independentConditions = new ArrayList<Function<E, Boolean>>();
		this.dependentConditions = new ArrayList<Function<Dependency<E>, Boolean>>();
		
		subcommands = new ArrayList<Command<E>>();
	}
	
	public Command<E> withSubcommands(Command<E> subcommand) {
		subcommands.add(subcommand);
		return this;
	}
	
	public Command<E> withDependentEffect(Function<Dependency<E>, Mono<?>> dependentEffect) {
		dependentEffects.add(dependentEffect);
		return this;
	}
	
	public Command<E> withDependentEffect(Consumer<Dependency<E>> effect) {
		dependentSideEffects.add(effect);
		return this;
	}
	
	public Command<E> withEffect(Function<E, Mono<?>> effect) {
		independentEffects.add(effect);
		return this;
	}
	
	public Command<E> withEffect(Consumer<E> effect) {
		independentSideEffects.add(effect);
		return this;
	}
	
	public Command<E> withEffect(Supplier<Mono<?>> effect) {
		suppliers.add(effect);
		return this;
	}
	
	public Command<E> withDependentCondition(Function<Dependency<E>, Boolean> dependentCondition) {
		dependentConditions.add(dependentCondition);
		return this;
	}
	
	public Command<E> withCondition(Function<E, Boolean> condition) {
		independentConditions.add(condition);
		return this;
	}
	
	protected boolean checkConditions(E t) {
		Mono<Dependency<E>> dependency = dependencies.getDependency(t);
		
		boolean result = true;
		
		for( Function<Dependency<E>, Boolean> condition : dependentConditions ) {
			result &= condition.apply(dependency.block());
		}
		
		for( Function<E, Boolean> condition : independentConditions ) {
			result &= condition.apply(t);
		}
		
		return result;
	}
	
	protected Mono<?> executeEffects(E t) {
		Mono<Dependency<E>> dependency = dependencies.getDependency(t);
		
		Mono<?> result = Mono.fromRunnable(() -> {});
		
		for( Function<Dependency<E>, Mono<?>> effect : dependentEffects ) {
			result = result.and(effect.apply(dependency.block()));
		}
		
		for( Function<E, Mono<?>> effect : independentEffects ) {
			result = result.and(effect.apply(t));
		}
		
		for( Consumer<Dependency<E>> effect : dependentSideEffects ) {
			result = result.and(Mono.fromRunnable(() -> {effect.accept(dependency.block());}));
		}
		
		for( Consumer<E> effect : independentSideEffects ) {
			result = result.and(Mono.fromRunnable(() -> {effect.accept(t);}));
		}
		
		for( Supplier<Mono<?>> effect : suppliers ) {
			result = result.and(effect.get());
		}
		
		return result;
	}
	
	@Override
	public Mono<?> apply(E t) {
		Mono<?> result = Mono.fromRunnable(() -> {});
		
		boolean overidden = false;
		for( Command<E> subcommand : subcommands ) {
			if( subcommand.checkConditions(t) ) {
				overidden = true;
				result = result.and(subcommand.apply(t));
			}
		}
		
		if( !overidden && checkConditions(t) ) {
			result = result.and(executeEffects(t));
		}
		
		return result;
	}
	
}
