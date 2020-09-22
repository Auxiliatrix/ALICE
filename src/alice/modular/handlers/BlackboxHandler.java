package alice.modular.handlers;

import java.util.Optional;

import alice.configuration.references.Keywords;
import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.Documentable;
import alice.framework.handlers.MentionHandler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.framework.utilities.EventUtilities;
import alice.modular.actions.MessageCreateAction;
import alice.modular.actions.MessageDeleteBulkAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel.Type;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

public class BlackboxHandler extends MentionHandler implements Documentable {

	public BlackboxHandler() {
		super("Blackbox", false, PermissionProfile.getAdminPreset());
		aliases.add("bb");
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		return (ts.containsIgnoreCase("blackbox") || ts.containsAllTokensIgnoreCase("black", "box"))
				&& event.getMessage().getChannel().block().getType() == Type.GUILD_TEXT;
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		Action response = new NullAction();
		
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		AtomicSaveFile guildData = Brain.guildIndex.get(EventUtilities.getGuildId(event));
		Mono<MessageChannel> channel = event.getMessage().getChannel();
		Optional<User> user = event.getMessage().getAuthor();
		if( ts.containsAnyIgnoreCase(Keywords.DESTROY) || ts.containsAnyIgnoreCase(Keywords.END) || ts.containsAnyIgnoreCase(Keywords.DISABLE) ) {
			if( guildData.has(String.format("%s_blackbox_start", channel.block().getId().asString())) ) {
				response.addAction(new MessageDeleteBulkAction(channel.map(c -> (GuildMessageChannel) c), guildData.getString(String.format("%s_blackbox_start", channel.block().getId().asString()))));
				response.addAction(new MessageCreateAction(channel, spec -> blackboxCloseConstructor(spec)));
				guildData.remove(String.format("%s_blackbox_start", channel.block().getId().asString()));
			} else {
				response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "There is no active blackbox in this channel!")));
			}
		} else if( ts.containsAnyIgnoreCase(Keywords.CANCEL) ) {
			if( guildData.has(String.format("%s_blackbox_start", channel.block().getId().asString())) ) {
				guildData.remove(String.format("%s_blackbox_start", channel.block().getId().asString()));
				response.addAction(new MessageCreateAction(channel, spec -> blackboxCancelConstructor(spec)));
			} else {
				response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "There is no active blackbox in this channel!")));
			}
		} else if( ts.containsAnyIgnoreCase(Keywords.CREATE) || ts.containsAnyIgnoreCase(Keywords.START) || ts.containsAnyIgnoreCase(Keywords.ENABLE) ) {
			if( guildData.has(String.format("%s_blackbox_start", channel.block().getId().asString())) ) {
				response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "A blackbox is already open in this channel!")));
			} else {
				guildData.put(String.format("%s_blackbox_start", channel.block().getId().asString()), event.getMessage().getId().asString());
				response.addAction(new MessageCreateAction(channel, spec -> blackboxOpenConstructor(spec)));
			}
		}
		
		return response;
	}
	
	private static synchronized EmbedCreateSpec blackboxOpenConstructor( EmbedCreateSpec spec ) {
		spec.setColor(Color.of(0, 0, 0));
		spec.setTitle(":unlock: Blackbox Opened!");
		spec.setDescription("All messages sent after this one will be deleted once the blackbox is closed.");
		return spec;
	}
	
	private static synchronized EmbedCreateSpec blackboxCloseConstructor( EmbedCreateSpec spec ) {
		spec.setColor(Color.of(0, 0, 0));
		spec.setTitle(":lock: Blackbox Closed!");
		spec.setDescription("All messages sent after the blackbox was opened have been deleted.");
		return spec;
	}
	
	private static synchronized EmbedCreateSpec blackboxCancelConstructor( EmbedCreateSpec spec ) {
		spec.setColor(Color.of(0, 0, 0));
		spec.setTitle(":recycle: Blackbox Cancelled!");
		spec.setDescription("This blackbox is no longer open.");
		return spec;
	}

	@Override
	public String getCategory() {
		return Documentable.ADMIN.name();
	}

	@Override
	public String getDescription() {
		return "A module that creates a \"blackbox\", in which any messages sent will be automatically deleted "
				+ "once the blackbox has been closed. Perfect for when you need a little bit of chaos, but don't "
				+ "want to clean the mess up afterwards. You can also cancel the blackbox at any time.\n"
				+ "This is a smart module, and will still work even if your message doesn't look exactly like it does in the help documentation.\n"
				+ "Make sure to put this bot's name somewhere in the message, so she knows you're talking to her!";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair("Alice, could you open up a blackbox?", "Starts up a blackbox session, starting from this message"),
			new DocumentationPair("Alright, you can end the blackbox now, alice!", "Closes the blackbox session, deleting all sent messages"),
			new DocumentationPair("Actualy, i think i might keep these-- alice, cancel the blackbox for me.", "Cancels the blackbox session")
		};
	}

}
