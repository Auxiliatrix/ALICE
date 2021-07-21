package alice.framework.utilities;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import alice.framework.main.Brain;
import alice.framework.main.Constants;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

public class EmbedBuilders {
	
	public static final String GENERIC_SUCCESS_MESSAGE = "Operation completed!";
	
	public static final String ERR_GENERAL = "Sorry!";
	public static final String ERR_PERMISSION = "Permission Denied!";
	public static final String ERR_USAGE = "Invalid Usage!";
	
	public static final String GENERIC_ERROR_MESSAGE = "An error occured during this operation.";
	public static final String GENERIC_ERROR_PERMISSION_MESSAGE = "You do not have permission to perform that operation.";
	public static final String GENERIC_ERROR_USAGE_MESSAGE = "Please check out the help documentation for how to use this command.";
	
	public static final int MAX_TITLE_LENGTH = 256;
	public static final int MAX_DESCRIPTION_LENGTH = 4096;
	public static final int MAX_FIELD_COUNT = 25;
	public static final int MAX_FIELD_NAME_LENGTH = 256;
	public static final int MAX_FIELD_VALUE_LENGTH = 1024;
	public static final int MAX_FOOTER_LENGTH = 2048;
	public static final int MAX_AUTHOR_LENGTH = 256;
	
	public static void safeAddTitle(EmbedCreateSpec spec, String title) {
		if( !title.isBlank() ) {
			spec.setTitle(StringUtilities.limitedString(title, MAX_TITLE_LENGTH));
		}
	}
	
	public static void safeAddDescription(EmbedCreateSpec spec, String description) {
		if( !description.isBlank() ) {
			spec.setDescription(StringUtilities.limitedString(description, MAX_DESCRIPTION_LENGTH));
		}
	}
	
	public static void safeAddField(EmbedCreateSpec spec, String fieldName, String fieldValue, boolean inline) {
		if( !fieldName.isBlank() && !fieldValue.isBlank() ) {
			spec.addField(StringUtilities.limitedString(fieldName, MAX_FIELD_NAME_LENGTH), StringUtilities.limitedString(fieldValue, MAX_FIELD_VALUE_LENGTH), inline);
		}
	}

	public static void safeAddFooter(EmbedCreateSpec spec, String footer, String icon) {
		if( !footer.isBlank() ) {
			spec.setFooter(StringUtilities.limitedString(footer, MAX_FOOTER_LENGTH), icon);
		}
	}
	
	public static void safeAddAuthor(EmbedCreateSpec spec, String author, String url, String icon) {
		if( !author.isBlank() ) {
			spec.setAuthor(StringUtilities.limitedString(author, MAX_AUTHOR_LENGTH), url, icon);
		}
	}
	
	public static void applyBotHeader(EmbedCreateSpec spec) {
		safeAddAuthor(spec, String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl());
	}
	
	public static void applyCreditsFormat(EmbedCreateSpec spec) {
		spec.setColor(Color.of(255, 192, 203));
		
		applyBotHeader(spec);
		safeAddTitle(spec, "Developed by Alina Kim");
		safeAddDescription(spec, "Built using the [Java Discord4j Framework](https://github.com/Discord4J/Discord4J)");
		safeAddField(spec, ":desktop: Developer", "[Alina Kim](https://www.github.com/Auxiliatrix)", false);
		safeAddField(spec, ":computer: Contributor", "[Anthony Zanella](https://www.github.com/InfinityPhase)", false);
		safeAddField(spec, ":paintbrush: Artist", "[Emily Lee](https://emlee.carrd.co/)", false);
	}
	
	public static void applySuccessFormat(EmbedCreateSpec spec) {
		applySuccessFormat(spec, GENERIC_SUCCESS_MESSAGE);
	}
	
	public static void applySuccessFormat(EmbedCreateSpec spec, String message) {
		spec.setColor(Color.of(95, 160, 82));
		
		safeAddTitle(spec, ":white_check_mark: Success!");
		safeAddDescription(spec, message.isEmpty() ? GENERIC_SUCCESS_MESSAGE : message);
	}
	
	public static void applyErrorFormat(EmbedCreateSpec spec) {
		applyErrorFormat(spec, GENERIC_ERROR_MESSAGE, ERR_GENERAL);
	}
	
	public static void applyErrorFormat(EmbedCreateSpec spec, String message) {
		applyErrorFormat(spec, message, ERR_GENERAL);
	}
	
