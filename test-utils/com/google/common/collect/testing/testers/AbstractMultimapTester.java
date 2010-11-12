package com.google.common.collect.testing.testers;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.collect.testing.AbstractContainerTester;
import com.google.common.collect.testing.Helpers;
import com.google.common.collect.testing.MinimalCollection;

public class AbstractMultimapTester<K, V, M extends Multimap<K, V>> extends AbstractContainerTester<M, Map.Entry<K, V>> {
	protected M getMultimap() {
		return container;
	}
	@Override
	protected Collection<Entry<K, V>> actualContents() {
		return getMultimap().entries();
	}
	/**
	 * Wrapper for {@link Map#get(Object)} that forces the caller to pass in a key
	 * of the same type as the map. Besides being slightly shorter than code that
	 * uses {@link #getMap()}, it also ensures that callers don't pass an {@link Entry} by mistake.
	 */
	protected Collection<V> get(K key) {
		return getMultimap().get(key);
	}
	protected void resetMultimap() {
		resetContainer();
	}
	protected void expectMissingKeys(K ... elements) {
		for (K element : elements) {
			assertFalse("Should not contain key " + element, getMultimap().containsKey(element));
		}
	}
	
	protected Collection<V> createReferenceValuesCollection(Collection<V> values) {
		M testSubject = getMultimap();
		if (testSubject instanceof SetMultimap) {
			return Sets.newHashSet(values);
		} else if (testSubject instanceof ListMultimap) {
			return Lists.newArrayList(values);
		} else if (testSubject instanceof SortedSetMultimap) {
			@SuppressWarnings({"unchecked", "rawtypes"})
			SortedSet<V> newTreeSet = (SortedSet) Sets.newTreeSet();
			for (V v : values) {
				newTreeSet.add(v);
			}
			return newTreeSet;
		} else {
			throw new IllegalStateException("Unknown test subject type: not a Set-, SortedSet- or ListMultimap");
		}
	}
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected Multimap<K, V> createReferenceMultimap(Collection<? extends Map.Entry<? extends K, ? extends V>> entries) {
		Multimap<K, V> mm;
		M testSubject = getMultimap();
		
		if (testSubject instanceof SetMultimap) {
			mm = HashMultimap.create();
		} else if (testSubject instanceof ListMultimap) {
			mm = ArrayListMultimap.create();
		} else if (testSubject instanceof SortedSetMultimap) {
			mm = (Multimap) TreeMultimap.create();
		} else {
			throw new IllegalStateException("Unknown test subject type: not a Set-, SortedSet- or ListMultimap");
		}
		
		for (Entry<? extends K, ? extends V> entry : entries) {
			mm.put(entry.getKey(), entry.getValue());
		}
		
		return mm;
	}

	protected void expectMissingValues(V ... elements) {
		for (V element : elements) {
			assertFalse("Should not contain value " + element, getMultimap().containsValue(element));
		}
	}

	protected void assertElementsEqual(String string, Collection<V> valuesMatchingSample, Collection<V> collection) {
		if (valuesMatchingSample.equals(collection)
				|| (valuesMatchingSample.size() == collection.size() && valuesMatchingSample.containsAll(collection) && collection.containsAll(valuesMatchingSample))) {
			// pass
		} else {
			fail(string + ". Expected " + valuesMatchingSample + ", got " + collection);
		}
	}

	/**
	 * @return an array of the proper size with {@code null} as the key of the
	 *         middle element.
	 */
	protected Map.Entry<K, V>[] createArrayWithNullKey() {
		Map.Entry<K, V>[] array = createSamplesArray();
		final int nullKeyLocation = getNullLocation();
		final Map.Entry<K, V> oldEntry = array[nullKeyLocation];
		array[nullKeyLocation] = entry(null, oldEntry.getValue());
		return array;
	}

	protected Collection<V> getValueCollectionForNullKey() {
		// TODO scan samples?
		return ImmutableList.of(getEntryNullReplaces().getValue());
	}
	protected V getValueForNullKey() {
		return getEntryNullReplaces().getValue();
	}

	protected K getKeyForNullValue() {
		return getEntryNullReplaces().getKey();
	}

	private Entry<K, V> getEntryNullReplaces() {
		Iterator<Entry<K, V>> entries = getSampleElements().iterator();
		for (int i = 0; i < getNullLocation(); i++) {
			entries.next();
		}
		return entries.next();
	}

