package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.Documentable;
import alice.framework.handlers.MentionHandler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.modular.actions.MessageCreateAction;
import alice.modular.actions.NicknameChangeAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel.Type;

public class NicknameLockHandler extends MentionHandler implements Documentable {
	
	public NicknameLockHandler() {
		super("Nickname", false, PermissionProfile.getAdminPreset());
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		return !event.getMessage().getAuthor().isEmpty() && event.getMessage().getChannel().block().getType() == Type.GUILD_TEXT
				&& ts.containsAnyTokensIgnoreCase("nick", "name", "nickname") && ts.containsAnyIgnoreCase("set", "reset", "lock", "unlock");
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		Action response = new NullAction();
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getGuild().block().getId().asString());
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());

		Member target = event.getMessage().getAuthorAsMember().block();
		if( event.getMessage().getUserMentions().blockFirst() != null ) {
			target = event.getMessage().getUserMentions().blockFirst().asMember(event.getGuild().block().getId()).block();
		}
		
		String key = String.format("nick_locked_%s", target.getId().asString());
		
		if( ts.containsTokenIgnoreCase("unlock") ) {
			if( guildData.has(key) ) {
				guildData.remove(key);
				response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Nickname unlocked successfully.")));
			} else {
				response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("No lock present!", EmbedBuilders.ERR_USAGE)));
			}
		} else if( ts.containsTokenIgnoreCase("lock") ) {
			if( ts.quotedOnly().size() > 0 ) {
				guildData.put(key, ts.quotedOnly().get(0));
				response.addAction(new NicknameChangeAction(target, ts.quotedOnly().get(0)));
			} else {
				guildData.put(key, target.getDisplayName());
			}
			response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Nickname locked successfully.")));
		} else if( ts.containsTokenIgnoreCase("reset") ) {
			if( guildData.has(key) ) {
				response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("This user's nickname is locked!", EmbedBuilders.ERR_USAGE)));
			} else {
				response.addAction(new NicknameChangeAction(target, target.getUsername()));
				response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Nickname reset successfully.")));
			}
		} else if( ts.containsTokenIgnoreCase("set") ) {
			if( ts.quotedOnly().size() > 0 ) {
				response.addAction(new NicknameChangeAction(target, ts.quotedOnly().get(0)));
				response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Nickname set successfully.")));
			} else {
				response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify a nickname in quotes!", EmbedBuilders.ERR_USAGE)));
			}
		}
		
		return response;
	}

	@Override
	public String getCategory() {
		return ADMIN.name();
	}

	@Override
	public String getDescription() {
		return "Allows admins to set, reset, and lock user's nicknames so that they cannot be changed by anyone.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair("Alice, set @user's nickname to \"Something Appropriate\".", "Sets the given user's nickname to the given nickname in quotes."),
			new DocumentationPair("Alice, lock @user's nickname to \"Permanent\".", "Sets the given user's nickname, and doesn't allow it to be changed."),
			new DocumentationPair("Alice, lock @user's nickname.", "Prevents the given user's nickname from being changed."),
			new DocumentationPair("Alice, unlock @user's nickname.", "Allows the given user's nickname to be changed again.")
		};
	}

}
