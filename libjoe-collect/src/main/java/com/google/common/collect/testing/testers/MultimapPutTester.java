/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect.testing.testers;

import static com.google.common.collect.testing.features.CollectionSize.ZERO;
import static com.google.common.collect.testing.features.MapFeature.ALLOWS_NULL_KEYS;
import static com.google.common.collect.testing.features.MapFeature.ALLOWS_NULL_VALUES;
import static com.google.common.collect.testing.features.MapFeature.SUPPORTS_PUT;
import static com.google.common.collect.testing.features.MultimapFeature.ALLOWS_DUPLICATE_VALUES;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.collect.Multimap;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import com.google.common.collect.testing.features.MultimapFeature;

/**
 * A generic JUnit test which tests {@code put} operations on a multimap.
 */
@SuppressWarnings("unchecked")
// too many "unchecked generic array creations"
public class MultimapPutTester<K, V> extends AbstractMultimapTester<K, V, Multimap<K, V>> {
	private Entry<K, V> nullKeyEntry;
	private Entry<K, V> nullValueEntry;
	private Entry<K, V> nullKeyValueEntry;
	private Entry<K, V> presentKeyNullValueEntry;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		nullKeyEntry = entry(null, samples.e3.getValue());
		nullValueEntry = entry(samples.e3.getKey(), null);
		nullKeyValueEntry = entry(null, null);
		presentKeyNullValueEntry = entry(samples.e0.getKey(), null);
	}

	@MapFeature.Require(SUPPORTS_PUT)
	public void testPut_supportedNotPresent() {
		assertTrue("put(notPresent, value) should return true", put(samples.e3));
		expectAdded(samples.e3);
	}

	@MapFeature.Require(absent = SUPPORTS_PUT)
	public void testPut_unsupportedNotPresent() {
		try {
			put(samples.e3);
			fail("put(notPresent, value) should throw");
		} catch (UnsupportedOperationException expected) {}
		expectUnchanged();
		expectMissing(samples.e3);
	}

	@MapFeature.Require(absent = SUPPORTS_PUT)
	@CollectionSize.Require(absent = ZERO)
	public void testPut_unsupportedPresentExistingValue() {
		try {
			assertEquals("put(present, existingValue) should return present or throw", samples.e0.getValue(),
					put(samples.e0));
		} catch (UnsupportedOperationException tolerated) {}
		expectUnchanged();
	}

	@MapFeature.Require(absent = SUPPORTS_PUT)
	@CollectionSize.Require(absent = ZERO)
	public void testPut_unsupportedPresentDifferentValue() {
		try {
			getMultimap().put(samples.e0.getKey(), samples.e3.getValue());
			fail("put(present, differentValue) should throw");
		} catch (UnsupportedOperationException expected) {}
		expectUnchanged();
	}

	@MapFeature.Require({SUPPORTS_PUT, ALLOWS_NULL_KEYS})
	public void testPut_nullKeySupportedNotPresent() {
		assertTrue("put(null, value) should return true", put(nullKeyEntry));
		expectAdded(nullKeyEntry);
	}

	@MultimapFeature.Require(ALLOWS_DUPLICATE_VALUES)
	@MapFeature.Require({SUPPORTS_PUT, ALLOWS_NULL_KEYS})
	@CollectionSize.Require(absent = ZERO)
	public void testPut_nullKeySupportedPresent() {
		Entry<K, V> newEntry = entry(null, samples.e3.getValue());
		initMapWithNullKey();
		assertTrue("put(present, value) should return true", put(newEntry));

		Entry<K, V>[] expected = ObjectArrays.concat(createArrayWithNullKey(), newEntry);
		expectContents(expected);
	}

	@MapFeature.Require(value = SUPPORTS_PUT, absent = ALLOWS_NULL_KEYS)
	public void testPut_nullKeyUnsupported() {
		try {
			put(nullKeyEntry);
			fail("put(null, value) should throw");
		} catch (NullPointerException expected) {}
		expectUnchanged();
		expectNullKeyMissingWhenNullKeysUnsupported("Should not contain null key after unsupported put(null, value)");
	}

	@MapFeature.Require({SUPPORTS_PUT, ALLOWS_NULL_VALUES})
	public void testPut_nullValueSupported() {
		assertTrue("put(key, null) should return true", put(nullValueEntry));
		expectAdded(nullValueEntry);
	}

	@MapFeature.Require(value = SUPPORTS_PUT, absent = ALLOWS_NULL_VALUES)
	public void testPut_nullValueUnsupported() {
		try {
			put(nullValueEntry);
			fail("put(key, null) should throw");
		} catch (NullPointerException expected) {}
		expectUnchanged();
		expectNullValueMissingWhenNullValuesUnsupported("Should not contain null value after unsupported put(key, null)");
	}

	@MultimapFeature.Require(absent = ALLOWS_DUPLICATE_VALUES)
	@MapFeature.Require({SUPPORTS_PUT, ALLOWS_NULL_VALUES})
	@CollectionSize.Require(absent = ZERO)
	public void testPut_addNullValueToExistingKeySupported_dupsUnsupported() {
		assertTrue("put(present, nullNotPresent) should return false", put(presentKeyNullValueEntry));
		expectAdded(presentKeyNullValueEntry);
	}
	@MultimapFeature.Require(absent = ALLOWS_DUPLICATE_VALUES)
	@MapFeature.Require({SUPPORTS_PUT, ALLOWS_NULL_VALUES})
	@CollectionSize.Require(absent = ZERO)
	public void testPut_addPresentValueToPresentKeySupported_dupsUnsupported() {
		assertFalse("put(present, present) should return false", put(samples.e0));
		expectUnchanged();
	}
	@MultimapFeature.Require(ALLOWS_DUPLICATE_VALUES)
	@MapFeature.Require({SUPPORTS_PUT, ALLOWS_NULL_VALUES})
	@CollectionSize.Require(absent = ZERO)
	public void testPut_addPresentValueToPresentKeySupported_dupsSupported() {
		assertTrue("put(present, present) should return true", put(samples.e0));
		expectAdded(samples.e0);
	}

	@MapFeature.Require(value = SUPPORTS_PUT, absent = ALLOWS_NULL_VALUES)
	@CollectionSize.Require(absent = ZERO)
	public void testPut_replaceWithNullValueUnsupported() {
		try {
			put(presentKeyNullValueEntry);
			fail("put(present, null) should throw");
		} catch (NullPointerException expected) {}
		expectUnchanged();
		expectNullValueMissingWhenNullValuesUnsupported("Should not contain null after unsupported put(present, null)");
	}

	@MultimapFeature.Require(absent = ALLOWS_DUPLICATE_VALUES)
	@MapFeature.Require({SUPPORTS_PUT, ALLOWS_NULL_VALUES})
	@CollectionSize.Require(absent = ZERO)
	public void testPut_addAnotherNullValueWithNullSupported_dupsUnsupported() {
		initMapWithNullValue();
		assertFalse("put(present, nullPresent) should return true", getMultimap().put(getKeyForNullValue(), null));
		expectContents(createArrayWithNullValue());
	}
	@MultimapFeature.Require(ALLOWS_DUPLICATE_VALUES)
	@MapFeature.Require({SUPPORTS_PUT, ALLOWS_NULL_VALUES})
	@CollectionSize.Require(absent = ZERO)
	public void testPut_addAnotherNullValueWithNullSupported_dupsSupported() {
		initMapWithNullValue();
		assertTrue("put(present, nullPresent) should return true", getMultimap().put(getKeyForNullValue(), null));

		Entry<K, V>[] expected = ObjectArrays.concat(createArrayWithNullValue(), entry(getKeyForNullValue(), null));
		expectContents(expected);
	}

	@MapFeature.Require({SUPPORTS_PUT, ALLOWS_NULL_VALUES})
	@CollectionSize.Require(absent = ZERO)
	public void testPut_replaceNullValueWithNonNullSupported() {
		Entry<K, V> newEntry = entry(getKeyForNullValue(), samples.e3.getValue());
		initMapWithNullValue();
		assertTrue("put(present, value) should return true", put(newEntry));

		Entry<K, V>[] expected = ObjectArrays.concat(createArrayWithNullValue(), newEntry);
		expectContents(expected);
	}

	@MapFeature.Require({SUPPORTS_PUT, ALLOWS_NULL_KEYS, ALLOWS_NULL_VALUES})
	public void testPut_nullKeyAndValueSupported() {
		assertTrue("put(null, null) should return true", put(nullKeyValueEntry));
		expectAdded(nullKeyValueEntry);
	}

	private boolean put(Map.Entry<K, V> entry) {
		return getMultimap().put(entry.getKey(), entry.getValue());
	}
}
