package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.handlers.Documentable;
import alice.framework.structures.PermissionProfile;
import alice.framework.utilities.FileIO;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PattisQuoteCommandHandler extends CommandHandler implements Documentable {

	private final String QUOTE_FILE = "pattisquotes.txt";
	private String[] quotes;
	
	public PattisQuoteCommandHandler() {
		super("Pattis", true, PermissionProfile.getAnyonePreset());
		quotes = FileIO.readFromFile(QUOTE_FILE, "").split("\n\n");
		aliases.add("pq");
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		Action response = new NullAction();
		
		String quote = quotes[(int) (Math.random() * quotes.length)];
		response.addAction(new MessageCreateAction(event.getMessage().getChannel(), String.format("> %s", quote)));
		
		response.toMono().block();
	}

	@Override
	public String getCategory() {
		return "UCI";
	}

	@Override
	public String getDescription() {
		return "Selects a random quote from Pattis's website (and by extension, his infinite wisdom) and displays it.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
				new DocumentationPair("%pattis", "Displays a random quote from Pattis's website."),
				new DocumentationPair("%pq", "Displays a random quote from Pattis's website."),
		};
	}

}
