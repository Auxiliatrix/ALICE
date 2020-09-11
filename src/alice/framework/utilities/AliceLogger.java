package alice.framework.utilities;

public class AliceLogger {
	
	public static final String INDENT_FILLER = "..";
	public static final String INDENT_HEADER = "> ";
	
	public static final String ERROR_PREFIX = "[Err]";
	public static final String DEBUG_PREFIX = "[Dbg]";
	public static final String INFO_PREFIX =  "[Inf]";
	public static final String ECHO_PREFIX =  "(Ech)";
	
	private static boolean verbose = false;
	private static boolean echo = false;
	private static int threshold = -1;
	
	public static void say(String message, String guildName, String channelName) {
		String prefix = buildPrefixStringBuilder("AL|CE", guildName, channelName).toString();
		System.out.println(buildLogStringBuilder(message, 0, prefix).toString());
	}
	
	public static void echo(String message, String authorName, String guildName, String channelName) {
		if( echo ) {
			StringBuilder extendedPrefix = new StringBuilder();
			String prefix = buildPrefixStringBuilder(authorName, guildName, channelName).toString();
			extendedPrefix.append(ECHO_PREFIX);
			extendedPrefix.append(" ");
			extendedPrefix.append(prefix);
			System.out.println(buildLogStringBuilder(message, 0, extendedPrefix.toString()).toString());
		}
	}
	
	public static void debug(String message) {
		debug(message, 0);
	}
	
	public static void debug(String message, int level) {
		if( verbose && (threshold == -1 || level < threshold) ) {
			System.out.println(buildLogStringBuilder(message, level, DEBUG_PREFIX).toString());
		}
	}

	public static void error(String message) {
		error(message, 0);
	}
	
	public static void error(String message, int level) {
		if( threshold == -1 || level < threshold ) {
			System.out.println(buildLogStringBuilder(message, level, ERROR_PREFIX).toString());
		}
	}
	
	public static void info(String message) {
		info(message, 0);
	}
	
	public static void info(String message, int level) {
		if( threshold == -1 || level < threshold ) {
			System.out.println(buildLogStringBuilder(message, level, INFO_PREFIX).toString());
		}
	}
	
	private static StringBuilder buildPrefixStringBuilder(String authorName, String guildName, String channelName) {
		StringBuilder prefix = new StringBuilder();
		prefix.append("[");
		prefix.append(authorName);
		prefix.append(" @");
		prefix.append(guildName);
		prefix.append(":");
		prefix.append(channelName);
		prefix.append("]");
		return prefix;
	}
	
	private static StringBuilder buildLogStringBuilder(String message, int level, String prefix) {
		StringBuilder output = new StringBuilder();
		output.append(prefix);
		for( int f=0; f<level; f++ ) {
			output.append(INDENT_FILLER);
		}
		output.append(INDENT_HEADER);
		output.append(message);
		return output;
	}
}
