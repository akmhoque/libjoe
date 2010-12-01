package joe.collect;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public final class SortedArrayBackedMap<K, V> extends AbstractArrayBackedMap<K, V> {
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
}
