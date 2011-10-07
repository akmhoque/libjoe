package joe.collect;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;

public class ConcatenatedListTest {
	public static Test suite() {
		return new ConcatenatedListTest().createTests();
	}
	
	public Test createTests() {
		TestSuite suite = new TestSuite("ConcatenatedList");
		
		suite.addTest(testsForConcatLists$Left());
		suite.addTest(testsForConcatLists$Right());
		
		return suite;
	}

	public Test testsForConcatLists$Right() {
		return ListTestSuiteBuilder.using(new TestStringListGenerator() {
			@Override
			public List<String> create(String[] elements) {
				return ConcatenatedList.concat(asList(Arrays.<String> asList(), asList(elements)));
			}
		}).named("ConcatManyLists[empty, some]").withFeatures(CollectionSize.ANY, CollectionFeature.ALLOWS_NULL_VALUES,
				CollectionFeature.KNOWN_ORDER).createTestSuite();
	}
	public Test testsForConcatLists$Left() {
		return ListTestSuiteBuilder.using(new TestStringListGenerator() {
			@Override
			public List<String> create(String[] elements) {
				return ConcatenatedList.concat(asList(asList(elements), Arrays.<String> asList()));
			}
		}).named("ConcatManyLists[some, empty]").withFeatures(CollectionSize.ANY, CollectionFeature.ALLOWS_NULL_VALUES,
				CollectionFeature.KNOWN_ORDER).createTestSuite();
	}
}
