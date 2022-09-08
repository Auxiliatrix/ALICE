package alice.modular.modules;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.main.Constants;
import alice.framework.modules.MessageModule;
import alice.framework.utilities.EmbedFactory;
import alice.framework.utilities.SaveFiles;
import alina.firebase.FirebaseIntegration;
import alina.structures.SyncedJSONObject;
import alina.structures.TokenizedString;
import alina.structures.TokenizedString.Token;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public class RegisterModule extends MessageModule {
	
	private static Map<String, Map<String, ValueEventListener>> listenerMapLogin = new HashMap<String, Map<String, ValueEventListener>>();
	private static Map<String, Map<String, ValueEventListener>> listenerMapSurvey = new HashMap<String, Map<String, ValueEventListener>>();

	
	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdm = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent, PermissionSet> psdm = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());

		DependencyFactory<MessageCreateEvent> df = dfb.build();
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getHumanCondition());
		command.withCondition(MessageModule.getInvokedCondition("register"));
		command.withCondition(MessageModule.getArgumentsCondition(2));
		command.withDependentSideEffect(mcdm.buildSideEffect(
				(mce,mc) -> {
					SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
					if( !ssf.has("%register_map") ) {
						ssf.putJSONObject("%register_map");
					}
					if( !ssf.has("%register_last") ) {
						ssf.putJSONObject("%register_last");
					}
					if( !ssf.has("%register_survey") ) {
						ssf.putJSONObject("%register_survey");
					}
					if( !ssf.has("%register_reward") ) {
						ssf.put("%register_reward", 0);
					}
					if( !ssf.has("%rep_map") ) {
						ssf.putJSONObject("%rep_map");
					}
					if( !listenerMapLogin.containsKey(mce.getGuildId().get().asString()) ) {
						listenerMapLogin.put(mce.getGuildId().get().asString(), new HashMap<String, ValueEventListener>());
					}
					
					if( !listenerMapSurvey.containsKey(mce.getGuildId().get().asString()) ) {
						listenerMapSurvey.put(mce.getGuildId().get().asString(), new HashMap<String, ValueEventListener>());
					}
					
					SyncedJSONObject register_map = ssf.getJSONObject("%register_map");
					SyncedJSONObject register_survey = ssf.getJSONObject("%register_survey");
					SyncedJSONObject last_map = ssf.getJSONObject("%register_last");
					SyncedJSONObject rep_map = ssf.getJSONObject("%rep_map");
					Map<String, ValueEventListener> listenersLogin = listenerMapLogin.get(mce.getGuildId().get().asString());
					Map<String, ValueEventListener> listenersSurvey = listenerMapSurvey.get(mce.getGuildId().get().asString());

					try {
						final FirebaseIntegration firebase = new FirebaseIntegration(Constants.FIREBASE_URL, Constants.CREDENTIAL_PATH);
						for( String key : register_map.keySet() ) {
							if( !listenersLogin.containsKey(key) ) {
								String path = String.join("/", "users", register_map.getString(key), "lastSeenTimestampHeadset");
								listenersLogin.put(key, firebase.addChangeListener(path, Long.class, t -> {	// Add listener and log it
									try {
										LocalDateTime ldt = LocalDateTime.now(ZoneId.ofOffset("GMT", ZoneOffset.ofHours(-7)));
										String dateString = Constants.SDF.format(Date.valueOf(ldt.toLocalDate()));
										if( !last_map.has(key) ) {
											last_map.put(key, "NEW");
										} else if( !last_map.getString(key).equals(dateString) ) {	// if not seen yet or last seen a day ago
											if( !rep_map.has(key) ) {
												rep_map.put(key, 0);
											}
											rep_map.put(key, rep_map.getInt(key) + ssf.getInt("%register_reward"));
											last_map.put(key, dateString);
										}
									} catch(Exception e) {
										e.printStackTrace();
									}
								}));
							}
						}
					} catch( IOException e) {} catch (InterruptedException e) {}
					
					try {
						final FirebaseIntegration firebase = new FirebaseIntegration(Constants.FIREBASE_URL, Constants.CREDENTIAL_PATH);
						for( String key : register_map.keySet() ) {
							if( !listenersSurvey.containsKey(key) && !register_survey.has(key) ) {
								String path = String.join("/", "users", register_map.getString(key));
								listenersSurvey.put(key, firebase.addChangeListener(path, new GenericTypeIndicator<Map<String,Object>>(){}, t -> {	// Add listener and log it
									if( t.containsKey("profileSurveyAnswers") ) {
										register_survey.put(key, "COMPLETE");
										rep_map.put(key, rep_map.getInt(key) + ssf.getInt("%register_reward"));
										try {
											firebase.removeListener(path, listenersSurvey.get(key));
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
								}));
							}
						}
					} catch( IOException e) {} catch (InterruptedException e) {}
				}
			));
		command.withDependentEffect(mcdm.buildEffect(
			(mce,mc) -> {				
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
				SyncedJSONObject register_map = ssf.getJSONObject("%register_map");
				SyncedJSONObject register_survey = ssf.getJSONObject("%register_survey");
				SyncedJSONObject last_map = ssf.getJSONObject("%register_last");
				SyncedJSONObject rep_map = ssf.getJSONObject("%rep_map");
				int reward = ssf.getInt("%register_reward");
				
				Map<String, ValueEventListener> listenersLogin = listenerMapLogin.get(mce.getGuildId().get().asString());
				Map<String, ValueEventListener> listenersSurvey = listenerMapSurvey.get(mce.getGuildId().get().asString());

				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				String userID = mce.getMessage().getAuthor().get().getId().asString();
				
				FirebaseIntegration firebase;
				try {
					firebase = new FirebaseIntegration(Constants.FIREBASE_URL, Constants.CREDENTIAL_PATH);
				} catch( IOException e ) {
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Internal credential error!")));
				}
								
				try {
					return firebase.<Map<String, Object>>getData("users", new GenericTypeIndicator<Map<String, Object>>(){})
						.flatMapIterable(map -> {	// turn mono of map into flux of keys
							return map.keySet();
						})
						.flatMap(key -> {	// turn flux of keys into flux of simple entries
							try {
								return firebase.<String>getData(String.join("/", "users", key, "email"), String.class, true)	// use keys to get emails
										.map(email -> new SimpleEntry<String, String>(key, email));
							} catch (InterruptedException e) {
								return null;
							}
						})
						.collectList()
						.flatMap(entries -> {	// turn flux of entries into mono result
							for( SimpleEntry<String, String> entry : entries ) {
								if( entry == null || entry.getValue() == null) {
									continue;
								}
								if( entry.getValue().equalsIgnoreCase(ts.getString(1)) ) {	// if the emails match
									return Mono.fromRunnable(() -> {
										try {
											String path = String.join("/", "users", entry.getKey(), "lastSeenTimestampHeadset");
											if( listenersLogin.containsKey(userID) ) {	// Remove any existing listeners
												firebase.removeListener(path, listenersLogin.get(userID));
											}
											register_map.put(userID, entry.getKey());	// identify user for the future
											listenersLogin.put(userID, firebase.addChangeListener(path, Long.class, t -> {	// Add listener and log it
												try {
													LocalDateTime ldt = LocalDateTime.now(ZoneId.ofOffset("GMT", ZoneOffset.ofHours(-7)));
													String dateString = Constants.SDF.format(Date.valueOf(ldt.toLocalDate()));
													if( !last_map.has(userID) ) {
														last_map.put(userID, "NEW");
													} else if( !last_map.getString(userID).equals(dateString) ) {	// if not seen yet or last seen a day ago
														if( !rep_map.has(userID) ) {
															rep_map.put(userID, 0);
														}
														rep_map.put(userID, rep_map.getInt(userID) + reward);
														last_map.put(userID, dateString);
													}
												} catch(Exception e) {
													e.printStackTrace();
												}
											}));
											if( !listenersSurvey.containsKey(userID) && !register_survey.has(userID) ) {
												String path2 = String.join("/", "users", entry.getKey());
												listenersSurvey.put(userID, firebase.addChangeListener(path2, new GenericTypeIndicator<Map<String,Object>>(){}, t -> {	// Add listener and log it
													if( t.containsKey("profileSurveyAnswers") ) {
														register_survey.put(userID, "COMPLETE");
														rep_map.put(userID, rep_map.getInt(userID) + ssf.getInt("%register_reward"));
														try {
															firebase.removeListener(path2, listenersSurvey.get(userID));
														} catch (InterruptedException e) {
															e.printStackTrace();
														}
													}
												}));
											}
										} catch (InterruptedException e) {
											mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Email found, but error occured!"))).block();
										}
									}).and(mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Registered successfully!"))));
								}
							}
							return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Email not found!")));
						});
				} catch (InterruptedException e) {
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Interrupted during request!")));
				}
			}
		));
		
		Command<MessageCreateEvent> reward = command.addSubcommand();
		reward.withCondition(MessageModule.getArgumentCondition(1, "reward"));
		reward.withDependentCondition(MessageModule.getPermissionCondition(psdm, Permission.ADMINISTRATOR));
		reward.withDependentEffect(mcdm.buildEffect((mce,mc) -> {
			SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
			int r = ssf.getInt("%register_reward");
			return mc.createMessage(String.format("The current daily reward for logging into Emerge Home is %d!", r));
		}));
		
		Command<MessageCreateEvent> rewardSet = reward.addSubcommand();
		rewardSet.withCondition(MessageModule.getArgumentsCondition(3));
		rewardSet.withDependentEffect(mcdm.buildEffect((mce,mc) -> {
			SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
			TokenizedString ts = MessageModule.tokenizeMessage(mce);
			Token t = ts.getToken(2);
			if( t.isInteger() ) {
				return Mono.fromRunnable(() -> {
					ssf.put("%register_reward", t.asInteger());
				}).and(mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Reward set successfully!"))));
			} else {
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Reward must be an integer!")));
			}
		}));
		return command;
	}

	
	
}
