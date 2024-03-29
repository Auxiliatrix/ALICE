package alice.framework.dependencies;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import alice.framework.main.Brain;
import alice.framework.utilities.AliceLogger;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

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
	
	protected String usage;
	protected String description;
	
	protected boolean transparent;
	protected boolean passive;
	
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
		
		usage = "";
		description = "";
		transparent = false;
		passive = false;
	}
	
	// TODO: conditions with things to execute on a failure
	
	public Command<E> withDocumentation(String usage, String description) {
		this.usage = usage;
		this.description = description;
		return this;
	}
	
	public Command<E> withTransparent(boolean transparent) {
		this.transparent = transparent;
		return this;
	}
	
	public String getUsage() {
		return usage.length() > 0 ? usage : "<passive activation>";
	}
	
	public String getDescription() {
		return description.length() > 0 ? description : "<no description provided>";
	}
	
	public List<Command<E>> getDocumentedSubcommands() {
		List<Command<E>> cumulativeSubcommands = new ArrayList<Command<E>>();
		for( Command<E> subcommand : subcommands ) {
			if( subcommand.transparent ) {
				for( Command<E> subsubcommand : subcommand.getDocumentedSubcommands() ) {
					cumulativeSubcommands.add(subsubcommand);
				}
			} else if( subcommand.isDocumented() ) {
				cumulativeSubcommands.add(subcommand);
			}
		}
		return subcommands;
	}
	
	public Command<E> getSubcommand(String usage) {
		for( Command<E> subcommand : getDocumentedSubcommands() ) {
			if( subcommand.getUsage().equals(usage) ) {
				return subcommand;
			} else {
				Command<E> subsubcommand = subcommand.getSubcommand(usage);
				if( subsubcommand != null ) {
					return subsubcommand;
				}
			}
		}
		return null;
	}
	
	public Command<E> withPassive(boolean passive) {
		this.passive = passive;
		return this;
	}
	
	public boolean isPassive() {
		return passive;
	}
	
	public boolean isDocumented() {
		return !usage.isEmpty() || !description.isEmpty();
	}
	
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
		
		int dependentEffectsCounter = 0;
		int independentEffectsCounter = 0;
		int dependentSideEffectsCounter = 0;
		int independentSideEffectsCounter = 0;
		int suppliersCounter = 0;
				
		for( List<?> effectList : executeOrder ) {
			if( effectList.equals(dependentEffects) ) {
				try {
					int dependentEffectIndex = dependentEffectsCounter++;
					Mono<?> wrapped = Mono.just(0).flatMap((c) -> dependentEffects.get(dependentEffectIndex).apply(dependency.block()));
					result = result.then(applyErrorHandler(wrapped));
				} catch (Exception e) {
					result = Mono.fromRunnable(() -> {AliceLogger.error("Error occured while building dependent effect:"); e.printStackTrace();});
					break;
				}
			}
			if( effectList.equals(independentEffects) ) {
				try {
					int independentEffectIndex = independentEffectsCounter++;
					Mono<?> wrapped = Mono.just(0).flatMap((c) -> independentEffects.get(independentEffectIndex).apply(t));
					result = result.then(applyErrorHandler(wrapped));
				} catch (Exception e) {
					result = Mono.fromRunnable(() -> {AliceLogger.error("Error occured while building independent effect:"); e.printStackTrace();});
					break;
				}
			}
			if( effectList.equals(dependentSideEffects) ) {
				try {
					Consumer<DependencyMap<E>> dependentSideEffect = dependentSideEffects.get(dependentSideEffectsCounter++);
					result = result.then(applyErrorHandler(Mono.fromRunnable(() -> {dependentSideEffect.accept(dependency.block());})));
				} catch (Exception e) {
					result = Mono.fromRunnable(() -> {AliceLogger.error("Error occured while building dependent side-effect:"); e.printStackTrace();});
					break;
				}
			}
			if( effectList.equals(independentSideEffects) ) {
				try {
					Consumer<E> independentSideEffect = independentSideEffects.get(independentSideEffectsCounter++);
					result = result.then(applyErrorHandler(Mono.fromRunnable(() -> {independentSideEffect.accept(t);})));
				} catch (Exception e) {
					result = Mono.fromRunnable(() -> {AliceLogger.error("Error occured while building independent side-effect:"); e.printStackTrace();});
					break;
				}
			}
			if( effectList.equals(suppliers) ) {
				result = result.then(applyErrorHandler(suppliers.get(suppliersCounter++).get()));
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
					result = result.then(subcommand.apply(t));
				}
			}
			
			if( !overidden ) {
				result = result.then(executeEffects(t));
			}
		}
		
		return result;
	}
	
	public Mono<?> applyErrorHandler(Mono<?> mono) {
		return mono.retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(5)).doBeforeRetry(rs -> {
			AliceLogger.error(String.format("Error propagated. Retrying %d of %d...",rs.totalRetriesInARow()+1,5));
		}))
		.doOnError(f -> {
			AliceLogger.error("Fatal error propagated during task execution:");
			f.printStackTrace();
			Brain.gateway.logout().block();
		})
		.onErrorStop();
	}
	
}
