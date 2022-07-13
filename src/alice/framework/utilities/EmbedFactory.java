package alice.framework.utilities;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.function.Consumer;

import org.checkerframework.checker.nullness.qual.Nullable;

import alice.framework.main.Brain;
import alice.framework.main.Constants;
import discord4j.core.spec.EmbedCreateFields.Field;
import discord4j.core.spec.EmbedCreateFields.Footer;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

public class EmbedFactory {
	
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
	
	public static class FunctionalEmbedBuilder {
		public EmbedCreateSpec.Builder ecsb;
		
		public FunctionalEmbedBuilder() {
			this(EmbedCreateSpec.builder());
		}
		
		public FunctionalEmbedBuilder(EmbedCreateSpec.Builder ecsb) {
			this.ecsb = ecsb;
		}
		
		public FunctionalEmbedBuilder apply(Consumer<EmbedCreateSpec.Builder> modifier) {
			modifier.accept(ecsb);
			return this;
		}
		
		public EmbedCreateSpec build() {
			return ecsb.build();
		}
		
		public static FunctionalEmbedBuilder from(Consumer<EmbedCreateSpec.Builder> modifier) {
			EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();
			modifier.accept(builder);
			return new FunctionalEmbedBuilder(builder);
		}
		
	}
	
	public static FunctionalEmbedBuilder builder() {
		return new FunctionalEmbedBuilder();
	}
	
	public static EmbedCreateSpec.Builder from(Consumer<EmbedCreateSpec.Builder> modifier) {
		EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();
		modifier.accept(builder);
		return builder;
	}
	
	public static EmbedCreateSpec build(Consumer<EmbedCreateSpec.Builder> modifier) {
		EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();
		modifier.accept(builder);
		return builder.build();
	}
	
	public static Consumer<EmbedCreateSpec.Builder> modSafeTitle(String title) {
		return ecsb -> {
					if( !title.isEmpty() ) {
						ecsb.title(StringUtilities.limitedString(title, MAX_TITLE_LENGTH));
					}
				};
	}
	
	public static Consumer<EmbedCreateSpec.Builder> modSafeDescription(String description) {
		return ecsb -> {
			if( !description.isEmpty() ) {
				ecsb.description(StringUtilities.limitedString(description, MAX_DESCRIPTION_LENGTH));
			}
		};
	}
	
	public static Consumer<EmbedCreateSpec.Builder> modSafeAddField(String name, String value, boolean inline) {
		return ecsb -> {
			if( !name.isEmpty() && !value.isEmpty() ) {
				ecsb.addField(Field.of(StringUtilities.limitedString(name, MAX_FIELD_NAME_LENGTH), StringUtilities.limitedString(value, MAX_FIELD_VALUE_LENGTH), inline));
			}
		};
	}
	
	public static Consumer<EmbedCreateSpec.Builder> modSafeFooter(String footer, @Nullable String icon) {
		return ecsb -> {
			if( !footer.isEmpty() ) {
				ecsb.footer(Footer.of(StringUtilities.limitedString(footer, MAX_FOOTER_LENGTH), icon));
			}
		};
	}
	
	public static Consumer<EmbedCreateSpec.Builder> modSafeAuthor(String author, @Nullable String url, @Nullable String icon) {
		return ecsb -> {
			if( !author.isEmpty() ) {
				ecsb.author(author, url, icon);
			}
		};
	}
	
	public static void applyBotHeader(EmbedCreateSpec.Builder builder) {
		modSafeAuthor(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl()).accept(builder);;
	}
	
	public static void applyCreditsFormat(EmbedCreateSpec.Builder builder) {
		builder.color(Color.of(255, 192, 203));
		modSafeAuthor(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl())
			.andThen(modSafeTitle("Developed by Alina Kim"))
			.andThen(modSafeDescription("Built using the [Java Discord4j Framework](https://github.com/Discord4J/Discord4J)"))
			.andThen(modSafeAddField(":desktop: Developer", "[Alina Kim](https://www.github.com/Auxiliatrix)", false))
			.andThen(modSafeAddField(":computer: Contributor", "[Anthony Zanella](https://www.github.com/InfinityPhase)", false))
			.andThen(modSafeAddField(":paintbrush: Artist", "[Emily Lee](https://emlee.carrd.co/)", false))
			.accept(builder);;
	}
	
	public static void applySuccessFormat(EmbedCreateSpec.Builder builder) {
		modSuccessFormat().accept(builder);
	}
	
	public static Consumer<EmbedCreateSpec.Builder> modSuccessFormat() {
		return modSuccessFormat(GENERIC_SUCCESS_MESSAGE);
	}
	
	public static Consumer<EmbedCreateSpec.Builder> modSuccessFormat(String message) {
		return ecsb -> {
			ecsb.color(Color.of(95, 100, 82));
			modSafeTitle(":white_check_mark: Success!")
			.andThen(modSafeDescription(message.isEmpty() ? GENERIC_SUCCESS_MESSAGE : message))
			.accept(ecsb);
		};
	}
	
	public static Consumer<EmbedCreateSpec.Builder> modErrorFormat() {
		return modErrorFormat(GENERIC_ERROR_MESSAGE, ERR_GENERAL);
	}
	
	public static Consumer<EmbedCreateSpec.Builder> modErrorFormat(String message) {
		return modErrorFormat(message, ERR_GENERAL);
	}
	
