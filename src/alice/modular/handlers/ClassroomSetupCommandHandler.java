package alice.modular.handlers;

import java.util.Optional;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.handlers.Documentable;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.framework.utilities.EventUtilities;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel.Type;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Mono;

public class ClassroomSetupCommandHandler extends CommandHandler implements Documentable {

	public ClassroomSetupCommandHandler() {
		super("ClassroomSetup", false, PermissionProfile.getAdminPreset());
		this.aliases.add("cs");
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return event.getMessage().getChannel().block().getType() == Type.GUILD_TEXT;
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		Mono<MessageChannel> channel = event.getMessage().getChannel();
		Optional<User> user = event.getMessage().getAuthor();
		
		AtomicSaveFile guildData = Brain.guildIndex.get(EventUtilities.getGuildId(event));
		
		if( ts.size() == 1 ) {
			response.addAction(new MessageCreateAction(channel, EmbedBuilders.getHelpConstructor(user, this)));
		} else {
			switch( ts.get(1).toLowerCase() ) {
				case "setup":
					VoiceChannel location = EventUtilities.getConnectedVC(event);
					if( location == null ) {
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "You must be connected to a voice channel!")));
					} else {
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor(user, String.format("Discord Classrooms Hub now bound to #%s", location.getName()))));
						guildData.put("classroom_hub_channel", location.getId().asString());
					}
					break;
				case "takedown":
					if( guildData.has("classroom_hub_channel") ) {
						guildData.remove("classroom_hub_channel");
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor(user, "Discord Classrooms has been unbound.")));
					} else {
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(user, "Discord Classrooms has not been set up in this server.")));
					}
					break;
				default:
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getHelpConstructor(user, this)));
					break;
			}
		}
		return response;
	}

	@Override
	public String getCategory() {
		return "Discord Classrooms";
	}

	@Override
	public String getDescription() {
		return "A module to help set up Discord Classrooms.\n"
				+ "Discord Classrooms designates a voice channel as a hub-- when someone joins it, "
				+ "a new voice channel will be created for them to be sent to, "
				+ "allowing any number of temporary voice channels to be created and destroyed based on demand.\n"
				+ "The created voice channels will disappear automagically once it becomes empty.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair(String.format("%s setup", invocation), "Sets up the invoker's current voice channel to be the Hub"),
			new DocumentationPair(String.format("%s takedown", invocation), "Unsets the previously assigned voice channel as the Hub")
		};
	}

}
