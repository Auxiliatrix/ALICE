package alice.framework.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtilities {

	public static final String QUOTED_PATTERN = "[\"“”]([^\"“”]+)[\"“”]"; // Any quotation symbol, followed by one or more non-quotation symbols, and ending with a quotation symbol
	
	// Taken from https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
	public static int levenshteinDistance(String stringA, String stringB) {                          
	    int len0 = stringA.length() + 1;                                                     
	    int len1 = stringB.length() + 1;                                                     
	                                                                                    
	    // the array of distances                                                       
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	                                                                                    
	    // initial cost of skipping prefix in String s0                                 
	    for (int i = 0; i < len0; i++) cost[i] = i;                                     
	                                                                                    
	    // dynamically computing the array of distances                                  
	                                                                                    
	    // transformation cost for each letter in s1                                    
	    for (int j = 1; j < len1; j++) {                                                
	        // initial cost of skipping prefix in String s1                             
	        newcost[0] = j;                                                             
	                                                                                    
	        // transformation cost for each letter in s0                                
	        for(int i = 1; i < len0; i++) {                                             
	            // matching current letters in both strings                             
	            int match = (stringA.charAt(i - 1) == stringB.charAt(j - 1)) ? 0 : 1;             
	                                                                                    
	            // computing cost for each transformation                               
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	                                                                                    
	            // keep minimum cost                                                    
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	                                                                                    
	        // swap cost/newcost arrays                                                 
	        int[] swap = cost; cost = newcost; newcost = swap;                          
	    }                                                                               
	                                                                                    
	    // the distance is the cost for transforming all letters in both strings        
	    return cost[len0 - 1];                                                          
	}
	
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