	/**
	 * @return an array of the proper size with {@code null} as the value of the
	 *         middle element.
	 */
	protected Map.Entry<K, V>[] createArrayWithNullValue() {
		Map.Entry<K, V>[] array = createSamplesArray();
		final int nullValueLocation = getNullLocation();
		final Map.Entry<K, V> oldEntry = array[nullValueLocation];
		array[nullValueLocation] = entry(oldEntry.getKey(), null);
		return array;
	}

	protected void initMapWithNullKey() {
		resetMultimap(createArrayWithNullKey());
	}

	protected void initMapWithNullValue() {
		resetMultimap(createArrayWithNullValue());
	}

	/**
	 * Equivalent to {@link #expectMissingKeys(Object[]) expectMissingKeys} {@code (null)} except that the call to
	 * {@code contains(null)} is permitted to throw a {@code NullPointerException}.
	 * 
	 * @param message message to use upon assertion failure
	 */
	protected void expectNullKeyMissingWhenNullKeysUnsupported(String message) {
		try {
			assertFalse(message, getMultimap().containsKey(null));
		} catch (NullPointerException tolerated) {
			// Tolerated
		}
	}

	/**
	 * Equivalent to {@link #expectMissingValues(Object[]) expectMissingValues} {@code (null)} except that the call to
	 * {@code contains(null)} is permitted to throw a {@code NullPointerException}.
	 * 
	 * @param message message to use upon assertion failure
	 */
	protected void expectNullValueMissingWhenNullValuesUnsupported(String message) {
		try {
			assertFalse(message, getMultimap().containsValue(null));
		} catch (NullPointerException tolerated) {
			// Tolerated
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected MinimalCollection<Map.Entry<K, V>> createDisjointCollection() {
		return MinimalCollection.of(samples.e3, samples.e4);
	}

	protected int getNumEntries() {
		return getNumElements();
	}

	protected Collection<Map.Entry<K, V>> getSampleEntries(int howMany) {
		return getSampleElements(howMany);
	}

	protected Collection<Map.Entry<K, V>> getSampleEntries() {
		return getSampleElements();
	}

	protected Collection<V> valuesMatchingSample(Map.Entry<K, V> thisSample) {
		Collection<Entry<K, V>> sampleElements = getSampleElements();

		Builder<V> builder = ImmutableList.builder();
		for (Entry<K, V> entry : sampleElements) {
			if (Objects.equal(entry.getKey(), thisSample.getKey())) {
				builder.add(entry.getValue());
			}
		}
		return builder.build();
	}

	@Override
	protected void expectMissing(Entry<K, V> ... entries) {
		for (Entry<K, V> entry : entries) {
			assertFalse("Should not contain entry " + entry, actualContents().contains(entry));
			assertFalse("Should not return a mapping for entry " + entry,
					getMultimap().get(entry.getKey()).contains(entry.getValue()));
		}
	}

	// This one-liner saves us from some ugly casts
	protected Entry<K, V> entry(K key, V value) {
		return Helpers.mapEntry(key, value);
	}

	// removing map assertion which assumes unique keys across entries
	// @Override
	// protected void expectContents(Collection<Entry<K, V>> expected) {
	// // TODO: move this to invariant checks once the appropriate hook exists?
	// super.expectContents(expected);
	// for (Entry<K, V> entry : expected) {
	// assertEquals("Wrong value for key " + entry.getKey(), entry.getValue(), getMultimap().get(entry.getKey()));
	// }
	// }

	protected final void expectReplacement(Entry<K, V> newEntry) {
		List<Entry<K, V>> expected = Helpers.copyToList(getSampleElements());
		replaceValue(expected, newEntry);
		expectContents(expected);
	}

	private void replaceValue(List<Entry<K, V>> expected, Entry<K, V> newEntry) {
		for (ListIterator<Entry<K, V>> i = expected.listIterator(); i.hasNext();) {
			if (Objects.equal(i.next().getKey(), newEntry.getKey())) {
				i.set(newEntry);
				return;
			}
		}

		throw new IllegalArgumentException(String.format("key %s not found in entries %s", newEntry.getKey(), expected));
	}

	protected void resetMultimap(Entry<K, V>[] entries) {
		resetContainer(getSubjectGenerator().create((Object[]) entries));
	}
	protected static void assertEmpty(Collection<?> collection) {
		assertTrue(collection.isEmpty());
	}
	protected static void assertEmpty(String message, Collection<?> collection) {
		assertTrue(message + ". Got: " + collection, collection.isEmpty());
	}
}
