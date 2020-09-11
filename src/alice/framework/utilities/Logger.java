package alice.framework.utilities;

public class Logger {
	
	public static final String INDENT_FILLER = "..";
	public static final String INDENT_HEADER = "> ";
	
	public static final String ERROR_PREFIX = "[Err]";
	public static final String DEBUG_PREFIX = "[Dbg]";
	public static final String INFO_PREFIX =  "[Inf]";
	
	private static boolean verbose = false;
	private static int threshold = -1;
	
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
