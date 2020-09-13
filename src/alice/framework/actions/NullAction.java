package alice.framework.actions;

public class NullAction extends VoidAction {

	public NullAction() {
		super(() -> {});
	}
	
}
