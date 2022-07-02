package alice.modular.modules;

import java.util.HashMap;
import java.util.Map;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.dependencies.EffectFactory;
import alice.framework.modules.MessageModule;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.FileIO;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;

public class DMModule extends MessageModule {

	public DMModule() {
		super();
	}

	@Override
	public Command<MessageCreateEvent> buildCommand(DependencyFactory.Builder<MessageCreateEvent> dfb) {
		EffectFactory<MessageCreateEvent,MessageChannel> mcef = dfb.addDependency(mce -> mce.getMessage().getAuthor().get().getPrivateChannel());
		EffectFactory<MessageCreateEvent,TokenizedString> tsef = dfb.addWrappedDependency(mce -> tokenizeMessage(mce));
		EffectFactory<MessageCreateEvent,Message> mef = dfb.addWrappedDependency(mce -> mce.getMessage());
		
		DependencyFactory<MessageCreateEvent> df = dfb.buildDependencyFactory();
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(mce -> mce.getMessage().getAuthor().isPresent());
		command.withCondition(MessageModule.getInvokedCondition("%tier"));
		command.withDependentEffect(
				mcef.with(tsef).with(mef)
				.getEffect(
						(mc,ts,m) -> {
							return mc.createMessage(lookup(ts.getString(1)))
									.and(m.addReaction(ReactionEmoji.unicode("\u2705")));
						}
					));
		
		return command;
	}
	
	protected String lookup(String email) {
		System.out.println(email);
		String content = FileIO.readFromFile("lab/tiers.csv");
		String[] lines = content.split("\n");
		Map<String,String> tierMap = new HashMap<String, String>();
		for( int f=1; f<lines.length; f++ ) {
			String[] line = lines[f].split(",");
			if( line.length > 1 ) {
				tierMap.put(line[0].toLowerCase(), line[1]);
				for( int g=2; g<line.length; g++ ) {
					String token = line[g];
					if( !token.isEmpty() ) {
						tierMap.put(token.toLowerCase(), line[1]);
					}
				}
			}
		}
		if( tierMap.containsKey(email.toLowerCase()) ) {
			return tierMap.get(email.toLowerCase());
		} else {
			return "Sorry, I couldn't find your email in the database.";
		}
	}
	
}
