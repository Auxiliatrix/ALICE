package alice.framework.structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A String wrapper that automatically parses the given String in a variety of ways for easy access.
 * @author Auxiliatrix
 *
 */
public class TokenizedString {
	
	public static final String DELIMIT_WHITESPACE = "\\s+";
	public static final String DELIMIT_WHITESPACE_PUNCTUATED = "[\\.\\?\\!\\,\\-\\;]*\\s+";
	
	/**
	 * A String and the way it was formatted
	 *
	 */
	public static class Token {
		
		protected String content;
		protected int index;
		
		protected boolean quoted;
		protected boolean coded;
		protected boolean mentioned;
		
		public Token(String content, int index) {
			this.content = content;
			this.index = index;
			this.quoted = false;
			this.coded = false;
			this.mentioned = false;
		}
		
		public Token withQuoted() {
			quoted = true;
			return this;
		}
		
		public Token withCoded() {
			coded = true;
			return this;
		}
		
		public Token withMentioned() {
			mentioned = true;
			return this;
		}
		
		public boolean isQuoted() {
			return this.quoted;
		}
		
		public boolean isCoded() {
			return this.coded;
		}
		
		public boolean isMentioned() {
			return this.mentioned;
		}
		
		public boolean isPlain() {
			return !this.quoted && !this.isCoded() && !this.isMentioned();
		}
		
		public boolean isInteger() {
			try {
				@SuppressWarnings("unused")
				int number = Integer.parseInt(content);
				return true;
			} catch( NumberFormatException e ) {}
			return false;
		}
		
		/**
		 * 
		 * @return the token as an integer
		 * @throws NumberFormatException - if the string does not contain a parsable integer.
		 */
		public int asInteger() {
			return Integer.parseInt(content);
		}
		
		public String getContent() {
			return this.content;
		}
		
		public int getIndex() {
			return index;
		}
		
		@Override
		public String toString() {
			return content;
		}
	}
	
	/**
	 * The original unalterted content of the message this object was constructed from.
	 */
	private String originalString;
	
	private Token[] tokens;
	
	public TokenizedString(String string) {
		this.originalString = string;
		tokens = parse(string);
	}
	
	public int getIndex(String token) {
		for( int f=0; f<tokens.length; f++ ) {
			if( tokens[f].toString().equals(token) ) {
				return f;
			}
		}
		return -1;
	}
	
	public int getIndexIgnoreCase(String token) {
		for( int f=0; f<tokens.length; f++ ) {
			if( tokens[f].toString().equalsIgnoreCase(token) ) {
				return f;
			}
		}
		return -1;
	}
	
	public Token getToken(int index) {
		return tokens[index];
	}
	
	public String getString(int index) {
		return tokens[index].getContent();
	}
	
	public List<String> getStrings() {
		List<String> strings = new ArrayList<String>();
		for( Token token : tokens ) {
			strings.add(token.getContent());
		}
		return strings;
	}
	
	public List<Token> getTokens() {
		return Arrays.asList(tokens);
	}
	
	public TokenizedString getSubTokens(int index) {
		return new TokenizedString(originalString.substring(tokens[index].getIndex()));
	}
	
	public static Token[] parse(String string) {
		return parse(string, DELIMIT_WHITESPACE_PUNCTUATED);
	}
	
