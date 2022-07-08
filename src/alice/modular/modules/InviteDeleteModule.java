package alice.modular.modules;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.InviteDeleteEvent;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyManager;
import alice.framework.main.Brain;
import alice.framework.modules.Module;
import alice.framework.utilities.FileIO;

public class InviteDeleteModule extends Module<InviteDeleteEvent> {

	public InviteDeleteModule() {
		super(InviteDeleteEvent.class);
	}

	@Override
	public Command<InviteDeleteEvent> buildCommand(DependencyFactory.Builder<InviteDeleteEvent> dfb) {
		SyncedJSONObject sfi = SyncedSaveFile.of("lab/invite_user.csv");

		DependencyManager<InviteDeleteEvent, List<Member>> lmef = dfb.addDependency(ide -> Brain.getMembers(ide.getGuildId().get()).collectList());
		DependencyManager<InviteDeleteEvent, String> cef = dfb.addWrappedDependency(ide -> ide.getCode());
		
		DependencyFactory<InviteDeleteEvent> df = dfb.build();
		Command<InviteDeleteEvent> command = new Command<InviteDeleteEvent>(df);
		command.withCondition(ice -> sfi.getJSONArray("self_invites").toList().contains(ice.getCode()));
		command.withDependentEffect(lmef.with(cef).buildEffect(
			(lm,c) -> {
				lm.sort(new Comparator<Member>() {
					@Override
					public int compare(Member o1, Member o2) {
						return -o1.getJoinTime().get().compareTo(o2.getJoinTime().get());
					}
				});
				Member target = lm.get(0);
				return target.addRole(Snowflake.of(sfi.getLong("roleID"))).and(Mono.fromRunnable(
					() -> {
						SyncedJSONObject inviteMap = sfi.getJSONObject("invite_map");
						inviteMap.put(target.getId().asString(), c);
						FileIO.appendToFile("tmp/user_associations.csv", String.format("%s,%s#%s,%s\n", c, target.getUsername(), target.getDiscriminator(), target.getId().asString()));
						System.out.println(target.getUsername() + "#" + target.getDiscriminator());
					}
				));
			}
		));
		
		return command;
	}

}
