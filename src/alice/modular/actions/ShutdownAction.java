package alice.modular.actions;

import alice.framework.actions.Action;
import alice.framework.main.Brain;

@Deprecated
public class ShutdownAction extends Action {

	public ShutdownAction() {
		super(Brain.client.logout());
	}

}
