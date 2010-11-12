package com.google.common.collect.testfw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map.Entry;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ConcurrentHashMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.testing.MultimapStringTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMultimapGenerator;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import com.google.common.collect.testing.features.MultimapFeature;
import com.google.common.collect.testing.features.ObjectFeature;

public class MultimapTestfwTest {
	public static Test suite() {
		return new MultimapTestfwTest().allTests();
	}

	public Test allTests() {
		TestSuite suite = new TestSuite("Multimaps");
		suite.addTest(testsForConcurrentHashMultimap());
		suite.addTest(testsForHashMultimap());
		suite.addTest(testsForArrayListMultimap());
		suite.addTest(testsForLinkedListMultimap());
		suite.addTest(testsForImmutableListMultimap());
		suite.addTest(testsForImmutableSetMultimap());
		return suite;
	}

	public Test testsForConcurrentHashMultimap() {
		return MultimapStringTestSuiteBuilder.using(new TestStringMultimapGenerator<SetMultimap<String, String>>() {
			@Override
			protected SetMultimap<String, String> create(Entry<String, String>[] entries) {
				ConcurrentHashMultimap<String, String> chm = ConcurrentHashMultimap.create();
				for (Entry<String, String> entry : entries) {
					chm.put(entry.getKey(), entry.getValue());
				}
				return chm;
			}
		}).named(ConcurrentHashMultimap.class.getSimpleName()).withFeatures(MapFeature.GENERAL_PURPOSE,
				CollectionSize.ANY, ObjectFeature.SERIALIZABLE).createTestSuite();
	}
	public Test testsForHashMultimap() {
		return MultimapStringTestSuiteBuilder.using(new TestStringMultimapGenerator<SetMultimap<String, String>>() {
			@Override
			protected SetMultimap<String, String> create(Entry<String, String>[] entries) {
				HashMultimap<String, String> m = HashMultimap.create();
				for (Entry<String, String> entry : entries) {
					m.put(entry.getKey(), entry.getValue());
				}
				return m;
			}
		}).named("HashMultimap").withFeatures(MapFeature.GENERAL_PURPOSE, CollectionSize.ANY,
				MapFeature.ALLOWS_NULL_KEYS, MapFeature.ALLOWS_NULL_VALUES, ObjectFeature.SERIALIZABLE).createTestSuite();
	}
	public Test testsForArrayListMultimap() {
		return MultimapStringTestSuiteBuilder.using(new TestStringMultimapGenerator<ListMultimap<String, String>>() {
			@Override
			protected ListMultimap<String, String> create(Entry<String, String>[] entries) {
				ArrayListMultimap<String, String> m = ArrayListMultimap.create();
				for (Entry<String, String> entry : entries) {
					m.put(entry.getKey(), entry.getValue());
				}
				return m;
			}
		}).named("ArrayListMultimap").withFeatures(MapFeature.GENERAL_PURPOSE, CollectionSize.ANY,
				MapFeature.ALLOWS_NULL_KEYS, MapFeature.ALLOWS_NULL_VALUES, MultimapFeature.ALLOWS_DUPLICATE_VALUES,
				ObjectFeature.SERIALIZABLE).createTestSuite();
	}
	public Test testsForLinkedListMultimap() {
		return MultimapStringTestSuiteBuilder.using(new TestStringMultimapGenerator<ListMultimap<String, String>>() {
			@Override
			protected ListMultimap<String, String> create(Entry<String, String>[] entries) {
				LinkedListMultimap<String, String> m = LinkedListMultimap.create();
				for (Entry<String, String> entry : entries) {
					m.put(entry.getKey(), entry.getValue());
				}
				return m;
			}
		}).named("LinkedListMultimap").withFeatures(MapFeature.GENERAL_PURPOSE, CollectionSize.ANY,
				MapFeature.ALLOWS_NULL_KEYS, MapFeature.ALLOWS_NULL_VALUES, MultimapFeature.ALLOWS_DUPLICATE_VALUES,
				ObjectFeature.SERIALIZABLE).createTestSuite();
	}
	public Test testsForImmutableListMultimap() {
		return MultimapStringTestSuiteBuilder.using(new TestStringMultimapGenerator<ListMultimap<String, String>>() {
			@Override
			protected ListMultimap<String, String> create(Entry<String, String>[] entries) {
				ImmutableListMultimap.Builder<String, String> m = ImmutableListMultimap.builder();
				for (Entry<String, String> entry : entries) {
					m.put(entry.getKey(), entry.getValue());
				}
				return m.build();
			}
		}).named("ImmutableListMultimap").withFeatures(CollectionSize.ANY, MultimapFeature.ALLOWS_DUPLICATE_VALUES,
				ObjectFeature.SERIALIZABLE).createTestSuite();
	}
	public Test testsForImmutableSetMultimap() {
		return MultimapStringTestSuiteBuilder.using(new TestStringMultimapGenerator<SetMultimap<String, String>>() {
			@Override
			protected SetMultimap<String, String> create(Entry<String, String>[] entries) {
				ImmutableSetMultimap.Builder<String, String> m = ImmutableSetMultimap.builder();
				for (Entry<String, String> entry : entries) {
					m.put(entry.getKey(), entry.getValue());
				}
				return m.build();
			}
		}).named("ImmutableSetMultimap").withFeatures(CollectionSize.ANY, ObjectFeature.SERIALIZABLE).createTestSuite();
	}

	@SuppressWarnings("unchecked")
	static <T> T reserialize(T object) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bytes);
			out.writeObject(object);
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
			return (T) in.readObject();
		} catch (IOException e) {
			fail(e, e.getMessage());
		} catch (ClassNotFoundException e) {
			fail(e, e.getMessage());
		}
		throw new AssertionError("not reachable");
	}

	static void fail(Throwable cause, Object message) {
		AssertionFailedError assertionFailedError = new AssertionFailedError(String.valueOf(message));
		assertionFailedError.initCause(cause);
		throw assertionFailedError;
	}
}
