package alina.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * An extension of ArrayList that ensures that all elements contained within are unique.
 * If modification of an instance of this object leads to a duplicate element, only the element that was already in the List to begin with will be kept.
 * All other operations will still have an effect; e.g. replacing an element with an element that already exists will simply result in the element being replaced being removed.
 * @author Auxiliatrix
 *
 * @param <E> Element to contain within the List
 */
public class ExclusiveList<E> extends ArrayList<E> {

	private static final long serialVersionUID = 2885010757791302920L;
	private Set<E> exclusionSet;
	
	public ExclusiveList(int initialCapacity) {
		super(initialCapacity);
		exclusionSet = new HashSet<E>();
	}
	
	public ExclusiveList() {
		super();
		exclusionSet = new HashSet<E>();
	}
	
	public ExclusiveList(Collection<? extends E> c) {
		super(new HashSet<E>(c));
		exclusionSet = new HashSet<E>();
	}
	
	@Override
	public E set(int index, E element) {
		E e = remove(index);
		add(element);
		return e;
	}
	
	@Override
	public boolean add(E element) {
		if( !exclusionSet.contains(element) ) {
			exclusionSet.add(element);
			return super.add(element);
		}
		return false;
	}
	
	@Override
	public void add(int index, E element) {
		if( !exclusionSet.contains(element) ) {
			exclusionSet.add(element);
			super.add(index, element);
		}
	}
	
	@Override
	public E remove(int index) {
		if( exclusionSet.contains(get(index)) ) {
			exclusionSet.remove(get(index));
		}
		return super.remove(index);
	}
	
	@Override
	public boolean remove(Object o) {
		if( exclusionSet.contains(o) ) {
			exclusionSet.remove(o);
		}
		return super.remove(o);
	}
	
	@Override
	public void clear() {
		exclusionSet.clear();
		super.clear();
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c) {
		Set<E> remainder = new HashSet<E>(c);
		remainder.removeAll(exclusionSet);
		exclusionSet.addAll(remainder);
		return super.addAll(remainder);
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		int insert = index;
		for( E e : c ) {
			if( !exclusionSet.contains(e) ) {
				exclusionSet.add(e);
				add(insert, e);
				insert++;
			}
		}
		return insert > index;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		Set<E> toRemove = new HashSet<E>();
		for( E e : this ) {
			if( c.contains(e) ) {
				toRemove.add(e);
			}
		}
		if( toRemove.size() > 0 ) {
			for( E e : toRemove ) {
				remove(e);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		Set<E> toRemove = new HashSet<E>();
		for( E e : this ) {
			if( !c.contains(e) ) {
				toRemove.add(e);
			}
		}
		if( toRemove.size() > 0 ) {
			for( E e : toRemove ) {
				remove(e);
			}
			return true;
		}
		return false;
	}
	
	@Override
    public boolean removeIf(Predicate<? super E> filter) {
        return super.removeIf(e -> { exclusionSet.remove(e); return filter.test(e); } );
    }
	
	@Override
	public void replaceAll(UnaryOperator<E> operator) {
		List<E> replacement = new ArrayList<E>(this);
		replacement.replaceAll(operator);
		clear();
		addAll(replacement);
	}
}
