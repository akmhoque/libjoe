package com.google.common.collect.testing.testers;

import static com.google.common.collect.testing.features.CollectionSize.*;
import static com.google.common.collect.testing.features.MapFeature.*;

import com.google.common.collect.Multimap;
import com.google.common.collect.testing.WrongType;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;

public class MultimapGetTester<K, V> extends AbstractMultimapTester<K, V, Multimap<K, V>> {
	@CollectionSize.Require(absent = ZERO)
	public void testGet_yes() {
		assertElementsEqual("get(present) should return the associated value", valuesMatchingSample(samples.e0),
				get(samples.e0.getKey()));
	}

	// otherwise e3 = one=>March, and returns January
	public void testGet_no() {
		assertEmpty("get(notPresent) should return an empty collection", get(samples.e3.getKey()));
	}

	@MapFeature.Require(ALLOWS_NULL_KEYS)
	public void testGet_nullNotContainedButSupported() {
		assertEmpty("get(null) should return an empty collection", get(null));
	}

	@MapFeature.Require(absent = ALLOWS_NULL_KEYS)
	public void testGet_nullNotContainedAndUnsupported() {
		try {
			assertEmpty("get(null) should return an empty collection", get(null));
		} catch (NullPointerException tolerated) {}
	}

	@MapFeature.Require(ALLOWS_NULL_KEYS)
	@CollectionSize.Require(absent = ZERO)
	public void testGet_nonNullWhenNullContained() {
		initMapWithNullKey();
		assertEmpty("get(notPresent) should return an empty collection", get(samples.e3.getKey()));
	}

	@MapFeature.Require(ALLOWS_NULL_KEYS)
	@CollectionSize.Require(absent = ZERO)
	public void testGet_nullContained() {
		initMapWithNullKey();
		assertElementsEqual("get(null) should return the associated value", getValueCollectionForNullKey(), get(null));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void testGet_wrongType() {
		try {
			assertEmpty("get(wrongType) should return an empty collection or throw",
					((Multimap) getMultimap()).get(WrongType.VALUE));
		} catch (ClassCastException tolerated) {}
	}
}
