package joe.collect;

import static com.google.common.collect.Iterators.transform;
import static com.google.common.collect.Iterators.unmodifiableIterator;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * List view over multiple lists, indexing transparently.
 * 
 * @author Joe Kearney
 */
public final class ConcatenatedList<E> extends AbstractList<E> {
	// TODO doesn't quite work yet.

	/**
	 * Returns a view over the lists, indexing through them transparently.
	 * <p>
	 * The returned list does not implement {@link RandomAccess} since lookups do not take constant time; for {@code m} lists of any length,
	 * lookup takes {@code O(m)} time.
	 * <p>
	 * The returned list is unmodifiable; modification operations throw {@link UnsupportedOperationException}.
	 * 
	 * @param lists lists to concatenate
	 * @return a concatenated view over the lists
	 */
	public static <T> List<T> concat(final Iterable<? extends List<? extends T>> lists) {
		return new ConcatenatedList<T>(lists);
	}

	private static <E> Function<Iterable<? extends E>, Iterator<? extends E>> toIterator() {
		return new Function<Iterable<? extends E>, Iterator<? extends E>>() {
			@Override
			public Iterator<? extends E> apply(Iterable<? extends E> input) {
				return input.iterator();
			};
		};
	}

	private static abstract class ListBasedListIterator<T> implements ListIterator<T> {
		/**
		 * Index of element to be returned by subsequent call to next.
		 */
		private int cursor;

		public ListBasedListIterator(int index) {
			cursor = index;
			iter = createIterator(cursor);
		}

		protected abstract Iterator<T> createIterator();
		protected Iterator<T> createIterator(int index) {
			if (index < 0) {
				throw new IndexOutOfBoundsException("Invalid list iterator index: " + index);
			}

			Iterator<T> iterator = createIterator();
			int skipped = Iterators.skip(iterator, index);
			if (skipped != index) {
				throw new IndexOutOfBoundsException("Invalid list iterator index: " + index);
			}
			return iterator;
		}
		protected abstract T get(int index);

		private Iterator<T> iter;

		@Override
		public boolean hasNext() {
			return getIterator().hasNext();
		}

		private Iterator<T> getIterator() {
			if (iter != null) {
				return iter;
			}
			try {
				iter = createIterator(cursor);
			} catch (IndexOutOfBoundsException e) {
				throw newNoSuchElementException(e);
			}
			return iter;
		}

		private static NoSuchElementException newNoSuchElementException(IndexOutOfBoundsException e) {
			NoSuchElementException nsee = new NoSuchElementException();
			nsee.initCause(e);
			return nsee;
		}
		@Override
		public T next() {
			T ret;
			try {
				ret = getIterator().next();
			} catch (IndexOutOfBoundsException e) {
				throw newNoSuchElementException(e);
			}
			++cursor;
			return ret;
		}

		@Override
		public boolean hasPrevious() {
			return cursor > 0;
		}

		@Override
		public T previous() {
			int index = cursor - 1;
			T ret;
			try {
				ret = get(index);
			} catch (IndexOutOfBoundsException e) {
				throw newNoSuchElementException(e);
			}
			cursor = index;
			iter = null;
			return ret;
		}

		@Override
		public int nextIndex() {
			return cursor;
		}

		@Override
		public int previousIndex() {
			return cursor - 1;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(T e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(T e) {
			throw new UnsupportedOperationException();
		}
	}

	private final Iterable<? extends List<? extends E>> lists;
	private ConcatenatedList(Iterable<? extends List<? extends E>> lists) {
		this.lists = lists;
	}
	@Override
	public E get(int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Invalid list index: " + index);
		}
		// don't check against size now, that's an extra iteration through the lists

		int currentIndex = 0;
		int nextIndex = 0;

		Iterator<? extends List<? extends E>> iter = lists.iterator();
		for (;;) {
			if (!iter.hasNext()) {
				throw new IndexOutOfBoundsException("Invalid list index: " + index);
			}
			List<? extends E> list = iter.next();
			nextIndex += list.size();
			if (nextIndex > index) {
				return list.get(index - currentIndex);
			}
			currentIndex = nextIndex;
		}
	}
	@Override
	public Iterator<E> iterator() {
		return iterator(0);
	}
	Iterator<E> iterator(int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Invalid list index: " + index);
		} else if (index == 0) {
			return unmodifiableIterator(Iterables.concat(lists).iterator());
		}
		// don't check against size now, that's an extra iteration through the lists

		int currentIndex = 0;
		int nextIndex = 0;

		Iterator<? extends List<? extends E>> iter = lists.iterator();
		for (;;) {
			if (!iter.hasNext()) {
				if (currentIndex == index) {
					// placing an iterator beyond all the elements
					return Iterators.emptyIterator();
				} else {
					throw new IndexOutOfBoundsException("Invalid list index: " + index);
				}
			}
			List<? extends E> list = iter.next();

			int size = list.size();
			if (size == 0) { // don't want to consider empty component lists
				continue;
			}

			nextIndex += size;
			if (nextIndex > index) {
				// get iterator for this last at the right index and concat the rest of the lists' iterators
				Iterator<? extends E> currentIterator = list.listIterator(index);
				Iterator<? extends E> tailIterator = Iterators.concat(transform(iter, ConcatenatedList.<E> toIterator()));
				Iterator<E> tail = Iterators.concat(currentIterator, tailIterator);
				int indexInCurrentIterator = index - currentIndex;
				int skipped = Iterators.skip(tail, indexInCurrentIterator);
				if (skipped != indexInCurrentIterator) {
					throw new AssertionError();
				}
				return tail;
			}
			currentIndex = nextIndex;
		}
	}
	@Override
	public ListIterator<E> listIterator(int index) {
		return new ListBasedListIterator<E>(index) {
			@Override
			protected Iterator<E> createIterator() {
				return ConcatenatedList.this.iterator();
			}
			@Override
			protected Iterator<E> createIterator(int index) {
				return ConcatenatedList.this.iterator(index);
			}

			@Override
			protected E get(int index) {
				return ConcatenatedList.this.get(index);
			}
		};
	}
	@Override
	public int size() {
		long size = 0;
		for (List<? extends E> list : lists) {
			size += list.size();
			if (size > Integer.MAX_VALUE) {
				return Integer.MAX_VALUE;
			}
		}
		return (int) size; // definitely in range
	}
}
