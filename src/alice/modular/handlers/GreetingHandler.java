package alice.modular.handlers;

import alice.framework.features.Documentable;
import alice.framework.handlers.MentionHandler;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class GreetingHandler extends MentionHandler implements Documentable {

	public static final String[] GREETINGS_IN = new String[] {
			"Hello",
			"Hi ",
			"Hi,",
			"Howdy",
			"G'day",
			"Gday",
			"Good morn",
			"Good even",
			"Good aftern",
			"Good day",
			"Morning",
			"Evening",
			"Afternoon",
			"Hey",
			"Yo ",
			"Yo,",
			"Sup ",
			"Sup,",
			"'Sup",
			"Salutations",
			"Greetings",
			"Hiya",
			"Hi-ya",
			"What's up",
			"Whats up"
	};
	
	public static final String[] GREETINGS_OUT = new String[] {
			"Hello,",
			"Hi,",
			"Hey,",
			"Salutations,",
			"Greetings,",
			"Hiya,",
	};
	
	public GreetingHandler() {
		super("Greet", false, PermissionProfile.getAnyonePreset());
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		return ts.containsAnyIgnoreCase(GREETINGS_IN);
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		String chosenGreeting = GREETINGS_OUT[(int) (Math.random()*GREETINGS_OUT.length)];
		String reference = event.getMessage().getAuthorAsMember().block() != null ? event.getMessage().getAuthorAsMember().block().getDisplayName() : event.getMessage().getAuthor().get().getUsername();
		new MessageCreateAction(event.getMessage().getChannel(), String.format("%s %s!", chosenGreeting, reference)).toMono().block();
	}

	@Override
	public String getCategory() {
		return Documentable.DEFAULT.name();
	}

	@Override
	public String getDescription() {
		return "Allows this bot to greet you back!\n"
				+ "This is a smart module, and will still work even if your message doesn't look exactly like it does in the help documentation.\n"
				+ "Make sure to put this bot's name somewhere in the message, so she knows you're talking to her!";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair("Hey, Alice!", "Causes AL | CE to greet you")
		};
	}

}
