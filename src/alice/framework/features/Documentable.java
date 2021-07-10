package alice.framework.features;

import java.util.List;

import alina.utilities.ExclusiveList;

public interface Documentable {
	
	public static enum HelpCategory {
		DEVELOPER,
		ADMIN,
		DEFAULT
	}

	public static List<String> categories = new ExclusiveList<String>();
	
	public static final class DocumentationPair {
		private String usage;
		private String outcome;
		public DocumentationPair(String usage, String outcome) {
			this.usage = usage;
			this.outcome = String.format("> %s", outcome);
		}
		
		public String getUsage() {
			return usage;
		}
		
		public String getOutcome() {
			return outcome;
		}
	}
	
	public String getCategory();
	public String getDescription();
	public DocumentationPair[] getUsage();
	public default DocumentationPair[] getExamples() {
		return new DocumentationPair[] {};
	}
	
}
