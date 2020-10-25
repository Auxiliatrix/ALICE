package alice.modular.handlers;

import alice.configuration.calibration.Constants;
import alice.framework.handlers.Documentable;
import alice.framework.handlers.MessageHandler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class ThankHandler extends MessageHandler implements Documentable {

	public ThankHandler() {
		super("Thank", true, PermissionProfile.getAnyonePreset().andNotDM().andFromUser());
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		AtomicSaveFile guildData = Brain.guildIndex.get(event.getGuild().block().getId().asString());

		String lastRepKey = String.format("%d_lastrep", event.getMessage().getAuthorAsMember().block().getId().asLong());
		String lastRemindKey = String.format("%d_lastremind", event.getMessage().getAuthorAsMember().block().getId().asLong());
		if( !guildData.has(lastRepKey) ) {
			guildData.put(lastRepKey, System.currentTimeMillis() - (Constants.REPUTATION_INTERVAL+1000));
		}
		if( !guildData.has(lastRemindKey) ) {
			guildData.put(lastRemindKey, System.currentTimeMillis() - (2*Constants.REPUTATION_INTERVAL+1000));
		}
		long lastRep = guildData.getLong(lastRepKey);
		long lastRemind = guildData.getLong(lastRemindKey);
		long remaining = Constants.REPUTATION_INTERVAL - (System.currentTimeMillis() - lastRep);
		long remindCooldown = Constants.REPUTATION_INTERVAL*2 - (System.currentTimeMillis() - lastRemind);
		
		return new TokenizedString(event.getMessage().getContent()).containsAnyTokensIgnoreCase("thanks", "ty", "tyty") && PermissionProfile.fromUser(event.getMessage().getAuthor()) && remaining <= 0 && remindCooldown <= 0;
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		new MessageCreateAction(event.getMessage().getChannel(), "You can thank them by typing `%rep @user`! You can do so every four hours, and will give both of you a ticket for an end-of-quarter raffle.").toMono().block();
	}

	@Override
	public String getCategory() {
		return "Reputation Plug-In";
	}

	@Override
	public String getDescription() {
		return "Reminds users that they can thank other users by giving them reputation points.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair("Thanks!", "Reminds user that they can thank others with %rep.")	
		};
	}

}
