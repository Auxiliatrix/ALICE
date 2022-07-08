package alice.framework.main;

import java.text.SimpleDateFormat;

public class Constants {
	/* Properties */
	public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

	
	public static final String COMMAND_PREFIX = "%";
	public static final String NAME = "AL | CE";
	public static final String FULL_NAME = "Assistive Logistics | Communicative Entity";
	public static final String LINK = "https://github.com/Auxiliatrix/ALICE";
	public static final String PROFILE_URL = "https://i.imgur.com/grVaLEQ.png";
	public static final String[] ALIASES = new String[] {
			"AL|CE",
			"ALICE"
	};
	public static final long[] DEVELOPERS = new long[] {
			Long.parseLong("246562987651891200"),
			Long.parseLong("365715538166415362"),
	};


	/* File System Data */
	public static final String TEMP_DATA_DIRECTORY = "tmp";
	public static final String GUILD_DATA_SUBDIRECTORY = "guilds";
	
	public static final String DEFAULT_GUILD_DATA = "{}";

	public static final String[] FEATURE_WHITELIST = new String[] {
			"alice.modular.features",
	};
	
	public static final String[] FEATURE_BLACKLIST = new String[] {
			"alice.framework.features",
	};
	
	
	public static long REPUTATION_INTERVAL = 4 * 60 * 60 * 1000;
}
