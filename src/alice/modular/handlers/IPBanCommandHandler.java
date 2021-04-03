package alice.modular.handlers;

import alice.framework.handlers.CommandHandler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.utilities.EventUtilities;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;

public class IPBanCommandHandler extends CommandHandler {
	
	
	public IPBanCommandHandler() {
		super("IPBan", false, PermissionProfile.getDeveloperPreset());
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		User target = event.getMessage().getUserMentions().blockFirst();
		AtomicSaveFile guildData = Brain.guildIndex.get(EventUtilities.getGuildId(event));
		guildData.put(String.format("ipban_%s", target.getUsername()), true);
	}
	
}
