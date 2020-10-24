package alice.framework.handlers;

import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class TellstonesCommandHandler extends CommandHandler {

	public TellstonesCommandHandler() {
		super("Tellstones", false, PermissionProfile.getAnyonePreset().andFromUser());
		this.aliases.add("ts");
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		List<String> tokens = ts.getTokens();

		AtomicSaveFile guildData = Brain.guildIndex.get(event.getGuild().block().getId().asString());

		if( !guildData.has("tellstones_counter") ) {
			guildData.put("tellstones_counter", 0);
		}

		if( !guildData.has("tellstones_boards") ) {
			guildData.put("tellstones_boards", new JSONObject());
		}
		JSONObject tellstonesBoards = guildData.getJSONObject("tellstones_board");

		if( !guildData.has("tellstones_invites") ) {
			guildData.put("tellstones_invites", new JSONObject());
		}
		JSONObject tellstonesInvites = guildData.getJSONObject("tellstones_invites");

		if( !guildData.has("tellstones_games") ) {
			guildData.put("tellstones_games", new JSONObject());
		}
		JSONObject tellstonesGames = guildData.getJSONObject("tellstones_games");

		Optional<User> user = event.getMessage().getAuthor();
		String userId = event.getMessage().getAuthorAsMember().block().getId().asString();
		Mono<MessageChannel> channel = event.getMessage().getChannel();

		if( tokens.size() == 1 ) {
			if( tellstonesGames.has(userId) ) {
				// TODO: get id and use to get game and return game state
			} else {
				response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor("There is no game running!", EmbedBuilders.ERR_USAGE)));
			}
		} else {
			switch( tokens.get(1).toLowerCase() ) {
			case "play":
				if( event.getMessage().getUserMentions().collectList().block().isEmpty() ) {
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor("You must mention someone to play with!", EmbedBuilders.ERR_USAGE)));
				} else {
					User invitee = event.getMessage().getUserMentions().blockFirst();
					if( tellstonesInvites.has(invitee.getId().asString()) && tellstonesInvites.getString(invitee.getId().asString()).equals(event.getMessage().getAuthor().get().getId().asString()) ) {
						// TODO: create board, generate board id, put board for both users
						// TODO: remove invites
						// TODO: send board state
					} else {
						tellstonesInvites.put(event.getMessage().getAuthor().get().getId().asString(), invitee.getId().asString());
						response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor("Invitation sent successfully!")));
					}
				}
				break;
			case "place":
				// TODO: check if in game
				// TODO: check if turn
				break;
			case "swap":
				break;
			case "hide":
				break;
			case "challenge":
				break;
			case "boast":
				break;
			case "believe":
				break;
			case "doubt":
				break;
			case "counter":
				break;
			}
		}

		response.toMono().block();
	}

}
