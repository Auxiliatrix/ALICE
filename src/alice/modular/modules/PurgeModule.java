package alice.modular.modules;

import java.util.List;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.DependencyFactory.Builder;
import alice.framework.dependencies.DependencyManager;
import alice.framework.modules.MessageModule;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Message.Type;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Flux;

public class PurgeModule extends MessageModule {

	@Override
	public Command<MessageCreateEvent> buildCommand(Builder<MessageCreateEvent> dfb) {
		DependencyManager<MessageCreateEvent, MessageChannel> mcdf = dfb.addDependency(mce -> mce.getMessage().getChannel());
		DependencyManager<MessageCreateEvent, PermissionSet> psdf = dfb.addDependency(mce -> mce.getMember().get().getBasePermissions());

		DependencyFactory<MessageCreateEvent> df = dfb.build();
		
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(MessageModule.getInvokedCondition("%purge"));
		command.withCondition(MessageModule.getGuildCondition());
		command.withCondition(MessageModule.getHumanCondition());
		command.withDependentCondition(MessageModule.getPermissionCondition(psdf, Permission.ADMINISTRATOR));
		
		Command<MessageCreateEvent> execute = command.addSubcommand();
		execute.withCondition(MessageModule.getArgumentsCondition(2));
		execute.withDependentEffect(d -> {
			Message m = d.getEvent().getMessage();
			MessageChannel mc = mcdf.requestFrom(d);
			TokenizedString ts = MessageModule.tokenizeMessage(d.getEvent());
			String start = "";
			String end = "";
			int quantity = -1;
			List<Snowflake> targets = m.getUserMentionIds();
			
			if( ts.getToken(1).isInteger() ) {
				quantity = ts.getToken(1).asInteger();
			}
			
			if( m.getType().equals(Type.REPLY) ) {
				String referenced = m.getReferencedMessage().get().getId().asString();
				if( ts.containsAnyIgnoreCase("--before","-b") ) {
					end = referenced;
				}
				if( ts.containsAnyIgnoreCase("--after","-a") ) {
					start = referenced;
				}
			}
			
			if( ts.containsAnyIgnoreCase("--override","-o") ) {
				quantity = -2;
			}
			
			if( ts.containsAnyIgnoreCase("--start","-s") ) {
				int index = Math.max(ts.getIndexIgnoreCase("--start"), ts.getIndexIgnoreCase("-s"));
				if( ts.size() > index+1 ) {
					start = ts.getToken(index+1).toString();
				}
			}
			
			if( ts.containsAnyIgnoreCase("--end","-e") ) {
				int index = Math.max(ts.getIndexIgnoreCase("--end"), ts.getIndexIgnoreCase("-e"));
				if( ts.size() > index+1 ) {
					end = ts.getToken(index+1).toString();
				}
			}
			
			Flux<Message> queue;

			if( !start.isEmpty() ) {
				queue = mc.getMessagesAfter(Snowflake.of(start));
				if( !end.isEmpty() ) {
					final String start_f = start;
					queue = queue.takeWhile(message -> message.getId().compareTo(Snowflake.of(start_f)) <= 0);
				}
				
			} else if( !end.isEmpty() ) {
				queue = mc.getMessagesBefore(Snowflake.of(end));
			} else {
				queue = mc.getMessagesBefore(m.getId());
			}
			
			queue = queue.filter(message -> targets.size() == 0 || message.getAuthor().isPresent() && targets.contains(message.getAuthor().get().getId()));
			
			switch(quantity) {
				case -2:
					break;
				case -1:
					queue = queue.take(100, true);
					break;
				default:
					queue = queue.take(quantity, true);
					break;
			}
			
			return queue.flatMap(message -> message.delete()).count().flatMap(l -> mc.createMessage(EmbedBuilders.applySuccessFormat("Purged messages!")));
		});
		
		return command;
	}

}
