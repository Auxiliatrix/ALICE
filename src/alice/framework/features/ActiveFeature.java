package alice.framework.features;

import java.util.PriorityQueue;

import alice.framework.main.Brain;
import alice.framework.structures.PermissionProfile;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.Member;
import reactor.util.annotation.Nullable;

/**
 * A Feature implementation with restrictions
 * @author Auxiliatrix
 *
 * @param <E> Event class to activate on
 */
public abstract class ActiveFeature<E extends Event> extends Feature<E> implements Comparable<ActiveFeature<E>> {
	
	/**
	 * A collection of permissions required in order for a user to trigger this Feature.
	 */
	protected PermissionProfile restriction;
	
	/**
	 * Construct an ActiveFeature with a name and an Event type.
	 * @param name String used to refer to this feature
	 * @param type Event class to activate on
	 */
	protected ActiveFeature(String name, Class<E> type) {
		super(name, type);
		withRestriction(PermissionProfile.getAnyonePreset());
	}
	
	/**
	 * Set the PermissionProfile of this Feature.
	 * @param restriction PermissionProfile of restrictions to enforce
	 * @return the modified Feature
	 */
	protected ActiveFeature<E> withRestriction(PermissionProfile restriction) {
		// TODO: null checking
		this.restriction = restriction;
		return this;
	}
	
	/**
	 * Check whether the restrictions allow a given Member to activate this Feature.
	 * @param member Member to check permissions for
	 * @return whether or not the given Member can activate this Feature
	 */
	protected boolean isAllowed(Member member) {
		return restriction.verify(member, member.getGuild().block());
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void load(Class<E> type) {
		if(!Brain.features.get().keySet().contains(type)) {
			Brain.features.get().put(type, new PriorityQueue<ActiveFeature>());
		}
		Brain.features.get().get(type).add(this);
	}
}
