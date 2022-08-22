package alice.modular.modules;

import java.io.IOException;
import java.util.Map;

import com.google.firebase.database.GenericTypeIndicator;

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
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class RegisterModule extends MessageModule {

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdm = dfb.addDependency(mce -> mce.getMessage().getChannel());
		
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
				}
			));
		command.withDependentEffect(mcdm.buildEffect(
			(mce,mc) -> {
				SyncedJSONObject ssf = SaveFiles.ofGuild(mce.getGuildId().get().asLong());
				SyncedJSONObject register_map = ssf.getJSONObject("%register_map");
				TokenizedString ts = MessageModule.tokenizeMessage(mce);
				FirebaseIntegration firebase;
				try {
					firebase = new FirebaseIntegration(Constants.FIREBASE_URL, Constants.CREDENTIAL_PATH);
				} catch( IOException e ) {
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Internal credential error!")));
				}
				
				try {
					Map<String, Object> map = firebase.<Map<String, Object>>getData("users", new GenericTypeIndicator<Map<String, Object>>(){}, true).block();
					for( String key : map.keySet() ) {
						String email = firebase.<String>getData(String.join("/", "users", key, "email"), String.class, true).block();
						if( email.equalsIgnoreCase(ts.getString(1)) ) {
							register_map.put(mce.getMessage().getAuthor().get().getId().asString(), key);
							return mc.createMessage(EmbedFactory.build(EmbedFactory.modSuccessFormat("Registered successfully!")));
						}
					}
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Email not found!")));
				} catch (InterruptedException e) {
					return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Interrupted during request!")));
				}
			}
		));
		
		return command;
	}

	
	
}
