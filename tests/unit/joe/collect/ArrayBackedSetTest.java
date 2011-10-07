package joe.collect;

import java.util.Set;
import java.util.SortedSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.google.common.collect.Ordering;
import com.google.common.collect.testing.SampleElements.Strings;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.TestStringSortedSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.SetFeature;

public class ArrayBackedSetTest extends TestCase {
	public static Test suite() {
		return new ArrayBackedSetTest().allTests();
	}
	
	public Test allTests() {
		TestSuite suite = new TestSuite("Array-backed sets");
		suite.addTest(testsForArrayBackedSet());
		suite.addTest(testsForSortedArrayBackedSet());
		suite.addTest(testsForSortedArrayBackedSetWithComparator());
		suite.addTest(testsForSortedArrayBackedSetHeadSet());
		suite.addTest(testsForSortedArrayBackedSetTailSet());
		return suite;
	}
	
	public Test testsForArrayBackedSet() {
		return SetTestSuiteBuilder.using(new TestStringSetGenerator() {
			@Override
			protected Set<String> create(String[] elements) {
				return populate(new ArrayBackedSet<String>(), elements);
			}
		}).named("ArrayBackedSet").withFeatures(SetFeature.GENERAL_PURPOSE, CollectionSize.ANY).createTestSuite();
	}
	public Test testsForSortedArrayBackedSet() {
		return SetTestSuiteBuilder.using(new TestStringSortedSetGenerator() {
			@Override
			protected SortedSet<String> create(String[] elements) {
				return populate(new SortedArrayBackedSet<String>(), elements);
			}
		}).named("SortedArrayBackedSet").withFeatures(SetFeature.GENERAL_PURPOSE, CollectionFeature.KNOWN_ORDER, CollectionSize.ANY).createTestSuite();
	}
	public Test testsForSortedArrayBackedSetWithComparator() {
		return SetTestSuiteBuilder.using(new TestStringSortedSetGenerator() {
			@Override
			protected SortedSet<String> create(String[] elements) {
				return populate(new SortedArrayBackedSet<String>(Ordering.natural()), elements);
			}
		}).named("SortedArrayBackedSet with Comparator").withFeatures(SetFeature.GENERAL_PURPOSE, CollectionFeature.KNOWN_ORDER,
				CollectionSize.ANY).createTestSuite();
	}
	public Test testsForSortedArrayBackedSetHeadSet() {
		return SetTestSuiteBuilder.using(new TestStringSortedSetGenerator() {
			@Override
			protected SortedSet<String> create(String[] elements) {
				return populate(new SortedArrayBackedSet<String>(), elements).headSet(Strings.AFTER_LAST);
			}
		}).named("SortedArrayBackedSet headSet").withFeatures(SetFeature.GENERAL_PURPOSE, CollectionFeature.KNOWN_ORDER,
				CollectionSize.ANY).createTestSuite();
	}
	public Test testsForSortedArrayBackedSetTailSet() {
		return SetTestSuiteBuilder.using(new TestStringSortedSetGenerator() {
			@Override
			protected SortedSet<String> create(String[] elements) {
				return populate(new SortedArrayBackedSet<String>(), elements).tailSet(Strings.BEFORE_FIRST);
			}
		}).named("SortedArrayBackedSet tailSet").withFeatures(SetFeature.GENERAL_PURPOSE, CollectionFeature.KNOWN_ORDER,
				CollectionSize.ANY).createTestSuite();
	}
	
	static <K, S extends Set<K>> S populate(S set, K[] entries) {
		for (K entry : entries) {
			set.add(entry);
		}
		return set;
	}
}