	public static void applyErrorFormat(EmbedCreateSpec spec, String message, String type) {
		spec.setColor(Color.of(166, 39, 0));
		
		safeAddTitle(spec, String.format(":warning: %s!", type.isEmpty() ? ERR_GENERAL : type));
		safeAddDescription(spec, message.isEmpty() ? GENERIC_ERROR_MESSAGE : message);
	}
	
	public static void applyListFormat( EmbedCreateSpec spec, String title, Color color, List<SimpleEntry<String, String>> items, boolean numbered, boolean inline ) {
		spec.setColor(color);
		
		safeAddTitle(spec, title);
		for( int f=0; f<Integer.min(MAX_FIELD_COUNT, items.size()); f++ ) {
			SimpleEntry<String, String> item = items.get(f);
			safeAddField(spec, numbered ? String.format("%d. %s", f+1, item.getKey()) : item.getKey(), item.getValue(), inline);
		}
	}
		
//	public static synchronized Consumer<EmbedCreateSpec> getHelpConstructor(User user) {
//		return c -> helpConstructor(c, user);
//	}
//	
//	public static synchronized Consumer<EmbedCreateSpec> getHelpConstructor(User user, Feature<?> feature) {
//		return c -> helpConstructor(c, user, feature);
//	}
//	
//	public static synchronized Consumer<EmbedCreateSpec> getReputationSelfConstructor(User user, int reputation) {
//		return c -> reputationSelfConstructor(c, user, reputation);
//	}
//	
//	public static synchronized Consumer<EmbedCreateSpec> getReputationChangeConstructor(User user, int reputation) {
//		return c -> reputationChangeConstructor(c, user, reputation);
//	}
//	
//	public static synchronized Consumer<EmbedCreateSpec> getLeaderboardConstructor( String category, List<String> entries, List<String> values, int total, int size ) {
//		return c -> leaderboardConstructor(c, category, entries, values, total, size);
//	}
//	
//	public static synchronized Consumer<EmbedCreateSpec> getBlacklistConstructor( User user, List<Integer> rules ) {
//		return c -> blacklistConstructor(c, user, rules);
//	}
//	
//	public static synchronized Consumer<EmbedCreateSpec> getFeaturesConstructor( List<String> enabledList, List<String> disabledList ) {
//		return c -> featuresConstructor(c, enabledList, disabledList);
//	}
//	
//	private static synchronized EmbedCreateSpec featuresConstructor( EmbedCreateSpec spec, List<String> enabledList, List<String> disabledList ) {
//		spec.setColor(Color.of(63, 79, 95));
//		spec.setAuthor(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl());
//		spec.setTitle(String.format(":gear: Features -- %s", enabledList != null && disabledList != null ? "All" : (enabledList != null ? "Enabled" : "Disabled")));
//		
//		int count = 0;
//		int enabledCount = 0;
//		
//		if( enabledList != null ) {
//			for( String enabled : enabledList ) {
//				spec.addField(enabled, ":green_circle: Enabled", true);
//				count++;
//				enabledCount++;
//			}
//		}
//		
//		if( disabledList != null ) {
//			for( String disabled : disabledList ) {
//				spec.addField(disabled, ":red_circle: Disabled", true);
//				count++;
//			}
//		}
//
//		spec.setFooter(String.format("Total active features: %d | Total enabled features: %d", count, enabledCount), null);
//		return spec;
//	}
//	
//	private static synchronized EmbedCreateSpec blacklistConstructor( EmbedCreateSpec spec, User user, List<Integer> rules ) {
//		spec.setColor(Color.of(139, 0, 0));
//		spec.setAuthor(String.format("%s#%s", user.getUsername(), user.getDiscriminator()), null, user.getAvatarUrl());
//		spec.setDescription("Message removed for violating blacklist rules.");
//		StringBuilder violated = new StringBuilder();
//		for( int rule : rules ) {
//			violated.append(String.format("Rule %d, ", rule));
//		}
//		if( rules.size() > 0 ) {
//			String violatedString = String.format("Violated Rules: %s (`%bl rules` for more info)", violated.toString().substring(0, violated.length()-2));
//			spec.setFooter(violatedString, null);
//		}
//		return spec;
//	}
//	
//	private static synchronized EmbedCreateSpec leaderboardConstructor( EmbedCreateSpec spec, String category, List<String> entries, List<String> values, int total, int size ) {
//		spec.setColor(Color.of(212, 175, 55));
//		spec.setAuthor(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl());
//		spec.setTitle(String.format("%s Leaderboard", category));
//		
//		for( int f=0; f<Math.min(entries.size(), values.size()); f++ ) {
//			spec.addField(entries.get(f), values.get(f), true);
//		}
//		
//		spec.setFooter(String.format("Total reputation awarded: %d | Total participants: %d", (int) (total / 2), size), null);
//		return spec;
//	}
//	
//	private static synchronized EmbedCreateSpec reputationChangeConstructor( EmbedCreateSpec spec, User user, int reputation ) {
//		spec.setColor(Color.of(212, 175, 55));
//		spec.setAuthor(String.format("%s#%s", user.getUsername(), user.getDiscriminator()), null, user.getAvatarUrl());
//		spec.setDescription(String.format("This user now has :scroll: %d reputation!", reputation));
//		return spec;
//	}
//	
//	private static synchronized EmbedCreateSpec reputationSelfConstructor( EmbedCreateSpec spec, User user, int reputation ) {
//		spec.setColor(Color.of(212, 175, 55));
//		spec.setAuthor(String.format("%s#%s", user.getUsername(), user.getDiscriminator()), null, user.getAvatarUrl());
//		spec.setDescription(String.format("This user has :scroll: %d reputation.", reputation));
//		return spec;
//	}
//	
//	@SuppressWarnings("rawtypes")
//	private static synchronized EmbedCreateSpec helpConstructor( EmbedCreateSpec spec, User user ) {
//		spec.setColor(Color.of(63, 79, 95));
//		spec.setAuthor(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl());
//		spec.setTitle(":grey_question: Help -- Categories");
//		Map<String, List<String>> categories = new HashMap<String, List<String>>();
//		for( PriorityQueue<Feature> f : Brain.features.get().values() ) {
//			for( Feature ff : f ) {
//				if( ff instanceof Documentable ) {
//					String category = ((Documentable) ff).getCategory();
//					if( !categories.containsKey(category) ) {
//						categories.put(category, new ArrayList<String>());
//					}
//					categories.get(category).add(ff.getName());
//				}
//			}
//		}
//		for( String key : categories.keySet() ) {
//			if( key.equals(Documentable.HelpCategory.DEVELOPER.name()) && !PermissionProfile.isDeveloper(user) ) {
//				continue;
//			}
//			List<String> names = categories.get(key);
//			StringBuilder nameString = new StringBuilder();
//			for( String name : names ) {
//				nameString.append(String.format(":small_blue_diamond: %s\n", name));
//			}
//			spec.addField(key, nameString.length() == 0 ? "No Features Exist!" : nameString.toString(), false);
//		}
//		return spec;
//	}
//	
//	private static synchronized EmbedCreateSpec helpConstructor( EmbedCreateSpec spec, User user, Feature<?> feature ) {
//		Documentable d = (Documentable) feature;
//		
//		if( d.getCategory().equals(Documentable.HelpCategory.DEVELOPER.name()) && !PermissionProfile.isDeveloper(user) ) {
//			return errorConstructor(spec, "You must be a developer to view this feature!", ERR_PERMISSION);
//		}
//		
//		spec.setColor(Color.of(63, 79, 95));
//		spec.setAuthor(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl());
//		spec.setTitle(String.format(":grey_question: %s -- %s Feature", d.getCategory(), feature.getName()));
//		spec.setDescription(d.getDescription());
//		
//		for( DocumentationPair dp : d.getUsage() ) {
//			spec.addField(dp.getUsage(), dp.getOutcome(), false);
//		}
//		
//		if( d.getExamples().length > 0 ) {
//			StringBuilder sb = new StringBuilder();
//			for( DocumentationPair dp : d.getExamples() ) {
//				sb.append(String.format("__%s__\n", dp.getUsage()));
//				sb.append(String.format(":arrow_right: %s\n", dp.getOutcome()));
//			}
//			spec.addField("Usage", sb.toString(), false);
//		}
//		
//		if( feature.getAliases().size() > 1 ) {
//			String aliases = String.join(", ", feature.getAliases());
//			spec.setFooter(String.format("Aliases: %s", aliases), null);
//		}
//		return spec;
//	}
	
}
