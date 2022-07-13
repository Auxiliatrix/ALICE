package alice.modular.modules;

import alice.framework.database.SyncedJSONArray;
import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyManager;
import alice.framework.main.Brain;
import alice.framework.modules.Module;
import alice.framework.utilities.FileIO;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.InviteCreateEvent;

public class InviteCreateModule extends Module<InviteCreateEvent> {

	public InviteCreateModule() {
		super(InviteCreateEvent.class);
	}

	@Override
	public Command<InviteCreateEvent> buildCommand(DependencyFactory.Builder<InviteCreateEvent> dfb) {
		SyncedJSONObject sfi = SyncedSaveFile.of("lab/invite_user.csv");
		
		DependencyManager<InviteCreateEvent,String> cef = dfb.addWrappedDependency(ice -> ice.getCode());
		
		DependencyFactory<InviteCreateEvent> df = dfb.build();
		Command<InviteCreateEvent> command = new Command<InviteCreateEvent>(df);
		command.withCondition(
			ice -> sfi.has("guildID")
				&& sfi.has("roleID")
				&& sfi.has("invite_map")
				&& sfi.has("self_invites")
				&& (Snowflake.of("367437754034028545").equals(ice.getInviter().get().getId()) || Brain.gateway.getSelfId().equals(ice.getInviter().get().getId()))
		);
		command.withDependentSideEffect(cef.buildSideEffect(
			code -> {
				SyncedJSONArray inviteList = sfi.getJSONArray("self_invites");
				inviteList.put(code);
				FileIO.appendToFile("tmp/codes.csv", String.format("%s\n", code));
				System.out.println(code);
			}
		));
		
		return command;
	}
	
}
