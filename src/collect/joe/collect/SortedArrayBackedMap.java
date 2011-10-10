package joe.collect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

/**
 * Sorted implementation of {@link AbstractArrayBackedMap}. An instance may be
 * created with or without a {@link Comparator}. No specified comparator
 * indicates that the natural ordering of elements should be used. You'll see
 * {@link ClassCastException}s if the keys are not mutually comparable,
 * similarly to {@link TreeMap}.
 * 
 * @author Joe Kearney
 * @param <K>
 *            type of the keys stored in the map
 * @param <V>
 *            type of the values stored in the map
 */
public final class SortedArrayBackedMap<K, V> extends
		AbstractArrayBackedMap<K, V>
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	// array creation, raw Comparators etc
	public SortedArrayBackedMap(Map<? extends K, ? extends V> map,
			Comparator<? super K> comparator) {
		this.comparator = comparator;

		int size = map.size();
		this.keys = (K[]) new Object[size];
		this.values = (V[]) new Object[map.size()];

		if (map instanceof SortedMap<?, ?>) {
			SortedMap<K, V> sorted = (SortedMap<K, V>) map;
			int i = 0;
			for (Entry<? extends K, ? extends V> entry : sorted.entrySet()) {
				this.keys[i] = entry.getKey();
				this.values[i++] = entry.getValue();
			}
		} else {
			/*
			 * Sort the entries into a list, then write into K/V arrays.
			 * Alternative is to drop everything into a TreeMap and copy it back
			 * out.
			 * 
			 * This is really nasty. This is a consequence of * Guava Ordering's
			 * raw-typed Comparable * K need not extend Comparable at all if
			 * there's a comparator
			 * 
			 * Just let CCE be thrown here if necessary.
			 */
			final Ordering<Map.Entry<?, V>> ordering;
			if (comparator == null) {
				// order naturally by entry keys
				ordering = Ordering.natural().onResultOf(
						new Function<Entry<?, V>, Comparable>() {
							@Override
							public Comparable apply(Map.Entry<?, V> input) {
								return (Comparable) input.getKey();
							}
						});
			} else {
				// order according to the comparator
				ordering = Ordering.from((Comparator) comparator);
			}
			List<Map.Entry<?, V>> sorted = ordering
					.sortedCopy((Set<Map.Entry<?, V>>) (Set<?>) map.entrySet());
			
			int i = 0;
			for (Entry<?, ? extends V> entry : sorted) {
				this.keys[i] = (K) entry.getKey();
				this.values[i++] = entry.getValue();
			}
		}

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
