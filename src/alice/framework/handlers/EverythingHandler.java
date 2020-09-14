package alice.framework.handlers;

import alice.framework.actions.Action;
import alice.framework.actions.VoidAction;
import discord4j.core.event.domain.Event;

public class EverythingHandler extends Handler<Event> {

	public EverythingHandler() {
		super("Report", "Root", false, Event.class);
	}

	@Override
	protected boolean trigger(Event event) {
		return true;
	}

	@Override
	protected Action execute(Event event) {
		return new VoidAction(() -> { System.out.println(event.getClass().getName()); });
	}

}
