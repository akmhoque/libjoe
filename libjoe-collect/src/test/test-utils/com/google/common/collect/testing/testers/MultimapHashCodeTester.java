/*
 * Copyright (C) 2008 Google Inc.
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

import static com.google.common.collect.testing.features.MapFeature.*;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Multimap;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;

/**
 * Tests {@link Multimap#hashCode}.
 */
public class MultimapHashCodeTester<K, V> extends AbstractMultimapTester<K, V, Multimap<K, V>> {
	public void testHashCode() {
		int expectedHashCode = 0;
		Multimap<K, V> referenceMultimap = createReferenceMultimap(getSampleEntries());
		for (Entry<K, Collection<V>> mmEntry : referenceMultimap.asMap().entrySet()) {
			expectedHashCode += hash(mmEntry.getKey(), createReferenceValuesCollection(mmEntry.getValue()));
		}
		assertEquals("A Multimap's hashCode() should be the sum of those of its entries.", expectedHashCode,
				getMultimap().hashCode());
	}

	@CollectionSize.Require(absent = CollectionSize.ZERO)
	@MapFeature.Require(ALLOWS_NULL_KEYS)
	public void testHashCode_containingNullKey() {
		Map.Entry<K, V> entryWithNull = entry(null, samples.e3.getValue());
		runEntryWithNullTest(entryWithNull);
	}

	@CollectionSize.Require(absent = CollectionSize.ZERO)
	@MapFeature.Require(ALLOWS_NULL_VALUES)
	public void testHashCode_containingNullValue() {
		Map.Entry<K, V> entryWithNull = entry(samples.e3.getKey(), null);
		runEntryWithNullTest(entryWithNull);
	}

	private void runEntryWithNullTest(Map.Entry<K, V> entryWithNull) {
		Collection<Map.Entry<K, V>> entries = getSampleEntries(getNumEntries() - 1);

		entries.add(entryWithNull);

		int expectedHashCode = 0;
		Multimap<K, V> referenceMultimap = createReferenceMultimap(entries);
		for (Entry<K, Collection<V>> mmEntry : referenceMultimap.asMap().entrySet()) {
			expectedHashCode += hash(mmEntry.getKey(), createReferenceValuesCollection(mmEntry.getValue()));
		}

		resetContainer(getSubjectGenerator().create(entries.toArray()));
		assertEquals("A Multimap's hashCode() should be the sum of those of its asMap entries (where "
				+ "a null element in an entry counts as having a hash of zero).", expectedHashCode,
				getMultimap().hashCode());
	}

	private static int hash(Object key, Object value) {
		return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
	}
}
