package alice.framework.structures;

import java.util.ArrayList;
import java.util.List;

import alice.framework.utilities.StringUtilities;

public class TokenizedString {
	
	private String originalString;
	
	private List<String> activeTokens;
	private List<String> allTokens;
	private List<String> unquotedTokens;
	private List<String> quotedTokens;
	
	public TokenizedString(String string) {
		originalString = string;
		
		allTokens = StringUtilities.getAllTokens(string);
		unquotedTokens = StringUtilities.getUnquotedTokens(string);
		quotedTokens = StringUtilities.getQuotedTokens(string);
		
		activeTokens = new ArrayList<String>(allTokens);
	}
	
	public TokenizedString quotedOnly() {
		TokenizedString copy = new TokenizedString(originalString);
		copy.activeTokens = new ArrayList<String>(copy.quotedTokens);
		return copy;
	}
	
	public TokenizedString unquotedOnly() {
		TokenizedString copy = new TokenizedString(originalString);
		copy.activeTokens = new ArrayList<String>(copy.unquotedTokens);
		return copy;
	}
	
	public TokenizedString inclusive() {
		TokenizedString copy = new TokenizedString(originalString);
		copy.activeTokens = new ArrayList<String>(copy.allTokens);
		return copy;
	}
	
	public List<String> getTokens() {
		return new ArrayList<String>(activeTokens);
	}
	
	public List<Integer> getNumbers() {
		List<Integer> numbers = new ArrayList<Integer>();
		for( String token : activeTokens ) {
			try {
				int number = Integer.parseInt(token);
				numbers.add(number);
			} catch( NumberFormatException e ) {}
		}
		return numbers;
	}
	
	public String get(int index) {
		return activeTokens.get(index);
	}
	
	public int size() {
		return activeTokens.size();
	}
	
	public boolean equals(String string) {
		return originalString.equals(string);
	}
	
	public boolean equalsIgnoreCase(String string) {
		return originalString.equalsIgnoreCase(string);
	}
	
	public boolean equalsAny(String...strings) {
		for( String string : strings ) {
			if( equals(string) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean equalsAnyIgnoreCase(String...strings) {
		for( String string : strings ) {
			if( equalsIgnoreCase(string) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean startsWith(String string) {
		return originalString.startsWith(string);
	}
	
	public boolean startsWithIgnoreCase(String string) {
		return originalString.toLowerCase().startsWith(string.toLowerCase());
	}
	
	public boolean startsWithAny(String...strings) {
		for( String string : strings ) {
			if( startsWith(string) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean startsWithAnyIgnoreCase(String...strings) {
		for( String string : strings ) {
			if( startsWithIgnoreCase(string) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean endsWith(String string) {
		return originalString.endsWith(string);
	}
	
	public boolean endsWithIgnoreCase(String string) {
		return originalString.toLowerCase().endsWith(string.toLowerCase());
	}
	
	public boolean endsWithAny(String...strings) {
		for( String string : strings ) {
			if( endsWith(string) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean endsWithAnyIgnoreCase(String...strings) {
		for( String string : strings ) {
			if( endsWithIgnoreCase(string) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean contains(String string) {
		return originalString.contains(string);
	}
	
	public boolean containsIgnoreCase(String string) {
		return originalString.toLowerCase().contains(string.toLowerCase());
	}
	
	public boolean containsAny(String...strings) {
		for( String string : strings ) {
			if( contains(string) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsAnyIgnoreCase(String...strings) {
		for( String string : strings ) {
			if( containsIgnoreCase(string) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsAll(String...strings) {
		for( String string : strings ) {
			if( !contains(string) ) {
				return false;
			}
		}
		return false;
	}
	
	public boolean containsAllIgnoreCase(String...strings) {
		for( String string : strings ) {
			if( !containsIgnoreCase(string) ) {
				return false;
			}
		}
		return false;
	}
	
	public boolean containsToken(String token) {
		return activeTokens.contains(token);
	}
	
	public boolean containsTokenIgnoreCase(String token) {
		for( String genericToken : activeTokens ) {
			if( genericToken.equalsIgnoreCase(token) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsAnyTokens(String...tokens) {
		for( String token : tokens ) {
			if( containsToken(token) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsAnyTokensIgnoreCase(String...tokens) {
		for( String token : tokens ) {
			if( containsTokenIgnoreCase(token) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsAllTokens(String...tokens) {
		for( String token : tokens ) {
			if( !containsToken(token) ) {
				return false;
			}
		}
		return true;
	}
	
	public boolean containsAllTokensIgnoreCase(String...tokens) {
		for( String token : tokens ) {
			if( !containsTokenIgnoreCase(token) ) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		return originalString;
	}
	
}
