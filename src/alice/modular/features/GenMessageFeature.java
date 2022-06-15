package alice.modular.features;

import alice.framework.features.MessageFeature;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.tasks.Stacker;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.InviteCreateSpec;
import discord4j.rest.entity.RestChannel;
import reactor.core.publisher.Mono;

public class GenMessageFeature extends MessageFeature {

    public GenMessageFeature() {
        super("Gen");
        withCheckInvoked();
        withRestriction(PermissionProfile.getAdminPreset());
    }

    @Override
    protected boolean condition(MessageCreateEvent event) {
        return true;
    }

    @Override
    protected Mono<?> respond(MessageCreateEvent type) {
        Stacker response = new Stacker();
        System.out.println("gen");
        TokenizedString ts = new TokenizedString(type.getMessage().getContent());
        RestChannel rc = type.getMessage().getRestChannel();
        if( ts.size() < 2 ) {
            System.out.println(genInvite(rc));
        } else {
            StringBuilder sb = new StringBuilder();
            int count = ts.getToken(1).asInteger();
            for( int f=0; f<count; f++ ) {
                System.out.println(f);
                sb.append(genInvite(rc));
                sb.append(",");
            }
            System.out.println(sb.toString());
        }
        return response.toMono();
    }

    protected String genInvite(RestChannel rc) {
        String code = rc.createInvite(InviteCreateSpec.builder().maxUses(1).temporary(false).unique(true).build().asRequest(), invocation).block().code();
        return "discord.gg/" + code;
    }

}