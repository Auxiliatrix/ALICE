package alice.framework.structures;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A String wrapper that automatically parses the given String in a variety of ways for easy access.
 * @author Auxiliatrix
 *
 */
public class PreparsedString {
	
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
		
		public Token(String content, int index) {
			this.content = content;
			this.index = index;
			this.quoted = false;
			this.coded = false;
		}
		
		public Token withQuoted() {
			quoted = true;
			return this;
		}
		
		public Token withCoded() {
			coded = true;
			return this;
		}
		
		public boolean isQuoted() {
			return this.quoted;
		}
		
		public boolean isCoded() {
			return this.coded;
		}
		
		public boolean isPlain() {
			return !this.quoted && !this.isCoded();
		}
		
		public String getContent() {
			return this.content;
		}
		
		public int getIndex() {
			return index;
		}
	}
	
	/**
	 * The original unalterted content of the message this object was constructed from.
	 */
	private String originalString;
	
	private Token[] tokens;
	
	public PreparsedString(String string) {
		this.originalString = string;
		tokens = parse(string);
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
							token = token.withQuoted();
							tokens.add(token);
							word = new StringBuilder();
							wordStartIndex = globalIndex+1;
						} else if( !inCode && globalIndex != string.length()-1 && verifyClosed(string.substring(globalIndex+1), '\"')) {
							if( word.length() > 0 ) {
								Token token = new Token(word.toString(), wordStartIndex);
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
							token = token.withCoded();
							tokens.add(token);
							word = new StringBuilder();
							wordStartIndex = globalIndex+1;
						} else if( !inQuote && globalIndex != string.length()-1 && verifyClosed(string.substring(globalIndex+1), '`')) {
							if( word.length() > 0 ) {
								Token token = new Token(word.toString(), wordStartIndex);
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
	
}