	public static Consumer<EmbedCreateSpec.Builder> modErrorFormat(String message, String type) {
		return ecsb -> {
			ecsb.color(Color.of(166, 39, 0));
			modSafeTitle(String.format(":warning: %s", type.isEmpty() ? ERR_GENERAL : type))
			.andThen(modSafeDescription(message.isEmpty() ? GENERIC_ERROR_MESSAGE : message))
			.accept(ecsb);
		};
	}
	
	public static Consumer<EmbedCreateSpec.Builder> modListFormat(String title, Color color, List<SimpleEntry<String, String>> items, boolean numbered, boolean inline) {
		return ecsb -> {
			ecsb.color(color);
			modSafeTitle(title).accept(ecsb);
			for( int f=0; f<Integer.min(MAX_FIELD_COUNT, items.size()); f++ ) {
				SimpleEntry<String, String> item = items.get(f);
				modSafeAddField(numbered ? String.format("%d. %s", f+1, item.getKey()) : item.getKey(), item.getValue(), inline).accept(ecsb);;
			}
		};
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
//	private static synchronized EmbedCreateSpec featuresConstructor( EmbedCreateSpec.Builder spec, List<String> enabledList, List<String> disabledList ) {
//		spec.withColor(Color.of(63, 79, 95));
//		spec.withAuthor(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl());
//		spec.withTitle(String.format(":gear: Features -- %s", enabledList != null && disabledList != null ? "All" : (enabledList != null ? "Enabled" : "Disabled")));
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
//		spec.withFooter(String.format("Total active features: %d | Total enabled features: %d", count, enabledCount), null);
//		return spec.build();
//	}
//	
//	private static synchronized EmbedCreateSpec blacklistConstructor( EmbedCreateSpec.Builder spec, User user, List<Integer> rules ) {
//		spec.withColor(Color.of(139, 0, 0));
//		spec.withAuthor(String.format("%s#%s", user.getUsername(), user.getDiscriminator()), null, user.getAvatarUrl());
//		spec.withDescription("Message removed for violating blacklist rules.");
//		StringBuilder violated = new StringBuilder();
//		for( int rule : rules ) {
//			violated.append(String.format("Rule %d, ", rule));
//		}
//		if( rules.size() > 0 ) {
//			String violatedString = String.format("Violated Rules: %s (`%bl rules` for more info)", violated.toString().substring(0, violated.length()-2));
//			spec.withFooter(violatedString, null);
//		}
//		return spec.build();
//	}
//	
//	private static synchronized EmbedCreateSpec leaderboardConstructor( EmbedCreateSpec.Builder spec, String category, List<String> entries, List<String> values, int total, int size ) {
//		spec.withColor(Color.of(212, 175, 55));
//		spec.withAuthor(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl());
//		spec.withTitle(String.format("%s Leaderboard", category));
//		
//		for( int f=0; f<Math.min(entries.size(), values.size()); f++ ) {
//			spec.addField(entries.get(f), values.get(f), true);
//		}
//		
//		spec.withFooter(String.format("Total reputation awarded: %d | Total participants: %d", (int) (total / 2), size), null);
//		return spec.build();
//	}
//	
//	private static synchronized EmbedCreateSpec reputationChangeConstructor( EmbedCreateSpec.Builder spec, User user, int reputation ) {
//		spec.withColor(Color.of(212, 175, 55));
//		spec.withAuthor(String.format("%s#%s", user.getUsername(), user.getDiscriminator()), null, user.getAvatarUrl());
//		spec.withDescription(String.format("This user now has :scroll: %d reputation!", reputation));
//		return spec.build();
//	}
//	
//	private static synchronized EmbedCreateSpec reputationSelfConstructor( EmbedCreateSpec.Builder spec, User user, int reputation ) {
//		spec.withColor(Color.of(212, 175, 55));
//		spec.withAuthor(String.format("%s#%s", user.getUsername(), user.getDiscriminator()), null, user.getAvatarUrl());
//		spec.withDescription(String.format("This user has :scroll: %d reputation.", reputation));
//		return spec.build();
//	}
//	
//	@SuppressWarnings("rawtypes")
//	private static synchronized EmbedCreateSpec helpConstructor( EmbedCreateSpec.Builder spec, User user ) {
//		spec.withColor(Color.of(63, 79, 95));
//		spec.withAuthor(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl());
//		spec.withTitle(":grey_question: Help -- Categories");
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
//		return spec.build();
//	}
//	
//	private static synchronized EmbedCreateSpec helpConstructor( EmbedCreateSpec.Builder spec, User user, Feature<?> feature ) {
//		Documentable d = (Documentable) feature;
//		
//		if( d.getCategory().equals(Documentable.HelpCategory.DEVELOPER.name()) && !PermissionProfile.isDeveloper(user) ) {
//			return errorConstructor(spec, "You must be a developer to view this feature!", ERR_PERMISSION);
//		}
//		
//		spec.withColor(Color.of(63, 79, 95));
//		spec.withAuthor(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl());
//		spec.withTitle(String.format(":grey_question: %s -- %s Feature", d.getCategory(), feature.getName()));
//		spec.withDescription(d.getDescription());
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
//			spec.withFooter(String.format("Aliases: %s", aliases), null);
//		}
//		return spec.build();
//	}
	
}
