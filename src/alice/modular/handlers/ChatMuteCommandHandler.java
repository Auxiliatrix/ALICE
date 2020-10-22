package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;

public class ChatMuteCommandHandler extends CommandHandler {
	
	public ChatMuteCommandHandler() {
		super("ChatMute", false, PermissionProfile.getAdminPreset());
		aliases.add("cm");
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return new TokenizedString(event.getMessage().getContent()).size() > 1 && !event.getMessage().getUserMentions().collectList().block().isEmpty();
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		Action response = new NullAction();
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getGuild().block().getId().asString());
		for( User user : event.getMessage().getUserMentions().collectList().block() ) {
			String key = String.format("chat_muted_%s", user.getId().asString());
			if( !guildData.has(key) ) {
				guildData.put(key, true);
			} else {
				guildData.remove(key);
			}
		}
		response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Chatmute enforced successfully.")));
		return response;
	}
	
	

}
