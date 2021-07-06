package alice.framework.features;

import java.util.ArrayList;

import alice.framework.main.Brain;
import discord4j.core.event.domain.Event;

/**
 * Identical to a Feature, but when loaded, is saved to the helpers map rather than the featues map.
 * @author Auxiliatrix
 *
 * @param <E> Event to be triggered on
 */
public abstract class HelperFeature<E extends Event> extends Feature<E> {
	// TODO: Replace with a variable in Feature for exclusivity
	
	protected HelperFeature(String name, Class<E> type) {
		super(name, type);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void load(Class<E> type) {
		if(!Brain.helpers.get().keySet().contains(type)) {
			Brain.helpers.get().put(type, new ArrayList<HelperFeature>());
		}
		Brain.helpers.get().get(type).add(this);
	}
}
