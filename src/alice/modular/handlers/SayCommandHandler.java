package alice.modular.handlers;
import alice.configuration.calibration.Constants;
import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.handlers.Documentable;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.modular.actions.MessageCreateAction;
import alice.modular.actions.MessageDeleteAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class SayCommandHandler extends CommandHandler implements Documentable {

	public SayCommandHandler() {
		super("Say", false, PermissionProfile.getDeveloperPreset());
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		Action response = new NullAction();
		
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		if( ts.quotedOnly().size() == 0 ) {
			response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must include something for me to say in quotes!", EmbedBuilders.ERR_USAGE)));
		} else {
			String say = ts.quotedOnly().get(0);
			response.addAction(new MessageDeleteAction(event.getMessage()));
			response.addAction(new MessageCreateAction(event.getMessage().getChannel(), say));
		}
		
		response.toMono().block();
	}

	@Override
	public String getCategory() {
		return DEVELOPER.name();
	}

	@Override
	public String getDescription() {
		return String.format("Forces %s to say something.", Constants.NAME);
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
				new DocumentationPair(String.format("%s \"content\"", invocation), "Forces alice to say the content in quotes")
		};
	}
	
}
