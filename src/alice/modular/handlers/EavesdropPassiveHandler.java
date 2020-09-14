package alice.modular.handlers;

import alice.framework.actions.Action;
import alice.framework.handlers.Handler;
import alice.framework.main.Brain;
import alice.modular.actions.DMEchoAction;
import alice.modular.actions.DMSayAction;
import alice.modular.actions.EchoAction;
import alice.modular.actions.SayAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel.Type;

public class EavesdropPassiveHandler extends Handler<MessageCreateEvent> {

	public EavesdropPassiveHandler() {
		super("Eavesdrop", "Root", false, MessageCreateEvent.class);
	}
	
	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return true;
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		if( event.getMessage().getChannel().block().getType() == Type.DM ) {
			if( event.getMessage().getAuthor().get().equals(Brain.client.getSelf().block()) ) {
				return new DMSayAction(event.getMessage().getContent(), event.getMessage().getChannel());
			} else {
				return new DMEchoAction(event.getMessage().getContent(), event.getMessage().getChannel());
			}
		} else { 
			if( event.getMessage().getAuthor().get().equals(Brain.client.getSelf().block()) ) {
				return new SayAction(event.getMessage().getContent(), event.getMessage().getGuild(), event.getMessage().getChannel());
			} else {
				return new EchoAction(event.getMessage().getContent(), event.getMessage().getAuthor(), event.getMessage().getGuild(), event.getMessage().getChannel());
			}
		}
	}

}
