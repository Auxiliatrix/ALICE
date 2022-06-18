package alice.modular.modules;

import java.util.HashMap;
import java.util.Map;

import alice.framework.modules.commands.Command;
import alice.framework.modules.commands.Module;
import alice.framework.modules.tasks.DependencyFactory;
import alice.framework.modules.tasks.EffectFactory;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.FileIO;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class TierModule extends Module<MessageCreateEvent> {

	public TierModule() {
		super(MessageCreateEvent.class);
	}

	@Override
	public Command<MessageCreateEvent> buildCommand() {
		DependencyFactory.Builder<MessageCreateEvent> dfb = DependencyFactory.builder();
		EffectFactory<MessageCreateEvent, MessageChannel> mcef = dfb.addDependency(mce -> mce.getMessage().getChannel());
		
		DependencyFactory<MessageCreateEvent> df = dfb.buildDependencyFactory();

		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		command.withCondition(mce -> mce.getMessage().getContent().startsWith("%tier"));
		command.withDependentEffect(d -> {
			MessageChannel mc = d.<MessageChannel>request(mcef.getRetriever());
			MessageCreateEvent mce = d.getEvent();
			String content = mce.getMessage().getContent();
			TokenizedString ts = new TokenizedString(content);
			if( ts.size() > 1 ) {
				String email = ts.getString(1);
				String tier = lookup(email);
				return mc.createMessage(tier);
			} else {
				return mc.createMessage("This module can be used to find what tier you are. `%tier email@email.com`");
			}
		});
		return command;
	}
	
	protected String lookup(String email) {
		System.out.println(email);
		String content = FileIO.readFromFile("lab/tiers.csv");
		String[] lines = content.split("\n");
		Map<String,String> tierMap = new HashMap<String, String>();
		for( int f=1; f<lines.length; f++ ) {
			String[] line = lines[f].split(",");
			if( line.length == 2 ) {
				tierMap.put(line[0].toLowerCase(), line[1]);
			}
		}
		if( tierMap.containsKey(email.toLowerCase()) ) {
			return tierMap.get(email.toLowerCase());
		} else {
			return "Sorry, I couldn't find your email in the database.";
		}
	}

}
