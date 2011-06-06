package joe.collect;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Joiner;

/**
 * Abstract implementation of a set backed by a pair of arrays, one for elements and one for values. Implementors need only
 * decide how to implement methods to pick an entry index or indicate where a new entry should be inserted. This set
 * implementation has an almost-minimal memory overhead of two array references and a modification counter.
 * 
 * @author Joe Kearney
 * @param <E> type of the elements stored in the set
 */
public abstract class AbstractArrayBackedSet<E> implements Set<E> {
	/*
	 * INVARIANTS:
	 * elements != null
	 */

	E[] elements;

	transient int modCount;

	public AbstractArrayBackedSet() {
		clear();
	}
	public AbstractArrayBackedSet(Set<? extends E> set) {
		addAll(set);
	}

	@Override
	public final boolean contains(Object element) {
		return getIndex(element) >= 0;
	}
	@Override
	public final int size() {
		return elements.length;
	}
	@Override
	public boolean isEmpty() {
		return elements.length == 0;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object instanceof Set) {
			// TODO
		}
		return false;
	}
	@Override
	public int hashCode() {
		// TODO
		return 0;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		Joiner.on(", ").appendTo(sb , this);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public final boolean add(E element) {
		if (element == null) {
			throw new NullPointerException("null element not permitted");
		}

		int index = getIndex(element);
		if (index < 0) {
			index = getIndexForNewEntry(element);
			putAtIndex(index, element);
			return true;
		} else {
			return false;
		}
	}
	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean modified = false;
		for (E e : c) {
			modified |= add(e);
		}
		return modified;
	}

	@Override
	public final boolean remove(Object element) {
		int index = getIndex(element);
		if (index < 0) {
			return false;
		} else {
			removeIndex(index);
			return true;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	// we're only going to put objects of the right type in it
	public final void clear() {
		elements = (E[]) new Object[0];
	}

	protected final E[] getelementsArray() {
		return elements;
	}

	/**
	 * Gets the array index of the specified element, or a negative value if it is not present in the set.
	 * 
	 * @param element element for which to search
	 * @return index of the element, or a negative value if it is not present in the set
	 */
	protected abstract int getIndex(Object element);
	/**
	 * Gets the index for insertion of a new element, where the element is
	 * not currently in the set. The index may depend on the element. The returned value must be in the range
	 * {@code [0, size()]}. If it is equal to size of the set prior to this insertion, then the new entry
	 * will be placed at the tail of the array.
	 * 
	 * @param element the new element
	 * @return index at which to insert the new element-value pair
	 */
	protected abstract int getIndexForNewEntry(E element);

	/**
	 * Checks that the parameter is equal to the expected mod count, and throws {@link ConcurrentModificationException}
	 * if not.
	 * 
	 * @param expectedModCount expected mod count
	 */
	protected final void checkForComodification(int expectedModCount) {
		if (expectedModCount != modCount) {
			throw new ConcurrentModificationException();
		}
	}

	private final void putAtIndex(int index, E element) {
		int oldSize = size();
		int newSize = oldSize + 1;

		assert index >= 0 && index <= oldSize;

		@SuppressWarnings("unchecked")
		E[] newelements = (E[]) new Object[newSize];

		System.arraycopy(elements, 0, newelements, 0, index);
		if (index < oldSize) { // else inserting at end
			System.arraycopy(elements, index, newelements, index + 1, oldSize - index);
		}

		newelements[index] = element;

		++modCount;
		this.elements = newelements;
	}

	/**
	 * Removes the element and value at the specified index. This assumes that such
	 * a pair exists, and performs no range checking.
	 * 
	 * @param index index at which to remove the setping
	 */
	final void removeIndex(int index) {
		int priorSize = size();
		int newSize = priorSize - 1;
		@SuppressWarnings("unchecked")
		E[] newElements = (E[]) new Object[newSize];

		System.arraycopy(elements, 0, newElements, 0, index);
		System.arraycopy(elements, index + 1, newElements, index, newSize - index);

		++modCount;
		this.elements = newElements;
	}

	protected static final int findByScan(Object value, Object[] array) {
		if (value == null) {
			return -1;
		}

		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(value)) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public Iterator<E> iterator() {
		return new ElementIterator();
	}

	// TODO weakly-consistent version that does not throw CME?
	private abstract class IndexIterator {
		private int expectedModCount = modCount;
		private int index = -1;

		IndexIterator() {}

		public final boolean hasNext() {
			return modCount == expectedModCount && size() > index + 1;
		}

		final int nextIndex() {
			checkForComodification(expectedModCount);
			int thisIndex = index + 1;
			if (thisIndex == size()) {
				throw new NoSuchElementException();
			}

			index = thisIndex;
			currentReady = true;
			assert index < size();
			return thisIndex;
		}

		/** marker that we have returned and not removed a {@code next} element */
		private boolean currentReady = false;
		public final void remove() {
			if (!currentReady) {
				throw new IllegalStateException("next() has not been called "
						+ "or the current element has already been removed.");
			}

			checkForComodification(expectedModCount);
			// decrement so that next gets the entry in the index just removed
			removeIndex(index--);
			currentReady = false;
			expectedModCount = modCount;
		}
	}

	private final class ElementIterator extends IndexIterator implements Iterator<E> {
		ElementIterator() {}

		@Override
		public E next() {
			return elements[nextIndex()];
		}
	}
}
