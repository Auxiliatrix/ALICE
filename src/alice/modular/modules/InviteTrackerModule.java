package alice.modular.modules;

import java.util.List;

import alice.framework.database.SyncedJSONArray;
import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SaveFiles;
import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyManager;
import alice.framework.main.Brain;
import alice.framework.modules.MessageModule;
import alice.framework.structures.TokenizedString;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;

public class InviteTrackerModule extends MessageModule {

	@Override
	public Command<MessageCreateEvent> buildCommand(DependencyFactory.Builder<MessageCreateEvent> dfb) {
		SyncedJSONObject sfi = SaveFiles.of("lab/invite_user.csv");

		DependencyManager<MessageCreateEvent, Guild> gdm = dfb.addDependency(mce -> mce.getGuild());
		DependencyManager<MessageCreateEvent, TokenizedString> tsdm = dfb.addWrappedDependency(mce -> tokenizeMessage(mce));
		
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(getInvokedCondition("%invact"));
		command.withDependentSideEffect(gdm.with(tsdm).buildSideEffect(
			(g,ts) -> {
				if( ts.size() > 1 ) {
					if( !sfi.has("invite_map") ) {
						sfi.putJSONObject("invite_map");
					}
					if( !sfi.has("guildID") ) {
						sfi.put("guildID", g.getId().asLong());
					}
					if( !sfi.has("roleID") ) {
						sfi.put("roleID", Long.parseLong(ts.getToken(1).getContent()));
					}
					
					if( !sfi.has("self_invites") ) {
						sfi.putJSONArray("self_invites");
					}
					SyncedJSONArray sai = sfi.getJSONArray("self_invites");
					List<String> invites = g.getInvites().filter(ei -> 
						Snowflake.of("367437754034028545").equals(ei.getInviterId().get())
						|| Brain.gateway.getSelfId().equals(ei.getInviterId().get())
						).map(ei -> ei.getCode()).collectList().block();
					System.out.println(invites.size());
					for( String invite : invites ) {
						if( !sai.toList().contains(invite) ) {
							sai.put(invite);
						}
					}
				}
			}
		));
		
		return command;
	}
}
