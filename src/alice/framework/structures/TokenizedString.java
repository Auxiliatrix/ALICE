package alice.framework.structures;

import java.util.ArrayList;
import java.util.List;

import alice.framework.utilities.StringUtilities;

/**
 * A String wrapper with built in utility functions for parsing instant messages.
 * @author Auxiliatrix
 *
 */
public class TokenizedString {
	
	/**
	 * The original unalterted content of the message this object was constructed from.
	 */
	private String originalString;
	
	/**
	 * The tokens that will currently be returned from this object's functions.
	 */
	private List<String> activeTokens;
	
	/**
	 * All the tokens available.
	 */
	private List<String> allTokens;
	
	/**
	 * All tokens that are not within quotation marks.
	 */
	private List<String> unquotedTokens;
	
	/**
	 * All tokens contained within quotation marks.
	 */
	private List<String> quotedTokens;
	
	/**
	 * Construct a TokenizedString object from a given String.
	 * @param string String to construct from
	 */
	public TokenizedString(String string) {								// Given the following String: "Hello" world!
		originalString = string;
																		// The following would contain:
		allTokens = StringUtilities.getAllTokens(string);				// "Hello", "world!"
		unquotedTokens = StringUtilities.getUnquotedTokens(string);		// "world!"
		quotedTokens = StringUtilities.getQuotedTokens(string);			// "Hello"
		
		activeTokens = new ArrayList<String>(allTokens);
	}
	
	/**
	 * Create a copy of this object that only uses the tokens contained within quotation marks.
	 * @return A copy of this object
	 */
	public TokenizedString quotedOnly() {
		TokenizedString copy = new TokenizedString(originalString);
		copy.activeTokens = new ArrayList<String>(copy.quotedTokens);
		return copy;
	}
	
	/**
	 * Create a copy of this object that only uses the tokens not contained within quotation marks.
	 * @return A copy of this object
	 */
	public TokenizedString unquotedOnly() {
		TokenizedString copy = new TokenizedString(originalString);
		copy.activeTokens = new ArrayList<String>(copy.unquotedTokens);
		return copy;
	}
	
	/**
	 * Create a copy of this object that uses all of the tokens.
	 * @return A copy of this object
	 */
	public TokenizedString inclusive() {
		TokenizedString copy = new TokenizedString(originalString);
		copy.activeTokens = new ArrayList<String>(copy.allTokens);
		return copy;
	}
	
	/**
	 * Get the active tokens in this object.
	 * @return ArrayList of String tokens
	 */
	public List<String> getTokens() {
		return new ArrayList<String>(activeTokens);
	}
	
	/**
	 * Get the numbered tokens in this object.
	 * @return Arraylist of Integer tokens
	 */
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
	
	/**
	 * Get the token at a certain index.
	 * @param index int to look up
	 * @return String token at that index
	 */
	public String get(int index) {
		return activeTokens.get(index);
	}
	
	/**
	 * Get the number of active tokens.
	 * @return the number of active tokens
	 */
	public int size() {
		return activeTokens.size();
	}
	
	/**
	 * Compare whether the original message String is the same as another.
	 * @param string String to compare to
	 * @return whether or not the Strings are equal
	 */
	public boolean equals(String string) {
		return originalString.equals(string);
	}
	
	/**
	 * Compare whether the original message String is the same as another, ignoring case.
	 * @param string String to compare to
	 * @return whether or not the Strings are equal ignoring case
	 */
	public boolean equalsIgnoreCase(String string) {
		return originalString.equalsIgnoreCase(string);
	}
	
	/**
	 * Compare whether the original message String is the same as any of a collection of Strings.
	 * @param strings String objects to compare to
	 * @return whether or not this String is equal to one of those given
	 */
	public boolean equalsAny(String...strings) {
		for( String string : strings ) {
			if( equals(string) ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Compare whether the original message String is the same as any of a collection of Strings, ignoring case.
	 * @param strings String objects to compare to
	 * @return whether or not this String is equal to one of those given ignoring case
	 */
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