	public static Token[] parse(String string, String delim) {
		/*
		 * Parsing rules:
		 * - A token is a quantity of text meant to be interpreted separately from the rest of the text.
		 * - A token is typically delimited by spaces, but exceptions can be made.
		 * - A token will ignore delimiters if it is contained within quotation marks of any type.
		 * - Quotation marks will be ignored while within code blocks, and vice versa.
		 * - For the sake of mobile support, “, ”, '', and " will be considered to be the same symbol. 
		 */
		string = string.replace("“", "\"");
		string = string.replace("”", "\"");
		string = string.replace("''", "\"");
		string = string.replaceAll(delim, " ");
		List<Token> tokens = new ArrayList<Token>();
		int globalIndex = 0; // how to track when skipping over delimiter
		
		String[] segments = string.split(DELIMIT_WHITESPACE);

		StringBuilder word = new StringBuilder();
		int wordStartIndex = 0;
		
		boolean inQuote = false;
		boolean inCode = false;
		
		for( int f=0; f<segments.length; f++ ) {
			String segment = segments[f];
			int localIndex = 0;
			while( localIndex < segment.length() ) {
				char c = segment.charAt(localIndex);
				switch(c) {
					case '\"':
						if( inQuote ) {
							inQuote = false;
							Token token = new Token(word.toString(), wordStartIndex);
							if( word.toString().startsWith("<@!") && word.toString().endsWith(">") ) {
								token = token.withMentioned();
							}
							token = token.withQuoted();
							tokens.add(token);
							word = new StringBuilder();
							wordStartIndex = globalIndex+1;
						} else if( !inCode && globalIndex != string.length()-1 && verifyClosed(string.substring(globalIndex+1), '\"')) {
							if( word.length() > 0 ) {
								Token token = new Token(word.toString(), wordStartIndex);
								if( word.toString().startsWith("<@!") && word.toString().endsWith(">") ) {
									token = token.withMentioned();
								}
								tokens.add(token);
							}
							word = new StringBuilder();
							wordStartIndex = globalIndex;
							inQuote = true;
						} else {
							word.append(c);
						}
						break;
					case '`':
						if( inCode ) {
							inCode = false;
							Token token = new Token(word.toString(), wordStartIndex);
							if( word.toString().startsWith("<@!") && word.toString().endsWith(">") ) {
								token = token.withMentioned();
							}
							token = token.withCoded();
							tokens.add(token);
							word = new StringBuilder();
							wordStartIndex = globalIndex+1;
						} else if( !inQuote && globalIndex != string.length()-1 && verifyClosed(string.substring(globalIndex+1), '`')) {
							if( word.length() > 0 ) {
								Token token = new Token(word.toString(), wordStartIndex);
								if( word.toString().startsWith("<@!") && word.toString().endsWith(">") ) {
									token = token.withMentioned();
								}
								tokens.add(token);
							}
							word = new StringBuilder();
							wordStartIndex = globalIndex;
							inCode = true;
						} else {
							word.append(c);
						}
						break;
					default:
						word.append(c);
						break;
				}
				globalIndex++;
				localIndex++;
			}
			if( (!inQuote && !inCode || f == segments.length-1) && word.length() > 0 ) {
				Token token = new Token(word.toString(), wordStartIndex);
				if( word.toString().startsWith("<@!") && word.toString().endsWith(">") ) {
					token = token.withMentioned();
				}
				tokens.add(token);
				word = new StringBuilder();
				wordStartIndex = globalIndex+1;
			} else if( inQuote || inCode ) {
				word.append(' ');
			}
			globalIndex++;
		}
		
		return tokens.toArray(new Token[tokens.size()]);
	}
	
	private static boolean verifyClosed(String string, char symbol) {
		return string.indexOf(symbol) != -1;
	}

	@Override
	public String toString() {
		return originalString;
	}
	
	public int size() {
		return tokens.length;
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
	
	public boolean containsToken(String t) {
		for( Token token : tokens ) {
			if( token.getContent().equals(t) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsTokenIgnoreCase(String t) {
		for( Token token : tokens ) {
			if( token.getContent().equalsIgnoreCase(t) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsAnyTokens(String...ts) {
		for( String t : ts ) {
			if( containsToken(t) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsAnyTokensIgnoreCase(String...ts) {
		for( String t : ts ) {
			if( containsTokenIgnoreCase(t) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsAllTokens(String...ts) {
		for( String t : ts ) {
			if( !containsToken(t) ) {
				return false;
			}
		}
		return true;
	}
	
	public boolean containsAllTokensIgnoreCase(String...ts) {
		for( String t : ts ) {
			if( !containsTokenIgnoreCase(t) ) {
				return false;
			}
		}
		return true;
	}
}
