package alice.modular.handlers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import alice.configuration.calibration.Constants;
import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.handlers.Documentable;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.QuantifiedPair;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.framework.utilities.EventUtilities;
import alice.modular.actions.MessageCreateAction;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class ReputationCommandHandler extends CommandHandler implements Documentable {
	
	public ReputationCommandHandler() {
		super("Reputation", true, PermissionProfile.getAnyonePreset().andFromUser().andNotDM());
		this.aliases.add("rep");
	}

	@Override
	protected void execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		AtomicSaveFile guildData = Brain.guildIndex.get(EventUtilities.getGuildId(event));
		Guild guild = event.getGuild().block();
		Optional<User> user = event.getMessage().getAuthor();
		String ownId = event.getMessage().getAuthorAsMember().block().getId().asString();
		
		Mono<MessageChannel> channel = event.getMessage().getChannel();
		
		if( !guildData.has("reputation_map") ) {
			guildData.put("reputation_map", new HashMap<Long, Integer>());
		}
		JSONObject reputationMap = guildData.getJSONObject("reputation_map");
		
		if( ts.size() == 1 ) {
			if( !reputationMap.has(ownId) ) {
				reputationMap = guildData.modifyJSONObject("reputation_map", jo -> jo.put(ownId, 1));
			}
			int reputation = reputationMap.getInt(ownId);
			response.addAction(new MessageCreateAction(channel, EmbedBuilders.getReputationSelfConstructor(user.get(), reputation)));
		} else if( !event.getMessage().getUserMentions().collectList().block().isEmpty()) {
			String lastRepKey = String.format("%d_lastrep", event.getMessage().getAuthorAsMember().block().getId().asLong());
			if( !guildData.has(lastRepKey) ) {
				guildData.put(lastRepKey, System.currentTimeMillis() - (Constants.REPUTATION_INTERVAL+1000));
			}
			long lastRep = guildData.getLong(lastRepKey);
			long remaining = Constants.REPUTATION_INTERVAL - (System.currentTimeMillis() - lastRep);
			
			if( remaining > 0 && !PermissionProfile.hasPermission(event.getMessage().getAuthor(), event.getGuild(), Permission.ADMINISTRATOR) ) {
				response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(String.format("You must wait %s until you may do that again.", Duration.ofMillis(remaining).toString().replace("PT", "").replace("H", " Hours, ").replace("M", " Minutes, ").replace("S", " Seconds")), "Cooldown Error")));
			} else {
				User target = event.getMessage().getUserMentions().blockFirst();
				if( target.equals(user.get()) && !PermissionProfile.hasPermission(user, event.getGuild(), Permission.ADMINISTRATOR) ) {
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor("You can't give reputation to yourself!", EmbedBuilders.ERR_USAGE)));
				} else {
					if( !reputationMap.has(target.getId().asString()) ) {
						reputationMap = guildData.modifyJSONObject("reputation_map", jo -> jo.put(target.getId().asString(), 1));
						// reputationMap.put(target.getId().asString(), 0);
					}
					final int targetReputation = reputationMap.getInt(target.getId().asString()) + 1; // blah blah efficiency its O(1) get off my back
					
					reputationMap = guildData.modifyJSONObject("reputation_map", jo -> jo.put(target.getId().asString(), targetReputation));
					// reputationMap.put(target.getId().asString(), reputation);
					
					if( !reputationMap.has(user.get().getId().asString()) ) {
						reputationMap = guildData.modifyJSONObject("reputation_map", jo -> jo.put(user.get().getId().asString(), 1));
						// reputationMap.put(user.get().getId().asString(), 0);
					}
					
					guildData.put(lastRepKey, System.currentTimeMillis());
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getReputationChangeConstructor(target, targetReputation)));
				}
			}
			
		} else if( ts.containsAnyTokensIgnoreCase("lead", "leader", "leaderboard") ) {
			int total = 0;
			List<QuantifiedPair<String>> entries = new ArrayList<QuantifiedPair<String>>();
			for( String key : new HashSet<String>(reputationMap.keySet()) ) {
				entries.add(new QuantifiedPair<String>(key, reputationMap.getInt(key)));
				total += reputationMap.getInt(key);
			}
			entries.sort( (a, b) -> a.compareTo(b) );
			List<String> fieldHeaders = new ArrayList<String>();
			List<String> fieldBodies = new ArrayList<String>();
			for( int f=0; f< Math.min(12, entries.size()); f++ ) {
				if( entries.isEmpty() ) {
					break;
				}
				QuantifiedPair<String> entry = entries.get(f);
				try {
					fieldHeaders.add(String.format("%d. %s%s", f+1, guild.getMemberById(Snowflake.of(entry.key)).block().getDisplayName(), f==0 ? " :star:" : ""));
					fieldBodies.add(String.format("Reputation:\t:scroll: %d", entry.value));
				} catch( Exception e ) {
					entries.remove(f);
					f--;
					continue;
				}
			}
			response.addAction(new MessageCreateAction(channel, EmbedBuilders.getLeaderboardConstructor("Reputation", fieldHeaders, fieldBodies, total, entries.size())));
		} else if( ts.containsAnyTokensIgnoreCase("draw", "select") && PermissionProfile.hasPermission(user, event.getGuild(), Permission.ADMINISTRATOR)) {
			int total = 0;
			List<QuantifiedPair<String>> entries = new ArrayList<QuantifiedPair<String>>();
			for( String key : new HashSet<String>(reputationMap.keySet()) ) {
				entries.add(new QuantifiedPair<String>(key, reputationMap.getInt(key)));
				total += reputationMap.getInt(key);
			}
			int selection = (int) (Math.random() * total) + 1;
			int passed = 0;
			for( QuantifiedPair<String> entry : entries ) {
				passed += entry.value;
				if( passed >= selection ) {
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getModularConstructor(Color.of(228, 180, 0), ":sparkles: Congratulations!", String.format("%s#%s was selected!", guild.getMemberById(Snowflake.of(entry.key)).block().getUsername(), guild.getMemberById(Snowflake.of(entry.key)).block().getDiscriminator() ))));
					break;
				}
			}
		} else if( ts.containsAnyTokensIgnoreCase("reset") && PermissionProfile.hasPermission(user, event.getGuild(), Permission.ADMINISTRATOR)) {
			guildData.put("reputation_map", new JSONObject());
			response.addAction(new MessageCreateAction(channel, EmbedBuilders.getSuccessConstructor("Reputation map reset successfully!")));
		}
		else {
			response.addAction(new MessageCreateAction(channel, EmbedBuilders.getHelpConstructor(user, this)));
		}
		
		// guildData.put("reputation_map", reputationMap);
		
		response.toMono().block();
	}

	@Override
	public String getCategory() {
		return "Reputation Plug-In";
	}

	@Override
	public String getDescription() {
		return "A module that allows users to express their respect for one another by giving them Reputation. This feature can be purely cosmetic, or be used to serve more practical purposes.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair(String.format("%s", invocation), "Displays your own reputation standing."),
			new DocumentationPair(String.format("%s <@user>", invocation), "Gives a point of reputation to the given user. Has a cooldown of two hours."),
			new DocumentationPair(String.format("%s lead|leader|leaderboard", invocation), "Displays this server's reputation leaderboard."),
			//new DocumentationPair(String.format("%s cooldown|cd <", args))
		};
	}
	
}
