package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.handlers.Documentable;
import alice.framework.structures.PermissionProfile;
import alice.modular.actions.MessageCreateAction;
import alice.modular.actions.ShutdownAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class RestartCommandHandler extends CommandHandler implements Documentable {
	
	public static final String[] RESTART_MESSAGES = new String[] {
			"Brb!",
			"See you soon! *robot shutdown noise*",
			"*windows xp restarting noise*",
			"*terminator voice* I'll be back.",
			"You know that one scene from adventure time, when that gameboy lays out the batteries behind it, and then takes out its own batteries a--",
			"Let me just drop back into the Darkness real quick!",
			"Restarting.",
			"God I hope I wake up after this"
	};
	
	public RestartCommandHandler() {
		super("Restart", false, PermissionProfile.getDeveloperPreset());
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return true;
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		String message = RESTART_MESSAGES[(int) (Math.random() * RESTART_MESSAGES.length)];
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
