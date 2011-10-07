package joe.collect;

import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.google.common.collect.Ordering;
import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;

public class ArrayBackedMapTest extends TestCase {
	public static Test suite() {
		return new ArrayBackedMapTest().allTests();
	}

	public Test allTests() {
		TestSuite suite = new TestSuite("Array-backed maps");
		suite.addTest(testsForArrayBackedMap());
		suite.addTest(testsForSortedArrayBackedMap());
		suite.addTest(testsForSortedArrayBackedMapWithComparator());
		return suite;
	}

	public Test testsForArrayBackedMap() {
		return MapTestSuiteBuilder.using(new TestStringMapGenerator() {
			@Override
			protected Map<String, String> create(Entry<String, String>[] entries) {
				return populate(new ArrayBackedMap<String, String>(), entries);
			}
		}).named("ArrayBackedMap").withFeatures(MapFeature.GENERAL_PURPOSE, CollectionSize.ANY).createTestSuite();
	}
	public Test testsForSortedArrayBackedMap() {
		return MapTestSuiteBuilder.using(new TestStringMapGenerator() {
			@Override
			protected Map<String, String> create(Entry<String, String>[] entries) {
				return populate(new SortedArrayBackedMap<String, String>(), entries);
			}
		}).named("SortedArrayBackedMap").withFeatures(MapFeature.GENERAL_PURPOSE, CollectionFeature.KNOWN_ORDER,
				CollectionSize.ANY).createTestSuite();
	}
	public Test testsForSortedArrayBackedMapWithComparator() {
		return MapTestSuiteBuilder.using(new TestStringMapGenerator() {
			@Override
			protected Map<String, String> create(Entry<String, String>[] entries) {
				return populate(new SortedArrayBackedMap<String, String>(Ordering.natural()), entries);
			}
		}).named("SortedArrayBackedMap").withFeatures(MapFeature.GENERAL_PURPOSE, CollectionFeature.KNOWN_ORDER,
				CollectionSize.ANY).createTestSuite();
	}

	static <K, V> Map<K, V> populate(Map<K, V> map, Entry<K, V>[] entries) {
		for (Entry<K, V> entry : entries) {
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}
}
