package alice.modular.modules;

import alice.framework.modules.commands.Command;
import alice.framework.modules.commands.MessageModule;
import alice.framework.modules.tasks.DependencyFactory;
import alice.framework.modules.tasks.EffectFactory;
import alice.framework.modules.tasks.DependencyFactory.Builder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;

public class RoomSetupModule extends MessageModule {

	public RoomSetupModule() {
		super();
	}

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		EffectFactory<MessageCreateEvent,MessageChannel> mcef = dfb.<MessageChannel>addDependency(mce -> mce.getMessage().getChannel());
		DependencyFactory<MessageCreateEvent> df = dfb.buildDependencyFactory();
		EffectFactory<MessageCreateEvent,PermissionSet> psef = dfb.<PermissionSet>addDependency(mce -> mce.getMember().get().getBasePermissions());		
		EffectFactory<MessageCreateEvent,VoiceChannel> vcef = dfb.<VoiceChannel>addDependency(mce -> mce.getMember().get().getVoiceState().flatMap(vs -> vs.getChannel()));
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getInvokedCondition("%room"));
		command.withCondition(MessageModule.getGuildCondition());
		command.withDependentCondition(MessageModule.getPermissionCondition(psef, Permission.ADMINISTRATOR));
		command.withDependentCondition(vcef.getCondition(vc -> vc != null));
		
		command.withDependentEffect(mcef.getEffect(mc -> {return mc.createMessage("Room module engaged.");}));
		
		return command;
	}
	
}
