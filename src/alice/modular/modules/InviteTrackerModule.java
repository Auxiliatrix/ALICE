package alice.modular.modules;

import java.util.List;

import alice.framework.database.SyncedJSONArray;
import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.main.Brain;
import alice.framework.modules.commands.Command;
import alice.framework.modules.commands.MessageModule;
import alice.framework.modules.tasks.DependencyFactory;
import alice.framework.modules.tasks.EffectFactory;
import alice.framework.structures.TokenizedString;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;

public class InviteTrackerModule extends MessageModule {

	public InviteTrackerModule() {
		super(MessageCreateEvent.class);
	}

	@Override
	public Command<MessageCreateEvent> buildCommand(DependencyFactory.Builder<MessageCreateEvent> dfb) {
		SyncedJSONObject sfi = SyncedSaveFile.of("lab/invite_user.csv");

		EffectFactory<MessageCreateEvent, Guild> gef = dfb.addDependency(mce -> mce.getGuild());
		
		DependencyFactory<MessageCreateEvent> df = dfb.buildDependencyFactory();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		
		command.withCondition(getInvokedCondition("%invact"));
		command.withDependentEffect(d -> {
			String content = d.getEvent().getMessage().getContent();
			Guild guild = d.<Guild>request(gef);
			TokenizedString ts = new TokenizedString(content);
			if( ts.size() > 1 ) {
				if( !sfi.has("invite_map") ) {
					sfi.putJSONObject("invite_map");
				}
				if( !sfi.has("guildID") ) {
					sfi.put("guildID", guild.getId().asLong());
				}
				if( !sfi.has("roleID") ) {
					sfi.put("roleID", Long.parseLong(ts.getToken(1).getContent()));
				}
				
				if( !sfi.has("self_invites") ) {
					sfi.putJSONArray("self_invites");
				}
				SyncedJSONArray sai = sfi.getJSONArray("self_invites");
				List<String> invites = guild.getInvites().filter(ei -> 
					Snowflake.of("367437754034028545").equals(ei.getInviterId().get())
					|| Brain.client.getSelfId().equals(ei.getInviterId().get())
					).map(ei -> ei.getCode()).collectList().block();
				System.out.println(invites.size());
				for( String invite : invites ) {
					if( !sai.toList().contains(invite) ) {
						sai.put(invite);
					}
				}
			}
		});
		
		return command;
	}
}
