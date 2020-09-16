package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.handlers.Documentable;
import alice.framework.structures.PermissionProfile;
import alice.modular.actions.MessageCreateAction;
import alice.modular.actions.ShutdownAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class ShutdownCommandHandler extends CommandHandler implements Documentable {
	
	public static final String[] SHUTDOWN_MESSAGES = new String[] {
			"Shutting down.",
			"Good night!",
			"Farewell.",
			"See you soon! *robot shutdown noise*",
			"And i oop--",
			"*windows xp powering off noise*",
			"I'll be back.",
			"I won't be down forever~",
			"Lights out!",
			"Going to sleep.",
			"AAAAaaa a  a   a    a",
			"Saving files... Alright! Shutting down!",
			"Aww. See you in a bit, I guess!"
	};
	
	public ShutdownCommandHandler() {
		super("Shutdown", false, PermissionProfile.getDeveloperPreset());
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return true;
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		String message = SHUTDOWN_MESSAGES[(int) (Math.random() * SHUTDOWN_MESSAGES.length)];
		return new NullAction()
				.addAction(new MessageCreateAction(event.getMessage().getChannel(), message))
				.addAction(new ShutdownAction());
	}

	@Override
	public String getCategory() {
		return Documentable.DEVELOPER.name();
	}

	@Override
	public String getDescription() {
		return "An override command to shut down this bot.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair(invocation, "Shuts down this bot across all servers.")	
		};
	}
	
}
