package alice.framework.handlers;

public interface Documentable {
	
	public static enum HelpCategory {
		ROOT,
		DEVELOPER,
		ADMIN,
		DEFAULT
	}

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
	
	public static final HelpCategory ROOT = HelpCategory.ROOT;
	public static final HelpCategory DEVELOPER = HelpCategory.DEVELOPER;
	public static final HelpCategory ADMIN = HelpCategory.ADMIN;
	public static final HelpCategory DEFAULT = HelpCategory.DEFAULT;
	
	public String getCategory();
	public String getDescription();
	public DocumentationPair[] getUsage();
	public default DocumentationPair[] getExamples() {
		return new DocumentationPair[] {};
	}
	
}
