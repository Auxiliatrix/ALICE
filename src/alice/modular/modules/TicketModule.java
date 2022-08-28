package alice.modular.modules;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.main.Brain;
import alice.framework.main.Constants;
import alice.framework.modules.MessageModule;
import alice.framework.utilities.EmbedFactory;
import alice.framework.utilities.SaveFiles;
import alina.structures.SyncedJSONObject;
import alina.structures.TokenizedString;
import alina.structures.TokenizedString.Token;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateFields.Author;
import discord4j.core.spec.EmbedCreateFields.Footer;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public class TicketModule extends MessageModule {
	
	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdm = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent, PermissionSet> psdm = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		DependencyManager<MessageCreateEvent, Guild> gdm = dfb.addDependency(mce -> mce.getGuild());
		DependencyFactory<MessageCreateEvent> df = dfb.build();
				
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getHumanCondition());
		command.withCondition(MessageModule.getInvokedCondition("tkt"));
		command.withDependentEffect(mcdm.buildEffect(
			(mce, mc) -> {
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
				if( !ssf.has("%rep_map") ) {
					ssf.putJSONObject("%rep_map");
				}
				if( !ssf.has("%rep_last") ) {
					ssf.putJSONObject("%rep_last");
				}
				SyncedJSONObject rep_map = ssf.getJSONObject("%rep_map");
				User targetUser = mce.getMessage().getAuthor().get();
				Snowflake s = mce.getMessage().getAuthor().get().getId();
				if( !rep_map.has(s.asString()) ) {
					rep_map.put(s.asString(), 0);
				}
				
				return mc.createMessage(
						EmbedCreateSpec.builder()
							.description(String.format("This user has :tickets:%s tickets!", rep_map.get(s.asString())))
							.color(Color.YELLOW)
							.author(targetUser.getUsername(), null, targetUser.getAvatarUrl())
							.build());
			}
		));
		
		Command<MessageCreateEvent> arg = command.addSubcommand();
		arg.withCondition(MessageModule.getArgumentsCondition(2));
		
		Command<MessageCreateEvent> setup = arg.addSubcommand();
		setup.withCondition(
			mce -> {
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
				return !ssf.has("%rep_map") && !ssf.has("%rep_last");
			}
		);
		setup.withSideEffect(
			mce -> {
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
				if( !ssf.has("%rep_map") ) {
					ssf.putJSONObject("%rep_map");
				}
				if( !ssf.has("%rep_last") ) {
					ssf.putJSONObject("%rep_last");
				}
			}
		);
		
		Command<MessageCreateEvent> leadCommand = arg.addSubcommand();
		leadCommand.withCondition(MessageModule.getArgumentCondition(1, "lead"));
		leadCommand.withDependentEffect(mcdm.with(gdm).buildEffect(
			(mce, mc, g) -> {
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
				SyncedJSONObject rep_map = ssf.getJSONObject("%rep_map");
				List<Mono<Member>> orderedMembers = new ArrayList<Mono<Member>>();
				for( String key : rep_map.keySet() ) {
					orderedMembers.add(g.getMemberById(Snowflake.of(key)));
				}
				
				return Mono.zip(orderedMembers, ms -> {
					List<Member> om = new ArrayList<Member>();
					for( Object m : ms ) {
						om.add((Member) m);
					}
					return om;
				}).flatMap(oms -> {
					PriorityQueue<SimpleEntry<String,Integer>> pq = new PriorityQueue<SimpleEntry<String,Integer>>((se1,se2) -> {return se2.getValue()-se1.getValue();});
					int entryTotal = 0;
					int repTotal = 0;
					for( Member member : oms ) {
						String key = member.getId().asString();
						int score = rep_map.getInt(key);
						pq.add(new SimpleEntry<String,Integer>(member.getUsername(), score));
						entryTotal++;
						repTotal += score;
					}
					
					List<SimpleEntry<String,String>> entries = new ArrayList<SimpleEntry<String,String>>();
					int counter = 0;
					while( !pq.isEmpty() ) {
						if( counter == 12 ) {
							break;
						}
						SimpleEntry<String,Integer> polled = pq.poll();
						entries.add(new SimpleEntry<String,String>(polled.getKey() + (counter<2 ? " :star:" : ""),String.format("Tickets: :tickets:%d", polled.getValue())));
						counter++;
					}
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modListFormat("Ticket Leaderboard", Color.MOON_YELLOW, entries, true, true))
							.withFooter(Footer.of(String.format("Cumulative Tickets: %d | Total Entries: %d", repTotal, entryTotal),null))
							.withAuthor(Author.of(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.gateway.getSelf().block().getAvatarUrl())));
				});
				
			}
		));
		
		Command<MessageCreateEvent> rewardCommand = arg.addSubcommand();
		rewardCommand.withCondition(MessageModule.getArgumentCondition(1, "reward"));
		rewardCommand.withDependentEffect(mcdm.with(psdm).buildEffect(
			(mce, mc, ps) -> {
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
				if( !ssf.has("%rep_reward") ) {
					ssf.putJSONObject("%rep_reward");
				}
				SyncedJSONObject rep_reward = ssf.getJSONObject("%rep_reward");
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				if( ts.size() > 2 && ts.getToken(2).isInteger() ) {
					Token token = ts.getToken(2);
					int reward = token.asInteger();
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat(String.format("Rewards set up successfully for %d tickets!", reward))))
							.and(Mono.fromRunnable(() -> {
								rep_reward.put(mc.getId().asString(), reward);
								ssf.putJSONObject("%" + String.format("rep_reward_%s", mc.getId().asString()));
							}));
				} else {
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Rewards set up successfully for 1 ticket!")))
							.and(Mono.fromRunnable(() -> {
								rep_reward.put(mc.getId().asString(), 1);
								ssf.putJSONObject("%" + String.format("rep_reward_%s", mc.getId().asString()));
							}));
				}
		}));
		
		Command<MessageCreateEvent> repCommand = arg.addSubcommand();
		repCommand.withCondition(MessageModule.getMentionsCondition(1));
		repCommand.withDependentEffect(mcdm.with(psdm).buildEffect(
			(mce, mc, ps) -> {
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
				SyncedJSONObject rep_map = ssf.getJSONObject("%rep_map");
				SyncedJSONObject rep_last = ssf.getJSONObject("%rep_last");
				boolean admin = ps.contains(Permission.ADMINISTRATOR);
				Snowflake s = mce.getMessage().getAuthor().get().getId();
				User targetUser = mce.getMessage().getUserMentions().get(0);
				Snowflake target = mce.getMessage().getUserMentionIds().get(0);
				
				if( !rep_map.has(target.asString()) ) {
					rep_map.put(target.asString(), 0);
				}
				if( !rep_map.has(s.asString()) ) {
					rep_map.put(s.asString(), 0);
				}
				if( s.equals(target) && !admin ) {
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("You cannot give a ticket to yourself!", EmbedFactory.ERR_PERMISSION)));
				} else {
					if( rep_last.has(s.asString()) ) {
						long last = rep_last.getLong(s.asString());
						long current = System.currentTimeMillis();
						long dif = current - last;
						if( dif < 14400000 && !admin ) {
							if( (14400000-dif) > 60000 ) {
								return mc.createMessage(
										EmbedCreateSpec.builder()
											.description(String.format("This user has :tickets:%s tickets!", rep_map.get(target.asString())))
											.color(Color.YELLOW)
											.author(targetUser.getUsername(), null, targetUser.getAvatarUrl())
											.footer(String.format("You can award someone again in %d minute(s)!", (14400000-dif) / 60000), null)
											.build());
							} else {
								return mc.createMessage(
										EmbedCreateSpec.builder()
											.description(String.format("This user has :tickets:%s tickets!", rep_map.get(target.asString())))
											.color(Color.YELLOW)
											.author(targetUser.getUsername(), null, targetUser.getAvatarUrl())
											.footer(String.format("You can award someone again in %d second(s)!", (14400000-dif) / 1000), null)
											.build());
							}
						}
					}
					return Mono.fromRunnable(() -> {
						rep_map.increment(target.asString());
						rep_map.increment(s.asString());
						
						rep_last.put(s.asString(), System.currentTimeMillis());
					}).then(mc.createMessage(EmbedCreateSpec.builder().description(String.format("This user now has :tickets:%s tickets!", rep_map.getInt(target.asString())+1)).color(Color.YELLOW).author(targetUser.getUsername(), null, targetUser.getAvatarUrl()).build()));
				}
			}
		));
		
		Command<MessageCreateEvent> raffleCommand = arg.addSubcommand();
		raffleCommand.withCondition(MessageModule.getArgumentCondition(1, "raffle"));
		raffleCommand.withDependentCondition(MessageModule.getPermissionCondition(psdm, Permission.ADMINISTRATOR));
		raffleCommand.withDependentEffect(mcdm.with(gdm).buildEffect((mce, mc, g) -> {
			SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
			SyncedJSONObject rep_map = ssf.getJSONObject("%rep_map");
			List<String> tickets = new ArrayList<String>();
			for( String key : rep_map.keySet() ) {
				for( int f=0; f<rep_map.getInt(key); f++ ) {
					tickets.add(key);
				}
			}
			int rand = (int) (Math.random()*tickets.size()) + 1;
			String result = "";
			for( int f=0; f<rand; f++ ) {
				result = tickets.remove(0);
			}
			return g.getMemberById(Snowflake.of(result)).flatMap(m -> mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat(String.format("The winner is %s#%s!", m.getUsername(), m.getDiscriminator())))));
		}));
		return command;
	}
	
}
