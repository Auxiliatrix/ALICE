package alice.modular.modules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.modules.Module;
import alice.framework.utilities.SaveFiles;
import alina.structures.SyncedJSONArray;
import alina.structures.SyncedJSONObject;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.ExtendedInvite;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

public class InviteAssocTrackerModule extends Module<MemberJoinEvent> {

	public InviteAssocTrackerModule() {
		super(MemberJoinEvent.class);
	}

	@Override
	public Command<MemberJoinEvent> buildCommand(Builder<MemberJoinEvent> dfb) {
		DependencyManager<MemberJoinEvent, List<ExtendedInvite>> idm = dfb.addDependency(mje -> mje.getGuild().flatMap(g -> g.getInvites().collectList()));
		DependencyFactory<MemberJoinEvent> df = dfb.build();
		
		Command<MemberJoinEvent> command = new Command<MemberJoinEvent>(df);
		command.withCondition(
			mje -> {
				SyncedJSONObject ssf = SaveFiles.ofGuild(mje.getGuildId().asLong());
				return ssf.has("%assoc_lists") && ssf.has("%assoc_roles") && ssf.has("%assoc_counts");
			}
		);
		command.withDependentEffect(idm.buildEffect(
			(mje, invites) -> {
				Member m = mje.getMember();
				SyncedJSONObject ssf = SaveFiles.ofGuild(mje.getGuildId().asLong());
				Map<String, Integer> inviteCounts = new HashMap<String, Integer>();
				for( ExtendedInvite i : invites ) {
					inviteCounts.put(i.getCode(), i.getUses());
				}
				SyncedJSONObject assoc_counts = ssf.getJSONObject("%assoc_counts");
				String code = "";
				for( String key : assoc_counts.keySet() ) {
					if( inviteCounts.containsKey(key) ) {
						if( inviteCounts.get(key) > assoc_counts.getInt(key) ) {
							assoc_counts.put(key, inviteCounts.get(key));
							code = key;
							SyncedJSONObject assoc_lists = ssf.getJSONObject("%assoc_lists");
							SyncedJSONArray list = assoc_lists.getJSONArray(code);
							list.put(m.getId().asString());
							break;
						}
					}
				}
				if( !code.isEmpty() ) {
					SyncedJSONObject assoc_roles = ssf.getJSONObject("%assoc_roles");
					if( assoc_roles.has(code) ) {
						String role = assoc_roles.getString(code);
						return m.addRole(Snowflake.of(role));
					}
				}
				return Mono.fromRunnable(() -> {});
			}
		));
		
		return command;
	}
	
}
