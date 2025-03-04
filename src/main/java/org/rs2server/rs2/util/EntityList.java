package org.rs2server.rs2.util;

import org.rs2server.rs2.model.Mob;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;


/**
 * A class which represents a list of entities.
 * @author Graham Edgecombe
 *
 * @param <E> The type of entity.
 */
public class EntityList<E extends Mob> implements Collection<E>, Iterable<E> {
	
	/**
	 * Internal entities array.
	 */
	private Mob[] mobs;
	
	/**
	 * Current size.
	 */
	private int size = 0;
	
	/**
	 * Creates an entity list with the specified capacity.
	 * @param capacity The capacity.
	 */
	public EntityList(int capacity) {
		mobs = new Mob[capacity+1]; // do not use idx 0
	}
	
	/**
	 * Gets an entity.
	 * @param index The index.
	 * @return The entity.
	 * @throws IndexOutOufBoundException if the index is out of bounds.
	 */
	public Mob get(int index) {
		if(index <= 0 || index >= mobs.length) {
			throw new IndexOutOfBoundsException();
		}
		return mobs[index];
	}
	
	/**
	 * Gets the index of an entity.
	 * @param mob The entity.
	 * @return The index in the list.
	 */
	public int indexOf(Mob mob) {
		return mob.getIndex();
	}
	
	/**
	 * Gets the next free id.
	 * @return The next free id.
	 */
	private int getNextId() {
		for(int i = 1; i < mobs.length; i++) {
			if(mobs[i] == null) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean add(E arg0) {
		int id = getNextId();
		if(id == -1) {
			return false;
		}
		mobs[id] = arg0;
		arg0.setIndex(id);
		size++;
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> arg0) {
		boolean changed = false;
		for(E entity : arg0) {
			if(add(entity)) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public void clear() {
		for(int i = 1; i < mobs.length; i++) {
			mobs[i] = null;
		}
		size = 0;
	}

	@Override
	public boolean contains(Object arg0) {
		for(int i = 1; i < mobs.length; i++) {
			if(mobs[i] == arg0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		for(Object o : arg0) {
			if(!contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public Iterator<E> iterator() {
		return new EntityListIterator<E>(this);
	}

	@Override
	public boolean remove(Object arg0) {
		for(int i = 1; i < mobs.length; i++) {
			if(mobs[i] == arg0) {
				mobs[i] = null;
				size--;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean changed = false;
		for(Object o : arg0) {
			if(remove(o)) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		boolean changed = false;
		for(int i = 1; i < mobs.length; i++) {
			if(mobs[i] != null) {
				if(!arg0.contains(mobs[i])) {
					mobs[i] = null;
					size--;
					changed = true;
				}
			}
		}
		return changed;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Mob[] toArray() {
		int size = size();
		Mob[] array = new Mob[size];
		int ptr = 0;
		for(int i = 1; i < mobs.length; i++) {
			if(mobs[i] != null) {
				int k = ptr++;
				if(k < array.length) {
					array[k] = mobs[i];
				}
			}
		}
		return array;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] arg0) {
		Mob[] arr = toArray();
		return (T[]) Arrays.copyOf(arr, arr.length, arg0.getClass());
	}

}
