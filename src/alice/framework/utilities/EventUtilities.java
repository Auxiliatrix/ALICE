package alice.framework.utilities;

import alice.framework.main.Brain;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel.Type;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.VoiceChannel;

public class EventUtilities {
	
	public static synchronized String getGuildId(MessageCreateEvent event) {
		return event.getGuild().block().getId().asString();
	}
	
	public static synchronized boolean fromSelf(MessageCreateEvent event) {
		return event.getMessage().getAuthor().isEmpty() ? false : event.getMessage().getAuthor().get().equals(Brain.client.getSelf().block());
	}
	
	public static synchronized VoiceChannel getConnectedVC(MessageCreateEvent event) {
		VoiceChannel location = null;
		for( GuildChannel gc : event.getGuild().block().getChannels().filter( c -> c.getType() == Type.GUILD_VOICE ).collectList().block() ) {
			VoiceChannel vc = (VoiceChannel) gc;
			if( vc.isMemberConnected(event.getMessage().getAuthorAsMember().block().getId()).block() ) {
				location = vc;
			}
		}
		return location;
	}
	
	public static synchronized boolean getConnected(VoiceStateUpdateEvent event) {
		return event.getCurrent().getChannel().block() != null;
	}
	
	public static synchronized boolean getDisconnected(VoiceStateUpdateEvent event) {
		return !event.getOld().isEmpty();
	}
	
	public static synchronized String escapeMarkdown(String message) {
		message = message.replace("\\", "\\\\");
		message = message.replace("*", "\\*");
		message = message.replace("__", "\\__");
		message = message.replace("~~", "\\~~");
		return message;
	}
	
}
