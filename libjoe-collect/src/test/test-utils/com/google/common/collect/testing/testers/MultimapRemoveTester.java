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

import static com.google.common.collect.testing.features.CollectionSize.*;
import static com.google.common.collect.testing.features.MapFeature.*;

import java.util.Map.Entry;

import com.google.common.collect.Multimap;
import com.google.common.collect.testing.WrongType;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;

/**
 * A generic JUnit test which tests {@code remove} operations on a map.
 */
@SuppressWarnings("unchecked")
// too many "unchecked generic array creations"
public class MultimapRemoveTester<K, V> extends AbstractMultimapTester<K, V, Multimap<K, V>> {
	@MapFeature.Require(SUPPORTS_REMOVE)
	@CollectionSize.Require(absent = ZERO)
	public void testRemove_present() {
		int initialSize = getMultimap().size();
		assertTrue("remove(present, present) should return true", remove(samples.e0));
		assertEquals("remove(present, present) should decrease a map's size by one.", initialSize - 1,
				getMultimap().size());
		expectMissing(samples.e0);
	}

	@MapFeature.Require(SUPPORTS_REMOVE)
	public void testRemove_notPresent() {
		assertFalse("remove(notPresent, notPresent) should return false", remove(samples.e3));
		expectUnchanged();
	}

	@MapFeature.Require({SUPPORTS_REMOVE, ALLOWS_NULL_KEYS})
	@CollectionSize.Require(absent = ZERO)
	public void testRemove_nullPresent() {
		initMapWithNullKey();

		int initialSize = getMultimap().size();
		assertTrue("remove(null, present) should return true", getMultimap().remove(null, getValueForNullKey()));
		assertEquals("remove(present) should decrease a map's size by one.", initialSize - 1, getMultimap().size());
		expectMissing(entry(null, getValueForNullKey()));
	}

	@MapFeature.Require(absent = SUPPORTS_REMOVE)
	@CollectionSize.Require(absent = ZERO)
	public void testRemove_unsupported() {
		try {
			remove(samples.e0);
			fail("remove(present, present) should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException expected) {}
		expectUnchanged();
		assertTrue("remove(present) should not remove the element",
				getMultimap().containsEntry(samples.e0.getKey(), samples.e0.getValue()));
	}

	@MapFeature.Require(absent = SUPPORTS_REMOVE)
	public void testRemove_unsupportedNotPresent() {
		try {
			assertFalse("remove(notPresent, notPresent) should return false or throw "
					+ "UnsupportedOperationException", remove(samples.e3));
		} catch (UnsupportedOperationException tolerated) {}
		expectUnchanged();
		expectMissing(samples.e3);
	}

	@MapFeature.Require(value = SUPPORTS_REMOVE, absent = ALLOWS_NULL_KEYS)
	public void testRemove_nullNotSupported() {
		try {
			assertFalse("remove(null, notPresent) should return false or throw " + "NullPointerException",
					getMultimap().remove(null, samples.e3.getValue()));
		} catch (NullPointerException tolerated) {}
		expectUnchanged();
	}

	@MapFeature.Require({SUPPORTS_REMOVE, ALLOWS_NULL_KEYS})
	public void testRemove_nullSupportedMissing() {
		assertFalse("remove(null, notPresent) should return null", getMultimap().remove(null, samples.e3.getValue()));
		expectUnchanged();
	}

	@MapFeature.Require(SUPPORTS_REMOVE)
	public void testRemove_wrongType() {
		try {
			assertFalse(getMultimap().remove(WrongType.VALUE, samples.e3.getValue()));
		} catch (ClassCastException tolerated) {}
		expectUnchanged();
	}

	private boolean remove(Entry<K, V> entry) {
		return getMultimap().remove(entry.getKey(), entry.getValue());
	}
}
