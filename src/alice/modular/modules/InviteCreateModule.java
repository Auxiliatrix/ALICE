package alice.modular.modules;

import alice.framework.database.SyncedJSONArray;
import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.main.Brain;
import alice.framework.modules.commands.Command;
import alice.framework.modules.commands.Module;
import alice.framework.modules.tasks.DependencyFactory;
import alice.framework.utilities.FileIO;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.InviteCreateEvent;

public class InviteCreateModule extends Module<InviteCreateEvent> {

	public InviteCreateModule() {
		super(InviteCreateEvent.class);
	}

	@Override
	public Command<InviteCreateEvent> buildCommand() {
		SyncedJSONObject sfi = SyncedSaveFile.of("lab/invite_user.csv");

		DependencyFactory.Builder<InviteCreateEvent> dfb = DependencyFactory.builder();
		
		DependencyFactory<InviteCreateEvent> df = dfb.buildDependencyFactory();
		Command<InviteCreateEvent> command = new Command<InviteCreateEvent>(df);
		
		command.withCondition(ice -> {
			boolean result = true;
			result &= sfi.has("guildID");
			result &= sfi.has("roleID");
			result &= sfi.has("invite_map");
			result &= sfi.has("self_invites");
			result &= Snowflake.of("367437754034028545").equals(ice.getInviter().get().getId()) || Brain.client.getSelfId().equals(ice.getInviter().get().getId());
			return result;
		});
		
		command.withEffect(ice -> {
			String code = ice.getCode();
			SyncedJSONArray inviteList = sfi.getJSONArray("self_invites");
			inviteList.put(code);
			FileIO.appendToFile("tmp/codes.csv", String.format("%s\n", code));
			System.out.println(code);
		});
		
		return command;
	}
	
}