package joe.collect;

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
