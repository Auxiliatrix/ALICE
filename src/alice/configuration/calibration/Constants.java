package alice.configuration.calibration;

public class Constants {
	public static final String COMMAND_PREFIX = "%";
	public static final String NAME = "AL | CE";
	public static final String FULL_NAME = "Assistive Logistics | Communicative Entity";
	public static final String LINK = "https://github.com/Auxiliatrix/ALICE";
	public static final String[] ALIASES = new String[] {
			"AL|CE",
			"ALICE"
	};
	public static final long[] DEVELOPER_IDS = new long[] {
			Long.parseLong("246562987651891200"),
			Long.parseLong("365715538166415362"),
	};
	
	public static final String INCLUDED_MODULES = "alice.modular.handlers";
	public static final String EXCLUDED_MODULES = "alice.framework.handlers";
	public static final String[] ADDITIONAL_MODULES = new String[] {
			
	};
	
	public static long REPUTATION_INTERVAL = 4 * 60 * 60 * 1000;
}
