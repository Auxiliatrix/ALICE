package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.MentionHandler;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class GreetingHandler extends MentionHandler {

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
		super("Greet", "Default", false);
		this.restrictions = PermissionProfile.getAnyonePreset();
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		return ts.containsAnyIgnoreCase(GREETINGS_IN);
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		String chosenGreeting = GREETINGS_OUT[(int) (Math.random()*GREETINGS_OUT.length)];
		return new NullAction()
				.addCreateMessageAction(event.getMessage().getChannel(), String.format("%s %s!", chosenGreeting, event.getMessage().getAuthorAsMember().block().getDisplayName()));
	}

}
