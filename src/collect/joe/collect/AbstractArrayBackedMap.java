package joe.collect;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public abstract class AbstractArrayBackedMap<K, V> extends AbstractMap<K, V> {
	protected K[] keys;
	protected V[] values;
	transient int modCount;

	public AbstractArrayBackedMap() {
		clear();
	}
	public AbstractArrayBackedMap(Map<? extends K, ? extends V> map) {
		putAll(map);
	}

	@Override
	public final V get(Object key) {
		int index = getIndexByKey(key);
		return index < 0 ? null : values[index];
	}
	@Override
	public final boolean containsKey(Object key) {
		return getIndexByKey(key) >= 0;
	}
	@Override
	public final boolean containsValue(Object value) {
		return getIndexByValue(value) >= 0;
	}
	@Override
	public final Set<K> keySet() {
		return new KeySet();
	}
	@Override
	public final Collection<V> values() {
		return new ValuesCollection();
	}
	@Override
	public final int size() {
		assert keys.length == values.length;
		return keys.length;
	}

	@Override
	public final Set<Entry<K, V>> entrySet() {
		return new AbstractSet<Map.Entry<K, V>>() {
			@Override
			public Iterator<Entry<K, V>> iterator() {
				return new EntryIterator();
			}

			@Override
			public void clear() {
				super.clear();
			}
			@Override
			public boolean contains(Object o) {
				if (o instanceof Entry) {
					Entry<?, ?> entry = (Entry<?, ?>) o;
					V value = get(entry.getKey());
					return value != null ? value.equals(((Entry<?, ?>) o).getValue()) : false;
				}
				return false;
			}

			@Override
			public int size() {
				return AbstractArrayBackedMap.this.size();
			}

		};
	}
	@Override
	public final V put(K key, V value) {
		if (key == null) {
			throw new NullPointerException("null key not permitted");
		}
		if (value == null) {
			throw new NullPointerException("null value not permitted");
		}

		int index = getIndexByKey(key);
		if (index < 0) {
			index = getIndexForNewEntry(key, value);
			putAtIndex(index, key, value);
			return null;
		} else {
			V oldValue = values[index];
			keys[index] = key;
			values[index] = value;
			return oldValue;
		}
	}

	@Override
	public final V remove(Object key) {
		int index = getIndexByKey(key);
		if (index < 0) {
			return null;
		} else {
			V oldValue = values[index];
			removeIndex(index);
			return oldValue;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	// we're only going to put objects of the right type in it
	public final void clear() {
		keys = (K[]) new Object[0];
		values = (V[]) new Object[0];
	}

	protected final K[] getKeysArray() {
		return keys;
	}
	protected final V[] getValuesArray() {
		return values;
	}

	/**
	 * Gets the array index of the specified key, or a negative value if it is not present in the map.
	 * 
	 * @param key key for which to search
	 * @return index of the key, or a negative value if it is not present in the map
	 */
	protected abstract int getIndexByKey(Object key);
	/**
	 * Gets the array index of the specified value, or a negative value if it is not present in the map.
	 * 
	 * @param value value for which to search
	 * @return index of the key, or a negative value if it is not present in the map
	 */
	protected abstract int getIndexByValue(Object value);
	/**
	 * Gets the index for insertion of a new key-value pair, where the key is
	 * not currently in the map. The index may depend on the key and/or the
	 * value. The returned value must be in the range {@code [0, size()]}. If it
	 * is equal to size of the map prior to this insertion, then the new entry
	 * will be placed at the tail of the array.
	 * 
	 * @param key the new key
	 * @param value the new value
	 * @return index at which to insert the new key-value pair
	 */
	protected abstract int getIndexForNewEntry(K key, V value);

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

	private final void putAtIndex(int index, K key, V value) {
		int oldSize = size();
		int newSize = oldSize + 1;

		assert index >= 0 && index <= oldSize;

		@SuppressWarnings("unchecked")
		K[] newKeys = (K[]) new Object[newSize];
		@SuppressWarnings("unchecked")
		V[] newValues = (V[]) new Object[newSize];

		System.arraycopy(keys, 0, newKeys, 0, index);
		System.arraycopy(values, 0, newValues, 0, index);
		if (index < oldSize) { // else inserting at end
			System.arraycopy(keys, index, newKeys, index + 1, oldSize - index);
			System.arraycopy(values, index, newValues, index + 1, oldSize - index);
		}

		newKeys[index] = key;
		newValues[index] = value;

		++modCount;
		this.keys = newKeys;
		this.values = newValues;
	}

	/**
	 * Removes the key and value at the specified index. This assumes that such
	 * a pair exists, and performs no range checking.
	 * 
	 * @param index index at which to remove the mapping
	 */
	final void removeIndex(int index) {
		int priorSize = size();
		int newSize = priorSize - 1;
		@SuppressWarnings("unchecked")
		K[] newKeys = (K[]) new Object[newSize];
		@SuppressWarnings("unchecked")
		V[] newValues = (V[]) new Object[newSize];

		System.arraycopy(keys, 0, newKeys, 0, index);
		System.arraycopy(keys, index + 1, newKeys, index, newSize - index);
		System.arraycopy(values, 0, newValues, 0, index);
		System.arraycopy(values, index + 1, newValues, index, newSize - index);

		++modCount;
		this.keys = newKeys;
		this.values = newValues;
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

	private final class KeySet extends AbstractSet<K> {
		KeySet() {}

		@Override
		public Iterator<K> iterator() {
			return new KeyIterator();
		}
		@Override
		public int size() {
			return AbstractArrayBackedMap.this.size();
		}
		@Override
		public boolean remove(Object o) {
			return AbstractArrayBackedMap.this.remove(o) != null;
		}
		@Override
		public boolean contains(Object o) {
			return AbstractArrayBackedMap.this.containsKey(o);
		}
		@Override
		public Object[] toArray() {
			return keys.clone();
		}
		@Override
		public <T> T[] toArray(T[] a) {
			K[] keys = AbstractArrayBackedMap.this.keys;
			int incomingLength = a.length;
			int outgoingLength = keys.length;
			if (incomingLength >= outgoingLength) {
				System.arraycopy(keys, 0, a, 0, keys.length);
				if (outgoingLength < incomingLength) {
					a[outgoingLength] = null;
				}
				return a;
			}
			@SuppressWarnings("unchecked")
			// type token is <? extends T>, so this is a fair warning
			T[] ret = (T[]) Arrays.copyOf(keys, keys.length, a.getClass());
			return ret;
		}
	}
	private final class ValuesCollection extends AbstractCollection<V> {
		ValuesCollection() {}

		@Override
		public Iterator<V> iterator() {
			return new ValuesIterator();
		}

		@Override
		public int size() {
			return AbstractArrayBackedMap.this.size();
		}
		@Override
		public boolean remove(Object o) {
			int index = getIndexByValue(o);
			if (index >= 0) {
				removeIndex(index);
				return true;
			} else {
				return false;
			}
		}
		@Override
		public boolean contains(Object o) {
			return AbstractArrayBackedMap.this.containsValue(o);
		}
		@Override
		public Object[] toArray() {
			return values.clone();
		}
		@Override
		public <T> T[] toArray(T[] a) {
			V[] values = AbstractArrayBackedMap.this.values;
			int incomingLength = a.length;
			int outgoingLength = values.length;
			if (incomingLength >= outgoingLength) {
				System.arraycopy(values, 0, a, 0, values.length);
				if (outgoingLength < incomingLength) {
					a[outgoingLength] = null;
				}
				return a;
			}
			@SuppressWarnings("unchecked")
			// type token is <? extends T>, so this is a fair warning
			T[] ret = (T[]) Arrays.copyOf(values, values.length, a.getClass());
			return ret;
		}
	}
	// TODO weakly-consistent version that does not throw CME?
	private class IndexIterator {
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

	private final class EntryIterator extends IndexIterator implements Iterator<Entry<K, V>> {
		private int expectedModCount = modCount;
		private int index = -1;

		EntryIterator() {}

		@Override
		public Entry<K, V> next() {
			int nextIndex = nextIndex();
			return new SimpleImmutableEntry<K, V>(keys[nextIndex], values[nextIndex]);
		}
	}
	private final class KeyIterator extends IndexIterator implements Iterator<K> {
		KeyIterator() {}
		@Override
		public K next() {
			return keys[nextIndex()];
		}
	}
	private final class ValuesIterator extends IndexIterator implements Iterator<V> {
		ValuesIterator() {}
		@Override
		public V next() {
			return values[nextIndex()];
		}
	}
}
