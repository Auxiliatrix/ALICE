package alice.framework.features;

import java.util.PriorityQueue;

import alice.framework.main.Brain;
import alice.framework.structures.PermissionProfile;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.Member;

public abstract class ActiveFeature<E extends Event> extends Feature<E> implements Comparable<ActiveFeature<E>> {

	public static enum PriorityClass {
		DOMINANT,	// Prevents any other features from activating
		STANDARD,	// Only the first Standard feature will be activated
		SUBMISSIVE,	// Will only activate if no other features are activated
	};
	
	protected PriorityClass priority;
	protected PermissionProfile restriction;
	
	protected ActiveFeature(String name, Class<E> type) {
		super(name, type);
		withPriority(PriorityClass.STANDARD);
		withRestriction(PermissionProfile.getAnyonePreset());
	}
	
	protected ActiveFeature<E> withPriority(PriorityClass priority) {
		this.priority = priority;
		return this;
	}
	
	protected ActiveFeature<E> withRestriction(PermissionProfile restriction) {
		this.restriction = restriction;
		return this;
	}
	
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
	
	@Override
	public int compareTo(ActiveFeature<E> f) {
		return priority.ordinal() - f.priority.ordinal();
	}
}
