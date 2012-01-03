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
import com.google.common.collect.testing.Helpers;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;

/**
 * Tests {@link Multimap#equals}.
 */
public class MultimapEqualsTester<K, V> extends AbstractMultimapTester<K, V, Multimap<K, V>> {
	public void testEquals_otherMapWithSameEntries() throws Exception {
		assertTrue("A Multimap should equal any other Multimap containing the same entries.",
				getMultimap().equals(createReferenceMultimap(getSampleEntries())));
	}

	@CollectionSize.Require(absent = CollectionSize.ZERO)
	public void testEquals_otherMapWithDifferentEntries() throws Exception {
		Multimap<K, V> other = createReferenceMultimap(getSampleEntries(getNumEntries() - 1));
		Entry<K, V> e3 = getSubjectGenerator().samples().e3;
		other.put(e3.getKey(), e3.getValue());
		assertFalse("A Multimap should not equal another Multimap containing different entries.",
				getMultimap().equals(other));
	}

	@CollectionSize.Require(absent = CollectionSize.ZERO)
	@MapFeature.Require(ALLOWS_NULL_KEYS)
	public void testEquals_containingNullKey() {
		Collection<Map.Entry<K, V>> entries = getSampleEntries(getNumEntries() - 1);
		entries.add(entry(null, samples.e3.getValue()));

		resetContainer(getSubjectGenerator().create(entries.toArray()));
		assertTrue("A Multimap should equal any other Multimap containing the same entries,"
				+ " even if some keys are null.", getMultimap().equals(createReferenceMultimap(entries)));
	}

	@CollectionSize.Require(absent = CollectionSize.ZERO)
	public void testEquals_otherContainsNullKey() {
		Collection<Map.Entry<K, V>> entries = getSampleEntries(getNumEntries() - 1);
		entries.add(entry(null, samples.e3.getValue()));
		Multimap<K, V> other = createReferenceMultimap(entries);

		assertFalse("Two Multimaps should not be equal if exactly one of them contains a null " + "key.",
				getMultimap().equals(other));
	}

	@CollectionSize.Require(absent = CollectionSize.ZERO)
	@MapFeature.Require(ALLOWS_NULL_VALUES)
	public void testEquals_containingNullValue() {
		Collection<Map.Entry<K, V>> entries = getSampleEntries(getNumEntries() - 1);
		entries.add(entry(samples.e3.getKey(), null));

		resetContainer(getSubjectGenerator().create(entries.toArray()));
		assertTrue("A Multimap should equal any other Multimap containing the same entries,"
				+ " even if some values are null.", getMultimap().equals(createReferenceMultimap(entries)));
	}

	@CollectionSize.Require(absent = CollectionSize.ZERO)
	public void testEquals_otherContainsNullValue() {
		Collection<Map.Entry<K, V>> entries = getSampleEntries(getNumEntries() - 1);
		entries.add(entry(samples.e3.getKey(), null));
		Multimap<K,V> other = createReferenceMultimap(entries);

		assertFalse("Two Multimaps should not be equal if exactly one of them contains a null " + "value.",
				getMultimap().equals(other));
	}

	@CollectionSize.Require(absent = CollectionSize.ZERO)
	public void testEquals_smallerMap() throws Exception {
		Collection<Map.Entry<K, V>> fewerEntries = getSampleEntries(getNumEntries() - 1);
		assertFalse("Maps of different sizes should not be equal.", getMultimap().equals(createReferenceMultimap(fewerEntries)));
	}

	public void testEquals_largerMap() {
		Collection<Map.Entry<K, V>> moreEntries = getSampleEntries(getNumEntries() + 1);
		assertFalse("Maps of different sizes should not be equal.", getMultimap().equals(createReferenceMultimap(moreEntries)));
	}

	public void testEquals_list() {
		assertFalse("A List should never equal a Multimap.",
				getMultimap().equals(Helpers.copyToList(getMultimap().entries())));
	}
}
