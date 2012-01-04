package com.google.common.collect.testing;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Multimap;
import com.google.common.collect.testing.Helpers;
import com.google.common.collect.testing.SampleElements;

public abstract class TestStringMultimapGenerator<C extends Multimap<String, String>> implements
		TestMultimapGenerator<String, String, C> {
	@Override
	public final C create(Object ... entries) {
		@SuppressWarnings("unchecked")
		Entry<String, String>[] array = new Entry[entries.length];
		int i = 0;
		for (Object o : entries) {
			@SuppressWarnings("unchecked")
			Entry<String, String> e = (Entry<String, String>) o;
			array[i++] = e;
		}
		return create(array);
	}

	protected abstract C create(Entry<String, String>[] entries);

	@Override
	@SuppressWarnings("unchecked")
	public final Entry<String, String>[] createArray(int length) {
		return new Entry[length];
	}

	@Override
	public String[] createKeysArray(int length) {
		return new String[length];
	}
	@Override
	public String[] createValuesArray(int length) {
		return new String[length];
	}

	/** Returns the original element list, unchanged. */
	@Override
	public Iterable<Entry<String, String>> order(List<Entry<String, String>> insertionOrder) {
		return insertionOrder;
	}

	@Override
	public SampleElements<Entry<String, String>> samples() {
		throw new UnsupportedOperationException("This generator is expected to wrapped by a "
				+ MultimapStringTestSuiteBuilder.class.getName());
	}
	
	static SampleElements<Entry<String, String>> createSamplesWithDistinctKeys() {
		return new SampleElements<Map.Entry<String, String>>(Helpers.mapEntry("one", "January"), Helpers.mapEntry(
				"two", "February"), Helpers.mapEntry("three", "March"), Helpers.mapEntry("four", "April"),
				Helpers.mapEntry("five", "May"));
	}
	static SampleElements<Entry<String, String>> createSamplesWithSameKeys() {
		return new SampleElements<Map.Entry<String, String>>(Helpers.mapEntry("one", "January"), Helpers.mapEntry(
				"one", "February"), Helpers.mapEntry("one", "March"), Helpers.mapEntry("two", "April"),
				Helpers.mapEntry("two", "May"));
	}
}
