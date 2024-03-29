package alice.framework.modules;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import alice.framework.dependencies.DependencyMap;
import alina.structures.TokenizedString;
import alice.framework.dependencies.DependencyManager;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Role;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;

public abstract class MessageModule extends Module<MessageCreateEvent> {

	public static final String PREFIX = "%";
	
	public MessageModule() {
		super(MessageCreateEvent.class);
	}
	
	public static TokenizedString tokenizeMessage(MessageCreateEvent mce) {
		return new TokenizedString(mce.getMessage().getContent());
	}
	
	public static Function<MessageCreateEvent, Boolean> getInvokedCondition(String invocation) {
		return getArgumentCondition(0, PREFIX+invocation);
	}
	
	public static Function<MessageCreateEvent, Boolean> getInvokedCondition(String invocation, String prefix) {
		return getArgumentCondition(0, prefix+invocation);
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
	
	public static Function<MessageCreateEvent, Boolean> getMatchCondition(String pattern, int position) {
		return mce -> {
			TokenizedString ts = tokenizeMessage(mce);
			Pattern checker = Pattern.compile(pattern);
			Matcher matcher = checker.matcher(ts.getString(position));
			return matcher.matches();
		};
	}
	
	public static Function<MessageCreateEvent, Boolean> getMatchCondition(String pattern) {
		return mce -> {
			Pattern checker = Pattern.compile(pattern);
			Matcher matcher = checker.matcher(mce.getMessage().getContent());
			return matcher.matches();
		};
	}
	
	public static Function<MessageCreateEvent, Boolean> getArgumentsCondition(int count) {
		return mce -> {
			TokenizedString ts = tokenizeMessage(mce);
			return ts.size() >= count;
		};
	}
	
	public static Function<MessageCreateEvent, Boolean> getDMCondition() {
		return mce -> !mce.getGuildId().isPresent();
	}
	
	public static Function<MessageCreateEvent, Boolean> getGuildCondition() {
		return mce -> mce.getGuildId().isPresent();
	}
	
	public static Function<MessageCreateEvent, Boolean> getHumanCondition() {
		return mce -> mce.getMessage().getAuthor().isPresent();
	}
	
	public static Function<MessageCreateEvent, Boolean> getBotCondition() {
		return mce -> !mce.getMessage().getAuthor().isPresent();
	}
	
	public static Function<MessageCreateEvent, Boolean> getMentionsCondition(int count) {
		return mce -> mce.getMessage().getUserMentionIds().size() >= count; 
	}
	
	public static Function<MessageCreateEvent, Boolean> getMentionsCondition(Snowflake user) {
		return mce -> mce.getMessage().getUserMentionIds().contains(user); 
	}
	
	public static Function<MessageCreateEvent, Boolean> getMentionsAllCondition(Snowflake... users) {
		return mce -> {
			for( Snowflake user : users ) {
				if( !mce.getMessage().getUserMentionIds().contains(user) ) {
					return false;
				}
			}
			return true;
		};
	}
	
	public static Function<MessageCreateEvent, Boolean> getMentionsAnyCondition(Snowflake... users) {
		return mce -> {
			for( Snowflake user : users ) {
				if( mce.getMessage().getUserMentionIds().contains(user) ) {
					return true;
				}
			}
			return false;
		};
	}
	
	public static Function<MessageCreateEvent, Boolean> getRoleMentionsCondition(int count) {
		return mce -> mce.getMessage().getRoleMentionIds().size() >= count; 
	}
	
	public static Function<MessageCreateEvent, Boolean> getRoleMentionsCondition(Snowflake role) {
		return mce -> mce.getMessage().getRoleMentionIds().contains(role); 
	}
	
	public static Function<MessageCreateEvent, Boolean> getRoleMentionsAllCondition(Snowflake... roles) {
		return mce -> {
			for( Snowflake role : roles ) {
				if( !mce.getMessage().getRoleMentionIds().contains(role) ) {
					return false;
				}
			}
			return true;
		};
	}
	
	public static Function<MessageCreateEvent, Boolean> getRoleMentionsAnyCondition(Snowflake... roles) {
		return mce -> {
			for( Snowflake role : roles ) {
				if( mce.getMessage().getRoleMentionIds().contains(role) ) {
					return true;
				}
			}
			return false;
		};
	}
	
	public static Function<DependencyMap<MessageCreateEvent>, Boolean> getRoleCondition(DependencyManager<MessageCreateEvent,List<Role>> retriever, Role condition) {
		return d -> {
			List<Role> roles = retriever.requestFrom(d);
			for( Role role : roles ) {
				if( role.equals(condition) ) {
					return true;
				}
			}
			return false;
		};
	}
	
	public static Function<DependencyMap<MessageCreateEvent>, Boolean> getPermissionCondition(DependencyManager<MessageCreateEvent,PermissionSet> retriever, Permission condition) {
		return d -> {
			PermissionSet permissions = retriever.requestFrom(d);
			for( Permission permission : permissions ) {
				if( permission.equals(condition) ) {
					return true;
				}
			}
			return false;
		};
	}
}
