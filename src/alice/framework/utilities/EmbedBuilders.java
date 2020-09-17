package alice.framework.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import alice.configuration.calibration.Constants;
import alice.framework.handlers.Documentable;
import alice.framework.handlers.Documentable.DocumentationPair;
import alice.framework.handlers.Handler;
import alice.framework.main.Brain;
import alice.framework.structures.PermissionProfile;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

public class EmbedBuilders {
	
	public static final String ERR_PERMISSION = "Permission Denied";
	
	public static synchronized Consumer<EmbedCreateSpec> getCreditsConstructor() {
		return c -> creditsConstructor(c);
	}
	
	public static synchronized Consumer<EmbedCreateSpec> getHelpConstructor(Optional<User> user) {
		return c -> helpConstructor(c, user);
	}
	
	public static synchronized Consumer<EmbedCreateSpec> getHelpConstructor(Optional<User> user, Handler<?> module) {
		return c -> helpConstructor(c, user, module);
	}
	
	public static synchronized Consumer<EmbedCreateSpec> getErrorConstructor(Optional<User> user, String message) {
		return getErrorConstructor(user, message, "");
	}
	
	public static synchronized Consumer<EmbedCreateSpec> getErrorConstructor(Optional<User> user, String message, String type) {
		return c -> errorConstructor(c, user, message, type);
	}
	
	public static synchronized Consumer<EmbedCreateSpec> getSuccessConstructor(Optional<User> user) {
		return getSuccessConstructor(user, "");
	}
	
	public static synchronized Consumer<EmbedCreateSpec> getSuccessConstructor(Optional<User> user, String message) {
		return c -> successConstructor(c, user, message);
	}
	
	private static synchronized EmbedCreateSpec successConstructor( EmbedCreateSpec spec, Optional<User> user, String message ) {
		spec.setColor(Color.of(95, 160, 82));
		if( !user.isEmpty() ) {
			spec.setAuthor(String.format("%s#%s", user.get().getUsername(), user.get().getDiscriminator()), null, user.get().getAvatarUrl());
		} else {
			spec.setAuthor(Constants.NAME, null, Brain.client.getSelf().block().getAvatarUrl());
		}
		spec.setTitle(":white_check_mark: Success!");
		spec.setDescription(message.isEmpty() ? "Operation completed successfully." : message);
		return spec;
	}
	
	private static synchronized EmbedCreateSpec errorConstructor( EmbedCreateSpec spec, Optional<User> user, String message, String type ) {
		spec.setColor(Color.of(166, 39, 0));
		if( !user.isEmpty() ) {
			spec.setAuthor(String.format("%s#%s", user.get().getUsername(), user.get().getDiscriminator()), null, user.get().getAvatarUrl());
		} else {
			spec.setAuthor(Constants.NAME, null, Brain.client.getSelf().block().getAvatarUrl());
		}
		String errorMessage = String.format(":warning: %s!", type.isEmpty() ? "Error" : type);
		spec.setTitle(errorMessage);
		spec.setDescription(message.isEmpty() ? "Operation failed." : message);
		return spec;
	}
	
	private static synchronized EmbedCreateSpec creditsConstructor( EmbedCreateSpec spec ) {
		spec.setColor(Color.of(255, 192, 203));
		spec.setAuthor(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl());
		spec.setTitle("Developed by Alina Kim");
		spec.setDescription("Built using the [Java Discord4j Framework](https://github.com/Discord4J/Discord4J)");
		spec.addField("Alina Kim (Developer)", "https://www.github.com/Auxiliatrix", false);
		spec.addField("Anthony Zanella (Contributor)", "https://www.github.com/InfinityPhase", false);
		spec.addField("Emily Lee (Artist)", "https://emlee.carrd.co/", false);
		return spec;
	}
	
	private static synchronized EmbedCreateSpec helpConstructor( EmbedCreateSpec spec, Optional<User> user ) {
		spec.setColor(Color.of(63, 79, 95));
		spec.setAuthor(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl());
		spec.setTitle(":grey_question: Help -- Categories");
		Map<String, List<String>> categories = new HashMap<String, List<String>>();
		for( Handler<?> h : Brain.handlers.get() ) {
			if( h instanceof Documentable ) {
				Documentable d = (Documentable) h;
				String category = d.getCategory();
				if( !categories.containsKey(category) ) {
					categories.put(category, new ArrayList<String>());
				}
				categories.get(category).add(h.getName());
			}
		}
		for( String key : categories.keySet() ) {
			if( key.equals(Documentable.DEVELOPER.name()) && !PermissionProfile.isDeveloper(user) ) {
				continue;
			}
			List<String> names = categories.get(key);
			StringBuilder nameString = new StringBuilder();
			for( String name : names ) {
				nameString.append(String.format(":small_blue_diamond: %s\n", name));
			}
			spec.addField(key, nameString.length() == 0 ? "No Modules Exist!" : nameString.toString(), false);
		}
		return spec;
	}
	
	private static synchronized EmbedCreateSpec helpConstructor( EmbedCreateSpec spec, Optional<User> user, Handler<?> module ) {
		Documentable d = (Documentable) module;
		
		if( d.getCategory().equals(Documentable.DEVELOPER.name()) && !PermissionProfile.isDeveloper(user) ) {
			return errorConstructor(spec, user, "You must be a developer to view this module!", ERR_PERMISSION);
		}
		
		spec.setColor(Color.of(63, 79, 95));
		spec.setAuthor(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.client.getSelf().block().getAvatarUrl());
		spec.setTitle(String.format(":grey_question: %s -- %s Module", d.getCategory(), module.getName()));
		spec.setDescription(d.getDescription());
		
		for( DocumentationPair dp : d.getUsage() ) {
			spec.addField(dp.getUsage(), dp.getOutcome(), false);
		}
		
		if( d.getExamples().length > 0 ) {
			StringBuilder sb = new StringBuilder();
			for( DocumentationPair dp : d.getExamples() ) {
				sb.append(String.format("__%s__\n", dp.getUsage()));
				sb.append(String.format(":arrow_right: %s\n", dp.getOutcome()));
			}
			spec.addField("Usage", sb.toString(), false);
		}
		
		if( module.getAliases().size() > 1 ) {
			String aliases = String.join(", ", module.getAliases());
			spec.setFooter(String.format("Aliases: %s", aliases), null);
		}
		return spec;
	}
	
}