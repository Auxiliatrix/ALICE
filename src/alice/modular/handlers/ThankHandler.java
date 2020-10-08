package alice.modular.handlers;

import alice.configuration.calibration.Constants;
import alice.framework.actions.Action;
import alice.framework.handlers.Handler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel.Type;

public class ThankHandler extends Handler<MessageCreateEvent> {

	public ThankHandler() {
		super("Thank", false, MessageCreateEvent.class);
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		if( event.getMessage().getChannel().block().getType() != Type.GUILD_TEXT || event.getMessage().getAuthor().isEmpty()) {
			return false;
		}
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getGuild().block().getId().asString());

		String lastRepKey = String.format("%d_lastrep", event.getMessage().getAuthorAsMember().block().getId().asLong());
		if( !guildData.has(lastRepKey) ) {
			guildData.put(lastRepKey, System.currentTimeMillis() - (Constants.REPUTATION_INTERVAL+1000));
		}
		long lastRep = guildData.getLong(lastRepKey);
		long remaining = Constants.REPUTATION_INTERVAL - (System.currentTimeMillis() - lastRep);
		
		return new TokenizedString(event.getMessage().getContent()).containsAnyTokensIgnoreCase("thanks", "ty", "tyty") && !PermissionProfile.isBot(event.getMessage().getAuthor(), event.getGuild()) && remaining <= 0;
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		return new MessageCreateAction(event.getMessage().getChannel(), "You can thank them by typing `%rep @user`! You can do so every four hours, and will give both of you a ticket for an end-of-quarter raffle.");
	}

}
