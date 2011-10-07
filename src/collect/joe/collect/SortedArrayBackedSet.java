package joe.collect;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.Ordering;

public final class SortedArrayBackedSet<E> extends AbstractArrayBackedSet<E> implements SortedSet<E> {
	private final Comparator<? super E> comparator;

	public SortedArrayBackedSet() {
		this.comparator = null;
	}
	public SortedArrayBackedSet(Comparator<? super E> comparator) {
		this.comparator = comparator;
	}
	public SortedArrayBackedSet(Set<? extends E> set) {
		this(set, null);
	}
	public SortedArrayBackedSet(Set<? extends E> set, Comparator<? super E> comparator) {
		this.comparator = comparator;
		addAll(set);
	}
	
	@Override
	protected int getIndex(Object element) {
		try {
			@SuppressWarnings("unchecked")
			E kElement = (E) element;
			return Arrays.binarySearch(getElementsArray(), kElement, comparator);
		} catch (ClassCastException e) {
			return -1;
		}
	}
	@Override
	protected int getIndexForNewEntry(E element) {
		int index = Arrays.binarySearch(getElementsArray(), element, comparator);
		assert index < 0;
		return -(index + 1);
	}
	
	@Override
	public Comparator<? super E> comparator() {
		return comparator;
	}
	@Override
	public SortedSet<E> subSet(E fromElement, E toElement) {
		return new SubSet(fromElement, toElement);
	}
	@Override
	public SortedSet<E> headSet(E toElement) {
		return subSet(null, toElement);
	}
	@Override
	public SortedSet<E> tailSet(E fromElement) {
		return subSet(fromElement, null);
	}
	@Override
	public E first() {
		return elements[0];
	}
	@Override
	public E last() {
		return elements[elements.length - 1];
	}
	
	private final class SubSet extends AbstractSet<E> implements SortedSet<E> {
		private final E fromElement;
		private final E toElement;

		SubSet(E fromElement, E toElement) {
			this.fromElement = fromElement;
			this.toElement = toElement;
		}

		@Override
		public Comparator<? super E> comparator() {
			return SortedArrayBackedSet.this.comparator();
		}
		@SuppressWarnings("unchecked")
		private Comparator<? super E> nonNullComparator() {
			Comparator<? super E> parentComparator = SortedArrayBackedSet.this.comparator();
			if (parentComparator == null) {
				return (Comparator<? super E>)Ordering.natural();
			} else {
				return parentComparator;
			}
		}

		private void checkRange(E e) {
			checkArgument(fromElement == null || nonNullComparator().compare(fromElement, e) <= 0);
			checkArgument(toElement == null || nonNullComparator().compare(e, toElement) < 0);
		}
		@Override
		public SortedSet<E> subSet(E fromElement, E toElement) {
			checkRange(fromElement);
			checkRange(toElement);
			return new SubSet(fromElement, toElement);
		}


		@Override
		public SortedSet<E> headSet(E toElement) {
			checkRange(toElement);
			return new SubSet(this.fromElement, toElement);
		}

		@Override
		public SortedSet<E> tailSet(E fromElement) {
			checkRange(fromElement);
			return new SubSet(fromElement, this.toElement);
		}

		@Override
		public E first() {
			int index;
			if (fromElement == null) {
				index = 0;
			} else {
				index = SortedArrayBackedSet.this.getIndexForNewEntry(fromElement);
			}
			
			if (index >= 0 && SortedArrayBackedSet.this.size() > index) {
				return elements[index];
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public E last() {
			int index;
			if (toElement == null) {
				index = 0;
			} else {
				index = SortedArrayBackedSet.this.getIndexForNewEntry(toElement) - 1;
			}
			
			if (index >= 0 && SortedArrayBackedSet.this.size() > index) {
				return elements[index];
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public Iterator<E> iterator() {
			return new ArrayBackedSetIterator(fromElement == null ? 0 : getIndexForNewEntry(fromElement)) {
				@Override
				public boolean hasNext() {
					return super.hasNext() && (toElement == null || nonNullComparator().compare(elements[peekNextIndex()], toElement) < 0);
				}
			};
		}
		
		@Override
		public boolean add(E e) {
			checkRange(e);
			return SortedArrayBackedSet.this.add(e);
		}
		@SuppressWarnings("unchecked") // CCE caught, safe
		@Override
		public boolean remove(Object o) {
			try {
				checkRange((E) o);
			} catch (ClassCastException e) {
				return false;
			}
			return SortedArrayBackedSet.this.remove(o);
		}

		@Override
		public int size() {
			if (fromElement != null) {
				if (toElement != null) {
					return getIndexForNewEntry(toElement) - getIndexForNewEntry(fromElement);
				} else {
					return SortedArrayBackedSet.this.size() - getIndexForNewEntry(fromElement);
				}
			} else {
				return getIndexForNewEntry(toElement) - SortedArrayBackedSet.this.size();
			}
		}
		
	}
}
