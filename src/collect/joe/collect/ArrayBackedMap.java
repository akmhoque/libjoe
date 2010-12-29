package joe.collect;

/**
 * Simple insertion-ordered implementation of {@link AbstractArrayBackedMap}. All lookups are done by scanning through
 * the keys array, with the performance characteristics you'd expect. Consider using {@link SortedArrayBackedMap} if
 * the type of the keys has an ordering.
 * 
 * @author Joe Kearney
 * @param <K> type of the keys stored in the map
 * @param <V> type of the values stored in the map
 */
public final class ArrayBackedMap<K, V> extends AbstractArrayBackedMap<K, V> {
	@Override
	protected int getIndexByKey(Object key) {
		return findByScan(key, getKeysArray());
	}
	@Override
	protected int getIndexByValue(Object value) {
		return findByScan(value, getValuesArray());
	}
	@Override
	protected int getIndexForNewEntry(K key, V value) {
		return size();
	}
}
