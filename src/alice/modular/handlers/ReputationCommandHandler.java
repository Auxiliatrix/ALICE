package alice.modular.handlers;

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
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.framework.utilities.EventUtilities;
import alice.modular.actions.MessageCreateAction;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class ReputationCommandHandler extends CommandHandler implements Documentable {

	public ReputationCommandHandler() {
		super("Reputation", false, PermissionProfile.getNotBotPreset());
		this.aliases.add("Rep");
	}

	@Override
	protected boolean trigger(MessageCreateEvent event) {
		return true;
	}

	@Override
	protected Action execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		AtomicSaveFile guildData = Brain.guildIndex.get(EventUtilities.getGuildId(event));
		Optional<User> user = event.getMessage().getAuthor();
		String ownId = event.getMessage().getAuthorAsMember().block().getId().asString();
		
		Mono<MessageChannel> channel = event.getMessage().getChannel();
		
		if( !guildData.has("reputation_map") ) {
			guildData.put("reputation_map", new HashMap<Long, Integer>());
		}
		JSONObject reputationMap = guildData.getJSONObject("reputation_map");
		
		if( ts.size() == 1 ) {
			if( !reputationMap.has(ownId) ) {
				reputationMap = guildData.modifyJSONObject("reputation_map", jo -> jo.put(ownId, 0));
				// reputationMap.put(ownId, 0);
			}
			int reputation = reputationMap.getInt(ownId);
			response.addAction(new MessageCreateAction(channel, EmbedBuilders.getReputationSelfConstructor(user.get(), reputation)));
		} else if( !event.getMessage().getUserMentions().collectList().block().isEmpty() ) {
			String lastRepKey = String.format("%d_lastrep", event.getMessage().getAuthorAsMember().block().getId().asLong());
			if( !guildData.has(lastRepKey) ) {
				guildData.put(lastRepKey, System.currentTimeMillis() - (Constants.REPUTATION_INTERVAL+1000));
			}
			long lastRep = guildData.getLong(lastRepKey);
			long remaining = Constants.REPUTATION_INTERVAL - (System.currentTimeMillis() - lastRep);
			
			if( remaining > 0 && !PermissionProfile.hasPermission(event.getMessage().getAuthor(), event.getGuild(), Permission.ADMINISTRATOR) ) {
				response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor(String.format("You must wait %d seconds until you may do that again.", remaining/1000), EmbedBuilders.ERR_USAGE)));
			} else {
				User target = event.getMessage().getUserMentions().blockFirst();
				if( target.equals(user.get()) && !PermissionProfile.hasPermission(user, event.getGuild(), Permission.ADMINISTRATOR) ) {
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getErrorConstructor("You can't give reputation to yourself!", EmbedBuilders.ERR_USAGE)));
				} else {
					if( !reputationMap.has(target.getId().asString()) ) {
						reputationMap = guildData.modifyJSONObject("reputation_map", jo -> jo.put(target.getId().asString(), 0));
						// reputationMap.put(target.getId().asString(), 0);
					}
					final int targetReputation = reputationMap.getInt(target.getId().asString()) + 1; // blah blah efficiency its O(1) get off my back
					
					reputationMap = guildData.modifyJSONObject("reputation_map", jo -> jo.put(target.getId().asString(), targetReputation));
					// reputationMap.put(target.getId().asString(), reputation);
					
					if( !reputationMap.has(user.get().getId().asString()) ) {
						reputationMap = guildData.modifyJSONObject("reputation_map", jo -> jo.put(user.get().getId().asString(), 0));
						// reputationMap.put(user.get().getId().asString(), 0);
					}
					final int userReputation = reputationMap.getInt(user.get().getId().asString()) + 1;

					reputationMap = guildData.modifyJSONObject("reputation_map", jo -> jo.put(user.get().getId().asString(), userReputation));
					// reputationMap.put(user.get().getId().asString(), reputation);
					
					guildData.put(lastRepKey, System.currentTimeMillis());
					response.addAction(new MessageCreateAction(channel, EmbedBuilders.getReputationChangeConstructor(target, targetReputation)));
				}
			}
			
		} else if( ts.containsAnyTokensIgnoreCase("lead", "leader", "leaderboard") ) {
			class QuantifiedPair<K> implements Comparable<QuantifiedPair<K>>{
				public K key;
				public int value;
				
				public QuantifiedPair(K key, int value) {
					this.key = key; this.value = value;
				}

				@Override
				public int compareTo(QuantifiedPair<K> arg0) {
					return arg0.value - this.value;
				}
			}
			
			List<QuantifiedPair<String>> entries = new ArrayList<QuantifiedPair<String>>();
			for( String key : new HashSet<String>(reputationMap.keySet()) ) {
				entries.add(new QuantifiedPair<String>(key, reputationMap.getInt(key)));
			}
			entries.sort( (a, b) -> a.compareTo(b) );
			List<String> fieldHeaders = new ArrayList<String>();
			List<String> fieldBodies = new ArrayList<String>();
			for( int f=0; f< Math.min(10, entries.size()); f++ ) {
				QuantifiedPair<String> entry = entries.get(f);
				fieldHeaders.add(String.format("%d. %s%s", f+1, event.getGuild().block().getMemberById(Snowflake.of(entry.key)).block().getDisplayName(), f==0 ? " :star:" : ""));
				fieldBodies.add(String.format("Reputation:\t:scroll: %d", entry.value));
			}
			
			response.addAction(new MessageCreateAction(channel, EmbedBuilders.getLeaderboardConstructor("Reputation", fieldHeaders, fieldBodies)));
		} else {
			response.addAction(new MessageCreateAction(channel, EmbedBuilders.getHelpConstructor(user, this)));
		}
		
		// guildData.put("reputation_map", reputationMap);
		
		return response;
	}

	@Override
	public String getCategory() {
		return DEFAULT.name();
	}

	@Override
	public String getDescription() {
		return "A module that allows users to express their respect for one another by giving them Reputation. This feature can be purely cosmetic, or be used to serve more practical purposes.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair(String.format("%s", invocation), "Displays your own reputation standing."),
			new DocumentationPair(String.format("%s <@user>", invocation), "Gives a point of reputation to the given user, as well as yourself. Has a cooldown of four hours."),
			new DocumentationPair(String.format("%s lead|leader|leaderboard", invocation), "Displays this server's reputation leaderboard."),
			//new DocumentationPair(String.format("%s cooldown|cd <", args))
		};
	}
	
}
