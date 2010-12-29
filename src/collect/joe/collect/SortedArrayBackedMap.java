package joe.collect;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Sorted implementation of {@link AbstractArrayBackedMap}. An instance may be created with or without a
 * {@link Comparator}. No specified comparator indicates that the natural ordering of elements should be used. You'll
 * see {@link ClassCastException}s if the keys are not mutually comparable, similarly to {@link TreeMap}.
 * 
 * @author Joe Kearney
 * @param <K> type of the keys stored in the map
 * @param <V> type of the values stored in the map
 */
public final class SortedArrayBackedMap<K, V> extends AbstractArrayBackedMap<K, V>
// TODO implements SortedMap<K, V>
{
	private final Comparator<? super K> comparator;

	public SortedArrayBackedMap() {
		this.comparator = null;
	}
	public SortedArrayBackedMap(Comparator<? super K> comparator) {
		this.comparator = comparator;
	}
	public SortedArrayBackedMap(Map<? extends K, ? extends V> map) {
		this(map, null);
	}
	public SortedArrayBackedMap(Map<? extends K, ? extends V> map, Comparator<? super K> comparator) {
		this.comparator = comparator;
		putAll(map);
	}

	@Override
	protected int getIndexByKey(Object key) {
		try {
			@SuppressWarnings("unchecked")
			K kkey = (K) key;
			return Arrays.binarySearch(getKeysArray(), kkey, comparator);
		} catch (ClassCastException e) {
			return -1;
		}
	}
	@Override
	protected int getIndexByValue(Object value) {
		return findByScan(value, getValuesArray());
	}
	@Override
	protected int getIndexForNewEntry(K key, V value) {
		int index = Arrays.binarySearch(getKeysArray(), key, comparator);
		assert index < 0;
		return -(index + 1);
	}

	// @Override
	// public Comparator<? super K> comparator() {
	// return comparator;
	// }
	// @Override
	// public K firstKey() {
	// K[] keysArray = getKeysArray();
	// if (keysArray.length == 0) {
	// throw new NoSuchElementException();
	// }
	// return keysArray[0];
	// }
	// @Override
	// public K lastKey() {
	// K[] keysArray = getKeysArray();
	// int length = keysArray.length;
	// if (length == 0) {
	// throw new NoSuchElementException();
	// }
	// return keysArray[length - 1];
	// }
}
