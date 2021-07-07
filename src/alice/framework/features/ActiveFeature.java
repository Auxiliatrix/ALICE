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
	 * Each Exclusion Class that is activated will prevent the activation of any Features in the Exclusion Classes below it.
	 * @author Auxiliatrix
	 *
	 */
	public static enum ExclusionClass {
		DOMINANT,	// Prevents non-dominant Features from activating. Best used with Features that alter save data, or important soft-matched Features to prevent overlap with other trigger conditions.
		STANDARD,	// In most cases, this Feature will be activated as intended. Best used with hard-matched cases that are mostly self-contained.
		SUBMISSIVE,	// Will only activate if no non-submissive Features are activated. Best used with soft-matching Features to prevent collisions with hard-matched ones.
	};
	
	/**
	 * What Exclusion Class this Feature belongs to. If set to null, this Feature will not stop any other Features from activating, nor will it be stopped by any other Features.
	 */
	protected ExclusionClass exclusionClass;
	
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
		withExclusionClass(null);
		withRestriction(PermissionProfile.getAnyonePreset());
	}
	
	/**
	 * Set the Exclusion Class of this Feature. Can be null.
	 * @param exclusionClass
	 * @return the modified Feature
	 */
	protected ActiveFeature<E> withExclusionClass(@Nullable ExclusionClass exclusionClass) {
		this.exclusionClass = exclusionClass;
		return this;
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
