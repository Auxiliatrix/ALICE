package alice.modular.handlers;

import alice.framework.actions.VoidAction;
import alice.framework.features.Documentable;
import alice.framework.handlers.CommandHandler;
import alice.framework.main.Brain;
import alice.framework.structures.PermissionProfile;
import alice.modular.actions.MessageCreateAction;
import alice.modular.actions.ShutdownAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Deprecated
public class ShutdownCommandHandler extends CommandHandler implements Documentable {
	
	public static final String[] SHUTDOWN_MESSAGES = new String[] {
			"Shutting down.",
			"Good night!",
			"Farewell.",
			"And i oop--",
			"*windows xp powering off noise*",
			"I won't be down forever~",
			"Lights out!",
			"Going to sleep.",
			"AAAAaaa a  a   a    a",
			"Saving files... Alright! Shutting down!",
			"Aww. See you later, I guess!"
	};
	
	public ShutdownCommandHandler() {
		super("Shutdown", false, PermissionProfile.getDeveloperPreset());
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		String message = SHUTDOWN_MESSAGES[(int) (Math.random() * SHUTDOWN_MESSAGES.length)];
		new VoidAction(() -> {Brain.ALIVE.set(false);})
				.addAction(new MessageCreateAction(event.getMessage().getChannel(), message))
				.addAction(new ShutdownAction()).toMono().block();
	}

	@Override
	public String getCategory() {
		return Documentable.DEVELOPER.name();
	}

	@Override
	public String getDescription() {
		return "An override command to restart this bot.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair(invocation, "Restarts this bot across all servers.")	
		};
	}
	
}
