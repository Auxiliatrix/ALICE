package alice.framework.handlers;

public interface Documentable {
	
	public static enum Category {
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
			this.outcome = outcome;
		}
		
		public String getUsage() {
			return usage;
		}
		
		public String getOutcome() {
			return outcome;
		}
	}
	
	public static final Category ROOT = Category.ROOT;
	public static final Category DEVELOPER = Category.DEVELOPER;
	public static final Category ADMIN = Category.ADMIN;
	public static final Category DEFAULT = Category.DEFAULT;
	
	public String getCategory();
	public String getDescription();
	public DocumentationPair[] getUsage();
	public default DocumentationPair[] getExamples() {
		return new DocumentationPair[] {};
	}
	
}
