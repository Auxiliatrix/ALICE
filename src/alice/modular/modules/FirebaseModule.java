package alice.modular.modules;

import java.io.IOException;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.main.Constants;
import alice.framework.modules.MessageModule;
import alice.framework.utilities.EmbedFactory;
import alina.firebase.FirebaseIntegration;
import alina.structures.TokenizedString;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;

public class FirebaseModule extends MessageModule {

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdm = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent, PermissionSet> psdm = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());
		
		DependencyFactory<MessageCreateEvent> df = dfb.build();
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getHumanCondition());
		command.withDependentCondition(MessageModule.getPermissionCondition(psdm, Permission.ADMINISTRATOR));
		command.withCondition(MessageModule.getInvokedCondition("fb"));
		
		command.withDependentEffect(mcdm.buildEffect((mce,mc) -> {
			TokenizedString ts = MessageModule.tokenizeMessage(mce);
			ts = ts.getSubTokens(1);
			FirebaseIntegration firebase;
			try {
				firebase = new FirebaseIntegration(Constants.FIREBASE_URL, Constants.CREDENTIAL_PATH);
			} catch( IOException e ) {
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Internal credential error!")));
			}
			
			String query = String.join("/", ts.getStrings());
			
			try {
				return firebase.<String>getData(query, String.class).flatMap(s -> mc.createMessage(s));
			} catch (InterruptedException e) {
				return mc.createMessage(EmbedFactory.build(EmbedFactory.modErrorFormat("Interrupted during request!")));
			}			
		}));
		
		return command;
	}

}
