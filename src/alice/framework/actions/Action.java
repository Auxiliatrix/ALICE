package alice.framework.actions;

import java.time.Duration;

import alice.framework.utilities.AliceLogger;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public abstract class Action {
	
	protected Mono<?> mono;
	
	protected Action(Mono<?> mono) {
		this.mono = Mono.defer(
						() -> mono
								.retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(2))
									.doBeforeRetry(signal -> AliceLogger.report(
											String.format("Error occured while executing %s. Retrying (%d/%d).", 
													this.getClass().getName(), 
													signal.totalRetries()+1, 
													5
												)
										))
									)
								
					);
	}
	
	public final Mono<?> toMono() {
		return mono;
	}
	
	
	public Action addAction(Action action) {
		mono = (mono == null) ? action.toMono() : mono.and(action.toMono());
		return this;
	}
	
	public Action addMono(Mono<?> newMono) {
		mono = (mono == null) ? newMono : mono.and(newMono);
		return this;
	}
	
}
