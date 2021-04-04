package alice.framework.features;

import java.util.ArrayList;

import alice.framework.main.Brain;
import discord4j.core.event.domain.Event;

public abstract class HelperFeature<E extends Event> extends Feature<E> {

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
