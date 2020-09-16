package alice.framework.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import alice.configuration.calibration.Constants;
import alice.framework.handlers.Handler;
import alice.framework.main.Brain;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

public class EmbedBuilders {
	
	public static synchronized Consumer<EmbedCreateSpec> getCreditsConstructor() {
		return c -> creditsConstructor(c);
	}
	
	public static synchronized Consumer<EmbedCreateSpec> getHelpConstructor() {
		return c -> helpConstructor(c);
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
		spec.addField("Emily Lee (Artist)", "https://emlee.carrd.co/", false);
		return spec;
	}
	
	private static synchronized EmbedCreateSpec helpConstructor( EmbedCreateSpec spec ) {
		spec.setColor(Color.of(63, 79, 95));
		spec.setAuthor(String.format("[%s] :grey_question: Help -- Categories", Constants.NAME, Constants.FULL_NAME), null, Brain.client.getSelf().block().getAvatarUrl());
		Map<String, List<String>> categories = new HashMap<String, List<String>>();
		for( Handler<?> h : Brain.handlers.get() ) {
			String category = h.getCategory();
			if( !categories.containsKey(category) ) {
				categories.put(category, new ArrayList<String>());
			}
			categories.get(category).add(h.getName());
		}
		for( String key : categories.keySet() ) {
			List<String> names = categories.get(key);
			StringBuilder nameString = new StringBuilder();
			for( String name : names ) {
				nameString.append(String.format(":small_blue_diamond: %s\n", name));
			}
			spec.addField(key, nameString.length() == 0 ? "No Modules Exist!" : nameString.toString(), false);
		}
		return spec;
	}
	
}
