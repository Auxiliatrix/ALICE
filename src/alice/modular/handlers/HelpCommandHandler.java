package alice.modular.handlers;

import java.util.Optional;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.handlers.Documentable;
import alice.framework.handlers.Handler;
import alice.framework.main.Brain;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class HelpCommandHandler extends CommandHandler implements Documentable {

	public HelpCommandHandler() {
		super("Help", false, PermissionProfile.getAnyonePreset());
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return true;
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		
		Mono<MessageChannel> channel = event.getMessage().getChannel();
		Optional<User> user = event.getMessage().getAuthor();
		
		if( ts.size() == 1 ) {
			response.addAction(new MessageCreateAction(channel, EmbedBuilders.getHelpConstructor(user)));
		} else {
			String moduleName = ts.get(1);
			Handler<?> module = Brain.getModuleByName(moduleName);
			if( module == null || !(module instanceof Documentable) ) {
				response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "That module does not exist!")));
			} else {
				response.addAction(new MessageCreateAction(channel, EmbedBuilders.getHelpConstructor(user, module)));
			}
		}
		return response;
	}

	@Override
	public String getCategory() {
		return Documentable.DEFAULT.name();
	}

	@Override
	public String getDescription() {
		return "A generic command that demonstrates how to use this bots' various features.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
				new DocumentationPair(invocation, "Displays a help embed listing out all possible command modules"),
				new DocumentationPair(String.format("%s <module>", invocation), "Displays a help embed demonstrating how to use a specific module")
		};
	}

}
