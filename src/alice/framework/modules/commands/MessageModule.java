package alice.framework.modules.commands;

import java.util.List;
import java.util.function.Function;

import alice.framework.modules.tasks.Dependency;
import alice.framework.modules.tasks.EffectFactory;
import alice.framework.structures.TokenizedString;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Role;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;

public abstract class MessageModule extends Module<MessageCreateEvent> {

	public MessageModule(Class<MessageCreateEvent> type) {
		super(type);
	}
	
	public static TokenizedString tokenizeMessage(MessageCreateEvent mce) {
		return new TokenizedString(mce.getMessage().getContent());
	}
	
	public static Function<MessageCreateEvent, Boolean> getInvokedCondition(String invocation) {
		return mce -> {
			TokenizedString ts = tokenizeMessage(mce);
			return ts.size() > 1 && ts.getString(0).equalsIgnoreCase(invocation);
		};
	}
	
	public static Function<MessageCreateEvent, Boolean> getArgumentCondition(int position, String argument) {
		return getArgumentCondition(position, argument, true);
	}
	
	public static Function<MessageCreateEvent, Boolean> getArgumentCondition(int position, String argument, boolean ignoreCase) {
		return mce -> {
			TokenizedString ts = tokenizeMessage(mce);
			return ts.size() > position && (ignoreCase ? ts.getString(position).equalsIgnoreCase(argument) : ts.getString(position).equals(argument));
		};
	}
	
	public static Function<Dependency<MessageCreateEvent>, Boolean> getRoleCondition(EffectFactory<MessageCreateEvent,List<Role>> retriever, Role condition) {
		return d -> {
			List<Role> roles = d.<List<Role>>request(retriever);
			for( Role role : roles ) {
				if( role.equals(condition) ) {
					return true;
				}
			}
			return false;
		};
	}
	
	public static Function<Dependency<MessageCreateEvent>, Boolean> getPermissionCondition(EffectFactory<MessageCreateEvent,PermissionSet> retriever, Permission condition) {
		return d -> {
			PermissionSet permissions = d.<PermissionSet>request(retriever);
			for( Permission permission : permissions ) {
				if( permission.equals(condition) ) {
					return true;
				}
			}
			return false;
		};
	}
}
