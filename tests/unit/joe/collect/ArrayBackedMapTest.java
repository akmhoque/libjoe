package joe.collect;

import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.TestsForMapsInJavaUtil;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;

public class ArrayBackedMapTest extends TestCase {
	public static Test suite() {
		return new ArrayBackedMapTest().allTests();
	}

	public Test allTests() {
		return testsForArrayBackedMap();
	}

	public Test testsForArrayBackedMap() {
		return MapTestSuiteBuilder
				.using(new TestStringMapGenerator() {
					@Override
					protected Map<String, String> create(
							Entry<String, String>[] entries) {
						return populate(
								new AbstractArrayBackedMap<String, String>() {
								}, entries);
					}
				}).named("ArrayBackedMap")
				.withFeatures(MapFeature.GENERAL_PURPOSE, CollectionSize.ANY)
				.createTestSuite();
	}

	private static <T> Map<T, String> populate(Map<T, String> map,
			Entry<T, String>[] entries) {
		for (Entry<T, String> entry : entries) {
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}
}
