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
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.EmojiData;

public class DMModule extends Module<MessageCreateEvent> {

	public DMModule() {
		super(MessageCreateEvent.class);
	}

	@Override
	public Command<MessageCreateEvent> buildCommand() {
		DependencyFactory.Builder<MessageCreateEvent> dfb = DependencyFactory.<MessageCreateEvent>builder();
		EffectFactory<MessageCreateEvent,MessageChannel> mcef = dfb.addDependency(mce -> mce.getMessage().getAuthor().get().getPrivateChannel());
		
		DependencyFactory<MessageCreateEvent> df = dfb.buildDependencyFactory();
		Command<MessageCreateEvent> command = new Command<MessageCreateEvent>(df);
		
		command.withCondition(mce -> mce.getMessage().getAuthor().isPresent());
		command.withCondition(mce -> {
			TokenizedString ts = tokenizeMessage(mce);
			return ts.getToken(0).toString().equalsIgnoreCase("%tier") && ts.size() > 1;
		});
		
		command.withDependentEffect(d -> {
			MessageChannel dm = d.<MessageChannel>request(mcef);
			TokenizedString ts = tokenizeMessage(d.getEvent());
			String email = ts.getString(1);
			String tier = lookup(email);
			return dm.createMessage(tier).and(d.getEvent().getMessage().addReaction(ReactionEmoji.unicode("\u2705")));
		});
		
		
		return command;
	}
	
	public static TokenizedString tokenizeMessage(MessageCreateEvent mce) {
		return new TokenizedString(mce.getMessage().getContent());
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
