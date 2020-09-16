package alice.modular.actions;

import alice.framework.actions.Action;
import alice.framework.main.Brain;

public class ShutdownAction extends Action {

	public ShutdownAction() {
		super();
		this.mono = Brain.client.logout();
	}

}
