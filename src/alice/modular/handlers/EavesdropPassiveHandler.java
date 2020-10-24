package alice.modular.handlers;

import alice.framework.handlers.MessageHandler;
import alice.framework.main.Brain;
import alice.framework.structures.PermissionProfile;
import alice.modular.actions.DMEchoAction;
import alice.modular.actions.DMSayAction;
import alice.modular.actions.EchoAction;
import alice.modular.actions.SayAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel.Type;

public class EavesdropPassiveHandler extends MessageHandler {

	public EavesdropPassiveHandler() {
		super("Eavesdrop", false, PermissionProfile.getAnyonePreset());
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return true;
	}
	
	@Override
	protected void execute(MessageCreateEvent event) {
		if( !event.getMessage().getAuthor().isEmpty() ) {
			if( event.getMessage().getChannel().block().getType() == Type.DM ) {
				if( event.getMessage().getAuthor().get().equals(Brain.client.getSelf().block()) ) {
					new DMSayAction(event.getMessage().getContent(), event.getMessage().getChannel()).toMono().block();
				} else {
					new DMEchoAction(event.getMessage().getContent(), event.getMessage().getChannel()).toMono().block();
				}
			} else {
				if( event.getMessage().getAuthor().get().equals(Brain.client.getSelf().block()) ) {
					new SayAction(event.getMessage().getContent(), event.getMessage().getGuild(), event.getMessage().getChannel()).toMono().block();
				} else {
					new EchoAction(event.getMessage().getContent(), event.getMessage().getAuthor(), event.getMessage().getGuild(), event.getMessage().getChannel()).toMono().block();
				}
			}
		}
	}

}
