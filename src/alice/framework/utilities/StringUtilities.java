package alice.framework.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtilities {

	public static final String QUOTED_PATTERN = "[\"“”]([^\"“”]+)[\"“”]";
	
	public static List<String> getAllTokens(String string) {
		List<String> tokens = new ArrayList<String>();
		
		List<String> quotedTokens = getQuotedTokens(string);
		String[] segments = string.split(QUOTED_PATTERN);
		for( String segment : segments ) {
			segment = segment.strip();
			segment = segment.replaceAll("  ", " ");
			for( String token : segment.split(" ") ) {
				tokens.add(token);
			}
			if( !quotedTokens.isEmpty() ) {
				tokens.add(quotedTokens.remove(0) );
			}
		}
		
		return tokens;
	}

	public static List<String> getQuotedTokens(String string) {
		Pattern pattern = Pattern.compile(QUOTED_PATTERN);
		Matcher matcher = pattern.matcher(string);
		
		List<String> quotedTokens = new ArrayList<String>();
		while( matcher.find() ) {
			quotedTokens.add(matcher.group(1));
		}
		return quotedTokens;
	}
	
	public static List<String> getUnquotedTokens(String string) {
		List<String> unquotedTokens = new ArrayList<String>();
		
		String[] segments = string.split(QUOTED_PATTERN);
		for( String segment : segments ) {
			segment = segment.strip();
			segment = segment.replaceAll("  ", " ");
			for( String token : segment.split(" ") ) {
				unquotedTokens.add(token);
			}
		}
		
		return unquotedTokens;
	}
	
}
