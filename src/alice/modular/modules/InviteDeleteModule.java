package alice.modular.modules;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.InviteDeleteEvent;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

import alice.framework.database.SyncedJSONObject;
import alice.framework.database.SyncedSaveFile;
import alice.framework.main.Brain;
import alice.framework.modules.commands.Command;
import alice.framework.modules.commands.Module;
import alice.framework.modules.tasks.DependencyFactory;
import alice.framework.modules.tasks.EffectFactory;
import alice.framework.utilities.FileIO;

public class InviteDeleteModule extends Module<InviteDeleteEvent> {

	public InviteDeleteModule() {
		super(InviteDeleteEvent.class);
	}

	@Override
	public Command<InviteDeleteEvent> buildCommand(DependencyFactory.Builder<InviteDeleteEvent> dfb) {
		SyncedJSONObject sfi = SyncedSaveFile.of("lab/invite_user.csv");

		EffectFactory<InviteDeleteEvent, List<Member>> lmef = dfb.addDependency(ide -> Brain.getMembers(ide.getGuildId().get()).collectList());
		
		DependencyFactory<InviteDeleteEvent> df = dfb.buildDependencyFactory();
		Command<InviteDeleteEvent> command = new Command<InviteDeleteEvent>(df);
		
		command.withCondition(ice -> {
			String code = ice.getCode();
			boolean result = true;
			
			result &= sfi.getJSONArray("self_invites").toList().contains(code);
			
			return result;
		});
		
		command.withDependentEffect(d -> {
			List<Member> lm = d.<List<Member>>request(lmef);
			String code = d.getEvent().getCode();
			lm.sort(new Comparator<Member>() {
				@Override
				public int compare(Member o1, Member o2) {
					return -o1.getJoinTime().get().compareTo(o2.getJoinTime().get());
			}});
			Member target = lm.get(0);
			return target.addRole(Snowflake.of(sfi.getLong("roleID"))).and(Mono.fromRunnable(() -> {
				SyncedJSONObject inviteMap = sfi.getJSONObject("invite_map");
				inviteMap.put(target.getId().asString(), code);
				FileIO.appendToFile("tmp/user_associations.csv", String.format("%s,%s#%s,%s\n", code, target.getUsername(), target.getDiscriminator(), target.getId().asString()));
				System.out.println(target.getUsername() + "#" + target.getDiscriminator());
			}));
		});
		
		return command;
	}

}
