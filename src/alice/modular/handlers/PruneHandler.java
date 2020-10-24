package alice.modular.handlers;

import java.util.List;

import alice.configuration.references.Keywords;
import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.Documentable;
import alice.framework.handlers.MentionHandler;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.modular.actions.MessageCreateAction;
import alice.modular.actions.MessageDeleteFilteredAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildMessageChannel;

public class PruneHandler extends MentionHandler implements Documentable {

	public PruneHandler() {
		super("Prune", false, PermissionProfile.getAdminPreset().andNotDM());
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		return ts.containsAnyTokensIgnoreCase(Keywords.DESTROY) && ts.containsAnyTokensIgnoreCase("message", "messages");
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		List<Integer> numbers = ts.getNumbers();
		List<User> mentions = event.getMessage().getUserMentions().collectList().block();
		List<String> quoted = ts.quotedOnly().getTokens();
		
		if( numbers.isEmpty() ) {
			response.addAction(new MessageDeleteFilteredAction(event.getMessage().getChannel().map(m -> (GuildMessageChannel) m), event.getMessage().getId(), 1, m -> filterMessage(m, mentions, quoted)));
		} else {
			response.addAction(new MessageDeleteFilteredAction(event.getMessage().getChannel().map(m -> (GuildMessageChannel) m), event.getMessage().getId(), numbers.get(0), m -> filterMessage(m, mentions, quoted)));
		}
		response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Messages pruned successfully!")));
		response.toMono().block();
	}
	
	private boolean filterMessage(Message message, List<User> mentions, List<String> quoted) {
		if( !mentions.contains(message.getAuthor().get()) && !mentions.isEmpty()) {
			return false;
		}
		
		boolean quoteMatch = false;
		TokenizedString ts = new TokenizedString(message.getContent());
		for( String quote : quoted ) {
			if( ts.containsIgnoreCase(quote) ) {
				quoteMatch = true;
				break;
			}
		}
		if( !quoteMatch && !quoted.isEmpty() ) {
			return false;
		}
				
		return true;
	}

	@Override
	public String getCategory() {
		return ADMIN.name();
	}

	@Override
	public String getDescription() {
		return "Allows for precision pruning of undesirable messages.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair("Alice, remove the last 7 messages.", "Deletes the last 7 messages sent in the channel."),
			new DocumentationPair("Alice, delete 18 messages with \"cat\" in them.", "Deletes the last 18 messages that contain the phrase \"cat\"."),
			new DocumentationPair("Alice, remove the last 4 messages that @Alice sent.", "Deletes the last 4 messages that were sent by the given user.")
		};
	}
	
}
