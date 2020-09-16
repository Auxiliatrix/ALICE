package alice.framework.embedbuilders;

import alice.configuration.calibration.Constants;
import alice.framework.main.Brain;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

public class CreditsBuilder {
		
	public static synchronized EmbedCreateSpec constructCredits( EmbedCreateSpec spec ) {
		spec.setColor(Color.of(253, 185, 200));
		spec.setAuthor(Constants.NAME, Constants.LINK, Brain.client.getSelf().block().getAvatarUrl());
		spec.setTitle("Developed by Alina Kim");
		spec.setDescription("Built using the [Java Discord4j Framework](https://github.com/Discord4J/Discord4J)");
		spec.addField("Alina Kim (Developer)", "https://www.github.com/Auxiliatrix", false);
		spec.addField("Emily Lee (Artist)", "https://emlee.carrd.co/", false);
		return spec;
	}
	
}
