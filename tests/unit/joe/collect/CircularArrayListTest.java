package joe.collect;

import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.powermock.reflect.Whitebox;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;

/**
 * Class under test: {@link CircularArrayList}
 *
 * @author Joe Kearney
 */
public class CircularArrayListTest extends TestCase {
	public static Test suite() {
		TestSuite testSuite = new TestSuite("CirularArrayList test");
		testSuite.addTest(new TestSuite(CustomTests.class, "Custom tests"));
		testSuite.addTest(new TestSuite(ListIteratorTests.class, "ListIterator tests"));
		testSuite.addTest(new TestSuite(InsertGapTests.class, "InsertGap tests"));
		testSuite.addTest(new TestSuite(DeleteRangeTests.class, "DeleteRange tests"));
		testSuite.addTest(createCircularArrayListTestCase(0));
		testSuite.addTest(createCircularArrayListTestCase(1));
		// testSuite.addTest(createCircularArrayListTestCase(7));
		// testSuite.addTest(createCircularArrayListTestCase(12));
		// testSuite.addTest(createCircularArrayListTestCase(15));
		return testSuite;
	}
	/**
	 * Creates a test suite to test the {@link List} methods of the {@link CircularArrayList}.
	 *
	 * @param offset {@code true} to shift the head and tail pointers away from zero
	 * @return the test suite
	 */
	private static Test createCircularArrayListTestCase(final int offset) {
		return ListTestSuiteBuilder.using(new TestStringListGenerator() {
			@Override
			public List<String> create(String[] elements) {
				CircularArrayList<String> cal = new CircularArrayList<String>();
				for (int i = 0; i < offset; ++i) {
					cal.add(String.valueOf(i));
				}
				for (int i = 0; i < offset; ++i) {
					cal.removeFirst();
				}
				assertThat((Integer) Whitebox.getInternalState(cal, "head"), is(offset));
				assertThat((Integer) Whitebox.getInternalState(cal, "tail"), is(offset));
				cal.addAll(MinimalCollection.of(elements));
				return cal;
			}
		}).named(CircularArrayList.class.getSimpleName() + (offset > 0 ? " offset[" + offset + "]" : "")).withFeatures(
				ListFeature.GENERAL_PURPOSE, CollectionSize.ANY).createTestSuite();
	}

	public static class ListIteratorTests extends TestCase {
		public void testIterator1AtThird() {
			int listSize = 10;
			runListIteratorMidpointInsertionTest(1, listSize, listSize / 3);
		}
		public void testIterator2AtThird() {
			int listSize = 10;
			runListIteratorMidpointInsertionTest(2, listSize, listSize / 3);
		}
		public void testIterator3AtThird() {
			int listSize = 10;
			runListIteratorMidpointInsertionTest(2, listSize, listSize / 3);
		}
		public void testIterator10AtThird() {
			int listSize = 10;
			runListIteratorMidpointInsertionTest(10, listSize, listSize / 3);
		}

		private void runListIteratorMidpointInsertionTest(int newElementCount, int listSize, int insertionPoint) {
			int newElement = 47;
			CountingArrayList referenceBase = new CountingArrayList(listSize);
			CircularArrayList<Integer> list = new CircularArrayList<Integer>(referenceBase);
			ListIterator<Integer> it = list.listIterator(insertionPoint);
			for (int i = 0; i < newElementCount; i++) {
				it.add(newElement);
			}

			List<Integer> ref = new ArrayList<Integer>(referenceBase);
			ref.addAll(insertionPoint, Collections.nCopies(newElementCount, newElement));

			assertThat(list, is(ref));
		}

		public void testIterator$HasNextHasNextHasNextHasNext() {
			CircularArrayList<String> list = new CircularArrayList<String>();
			list.add("a");
			list.add("b");
			list.add("c");
			ListIterator<String> listIterator = list.listIterator();
			assertThat(listIterator.hasNext(), is(true));
			assertThat(listIterator.next(), is("a"));
			assertThat(listIterator.hasNext(), is(true));
			assertThat(listIterator.next(), is("b"));
			assertThat(listIterator.hasNext(), is(true));
			assertThat(listIterator.next(), is("c"));
			assertThat(listIterator.hasNext(), is(false));
		}
		public void testIterator$HasNextHasNextHasNextAdd() {
			CircularArrayList<String> list = new CircularArrayList<String>();
			ListIterator<String> listIterator = list.listIterator();
			assertThat(listIterator.hasNext(), is(false));
			assertThat(listIterator.hasNext(), is(false));
			assertThat(listIterator.hasNext(), is(false));
			listIterator.add("a"); // should not throw
			assertTrue(list.contains("a"));
			assertThat(list.get(0), is("a"));
		}
		public void testIterator$HasNextHasNextHasNextSet() {
			CircularArrayList<String> list = new CircularArrayList<String>();
			ListIterator<String> listIterator = list.listIterator();
			assertThat(listIterator.hasNext(), is(false));
			assertThat(listIterator.hasNext(), is(false));
			assertThat(listIterator.hasNext(), is(false));
			try {
				listIterator.set("a"); // should throw
				fail();
			} catch (IllegalStateException expected) {}
			assertThat(list.size(), is(0));
			assertFalse(list.contains("a"));
		}
		public void testIterator$HasNextHasNextAddAdd() {
			CircularArrayList<String> list = new CircularArrayList<String>();
			ListIterator<String> listIterator = list.listIterator();
			assertThat(listIterator.hasNext(), is(false));
			assertThat(listIterator.hasNext(), is(false));
			listIterator.add("a"); // should not throw
			assertThat(list.size(), is(1));
			assertTrue(list.contains("a"));
			assertThat(list.get(0), is("a"));

			listIterator.add("b"); // should not throw
			assertThat(list.size(), is(2));
			assertTrue(list.contains("b"));
			assertThat(list.get(0), is("a"));
			assertThat(list.get(1), is("b"));
		}
		public void testIterator$HasNextHasNextAddRemove() {
			CircularArrayList<String> list = new CircularArrayList<String>();
			ListIterator<String> listIterator = list.listIterator();
			assertThat(listIterator.hasNext(), is(false));
			assertThat(listIterator.hasNext(), is(false));
			listIterator.add("a"); // should not throw
			assertThat(list.size(), is(1));
			assertTrue(list.contains("a"));
			assertThat(list.get(0), is("a"));

			try {
				listIterator.remove(); // should throw
				fail();
			} catch (IllegalStateException expected) {}
			// same assertions as above
			assertThat(list.size(), is(1));
			assertTrue(list.contains("a"));
			assertThat(list.get(0), is("a"));
		}
		public void testIterator$AddPreviousPreviousRemove() {
			List<String> list = new CircularArrayList<String>();
			ListIterator<String> listIterator = list.listIterator();

			listIterator.add("a"); // should not throw
			assertThat(list.size(), is(1));
			assertTrue(list.contains("a"));
			assertThat(list.get(0), is("a"));

			listIterator.previous();
			try {
				listIterator.previous();
				fail();
			} catch (NoSuchElementException expected) {}
			listIterator.remove(); // should remove "a"
			assertTrue(list.isEmpty());
			assertThat(list.size(), is(0));
			assertFalse(list.contains("a"));
		}
		public void testIterator$HasNextHasNextNextRemoveHasNext() {
			List<String> list = new CircularArrayList<String>();
			list.add("a"); // should not throw
			assertThat(list.size(), is(1));
			assertTrue(list.contains("a"));
			assertThat(list.get(0), is("a"));

			ListIterator<String> listIterator = list.listIterator();

			assertTrue(listIterator.hasNext());
			assertTrue(listIterator.hasNext());
			assertThat(listIterator.next(), is("a"));
			listIterator.remove();
			assertTrue(list.isEmpty());
			assertThat(list.size(), is(0));
			assertFalse(list.contains("a"));

			assertFalse(listIterator.hasNext());
		}
		public void testIterator$HasNextHasNextNextRemoveNext$several() {
			List<String> list = new CircularArrayList<String>();
			list.add("a"); // should not throw
			list.add("b"); // should not throw
			assertThat(list.size(), is(2));
			assertTrue(list.contains("a"));
			assertTrue(list.contains("b"));
			assertThat(list.get(0), is("a"));
			assertThat(list.get(1), is("b"));

			ListIterator<String> listIterator = list.listIterator();

			assertTrue(listIterator.hasNext());
			assertTrue(listIterator.hasNext());
			assertThat(listIterator.next(), is("a"));
			listIterator.remove();
			assertThat(list.size(), is(1));
			assertFalse(list.contains("a"));
			assertTrue(list.contains("b"));

			assertThat(listIterator.next(), is("b"));
		}
		public void testIterator$AddPreviousRemoveHasNext() {
			List<String> list = new CircularArrayList<String>();

			ListIterator<String> listIterator = list.listIterator();

			listIterator.add("a"); // should not throw
			assertThat(list.size(), is(1));
			assertTrue(list.contains("a"));
			assertThat(list.get(0), is("a"));

			assertThat(listIterator.previous(), is("a"));

			listIterator.remove();
			assertTrue(list.isEmpty());
			assertThat(list.size(), is(0));
			assertFalse(list.contains("a"));

			assertFalse(listIterator.hasNext());
		}
		public void testIterator$NextNextPreviousRemoveHasNextNextPreviousPrevious() {
			List<String> list = new CircularArrayList<String>();
			list.add("a"); // should not throw
			list.add("b"); // should not throw
			list.add("c"); // should not throw
			assertThat(list.size(), is(3));
			assertTrue(list.contains("a"));
			assertTrue(list.contains("b"));
			assertTrue(list.contains("c"));

			ListIterator<String> listIterator = list.listIterator();

			assertThat(listIterator.next(), is("a"));
			assertThat(listIterator.next(), is("b"));
			assertThat(listIterator.previous(), is("b"));
			listIterator.remove();
			assertThat(list.size(), is(2));
			assertTrue(list.contains("a"));
			assertFalse(list.contains("b"));
			assertTrue(list.contains("c"));

			assertTrue(listIterator.hasNext());
			assertThat(listIterator.next(), is("c"));
			assertThat(listIterator.previous(), is("c"));
			assertThat(listIterator.previous(), is("a"));
		}
		public void testIterator$AddNextRemoveHasNext$zero() {
			List<String> list = new CircularArrayList<String>();

			ListIterator<String> listIterator = list.listIterator();

			listIterator.add("a"); // should not throw
			assertThat(list.size(), is(1));
			assertTrue(list.contains("a"));
			assertThat(list.get(0), is("a"));

			try {
				listIterator.next();
				fail();
			} catch (NoSuchElementException expected) {}

			try {
				listIterator.remove();
				fail();
			} catch (IllegalStateException expected) {}

			assertFalse(listIterator.hasNext());
		}
		public void testIterator$AddNextRemoveHasNext$one() {
			List<String> list = new CircularArrayList<String>();
			list.add("b");

			ListIterator<String> listIterator = list.listIterator();

			listIterator.add("a"); // should not throw
			assertThat(list.size(), is(2));
			assertTrue(list.contains("a"));
			assertThat(list.get(0), is("a"));
			assertThat(list.get(1), is("b"));

			assertThat(listIterator.next(), is("b"));

			listIterator.remove();
			assertThat(list.size(), is(1));
			assertTrue(list.contains("a"));
			assertFalse(list.contains("b"));
			assertThat(list.get(0), is("a"));

			assertFalse(listIterator.hasNext());
		}
		public void testIterator$AddNextRemoveNext$one() {
			List<String> list = new CircularArrayList<String>();
			list.add("b");

			ListIterator<String> listIterator = list.listIterator();

			listIterator.add("a"); // should not throw
			assertThat(list.size(), is(2));
			assertTrue(list.contains("a"));
			assertThat(list.get(0), is("a"));
			assertThat(list.get(1), is("b"));

			assertThat(listIterator.next(), is("b"));

			listIterator.remove();
			assertThat(list.size(), is(1));
			assertTrue(list.contains("a"));
			assertFalse(list.contains("b"));
			assertThat(list.get(0), is("a"));

			try {
				listIterator.next();
				fail();
			} catch (NoSuchElementException expected) {}
		}
		public void testIterator$HasNextAddNextRemove$zero() {
			List<String> list = new CircularArrayList<String>();

			ListIterator<String> listIterator = list.listIterator();

			assertFalse(listIterator.hasNext());

			listIterator.add("a"); // should not throw
			assertThat(list.size(), is(1));
			assertTrue(list.contains("a"));
			assertThat(list.get(0), is("a"));

			try {
				listIterator.next();
				fail();
			} catch (NoSuchElementException expected) {}

			try {
				listIterator.remove();
				fail();
			} catch (IllegalStateException expected) {}
		}
		public void testIterator$HasNextAddNextRemove$several() {
			List<String> list = new CircularArrayList<String>();

			list.add("a");
			list.add("b");
			list.add("c");

			ListIterator<String> listIterator = list.listIterator();

			assertTrue(listIterator.hasNext());

			listIterator.add("d"); // should not throw
			assertThat(list.size(), is(4));
			assertTrue(list.contains("d"));
			assertThat(list.get(0), is("d"));

			String next = listIterator.next();
			assertThat(next, is("a"));

			listIterator.remove();
			assertFalse(list.contains("a"));
			assertThat(list.size(), is(3));
		}
		public void testIterator$HasNextAddPreviousRemove() {
			List<String> list = new CircularArrayList<String>();
			list.add("other");

			ListIterator<String> listIterator = list.listIterator();

			assertTrue(listIterator.hasNext());

			listIterator.add("a"); // should not throw
			assertThat(list.size(), is(2));
			assertTrue(list.contains("a"));
			assertThat(list.get(0), is("a"));

			String previous = listIterator.previous();
			assertThat(previous, is("a"));

			listIterator.remove();
			assertFalse(list.contains("a"));
		}
		public void testIterator$offset1$HasNextHasNextHasNextHasNextRemove() {
			CircularArrayList<String> list = new CircularArrayList<String>();
			list.add("a");
			list.removeFirst();
			ListIterator<String> listIterator = list.listIterator();

			assertFalse(listIterator.hasNext());
			assertFalse(listIterator.hasNext());
			assertFalse(listIterator.hasNext());
			assertFalse(listIterator.hasNext());
			try {
				listIterator.remove();
				fail();
			} catch (IllegalStateException expected) {}
		}
	}

	public static class InsertGapTests extends TestCase {
		public void testInsertGap$caseAHead() throws Exception {
			List<String> list = asList(null, "a", "b", "c", null, null, null, null);
			List<String> expected = asList(null, "a", "b", "c", null, null, null, null);

			CircularArrayList<Object> cal = runInsertTest(1, 4, list, expected, 1, 1);
			assertHeadTail(cal, 0, 4);
		}
		public void testInsertGap$caseAHardLeft() throws Exception {
			List<String> list = asList(null, "a", "b", "c", null, null, null, null);
			List<String> expected = asList(null, null, "a", "b", "c", null, null, null);

			runInsertTest(1, 4, list, expected, 0, 1);
		}
		public void testInsertGap$caseAHardLeftZeroLength() throws Exception {
			List<String> list = asList(null, "a", "b", "c", null, null, null, null);
			List<String> expected = asList(null, "a", "b", "c", null, null, null, null);

			runInsertTest(1, 4, list, expected, 0, 0);
		}
		public void testInsertGap$caseAMiddleZeroLength() throws Exception {
			List<String> list = asList(null, "a", "b", "c", null, null, null, null);
			List<String> expected = asList(null, "a", "b", "c", null, null, null, null);

			runInsertTest(1, 4, list, expected, 2, 0);
		}
		public void testInsertGap$caseAMiddleOneLength() throws Exception {
			List<String> list = asList(null, "a", "b", "c", null, null, null, null);
			List<String> expected = asList("a", "a", "b", "c", null, null, null, null);

			runInsertTest(1, 4, list, expected, 2, 1);
		}
		public void testInsertGap$caseBOneLength() throws Exception {
			List<String> list = asList(null, null, null, null, null, "a", "b", "c");
			List<String> expected = asList(null, null, null, null, "a", "a", "b", "c");

			runInsertTest(5, 0, list, expected, 6, 1);
		}
		public void testInsertGap$caseDOneLength() throws Exception {
			List<String> list = asList("c", null, null, null, null, null, "a", "b");
			List<String> expected = asList("c", null, null, null, null, "a", "a", "b");

			runInsertTest(6, 1, list, expected, 7, 1);
		}
		/**
		 * @param head
		 * @param tail
		 * @param list
		 * @param expected
		 * @param gapIndex TODO
		 * @param gapLength TODO
		 * @throws Exception
		 */
		@SuppressWarnings("unchecked")
		private CircularArrayList<Object> runInsertTest(int head, int tail, List<String> list, List<String> expected,
				int gapIndex, int gapLength) throws Exception {
			CircularArrayList<Object> adl = createADL(head, tail, list.toArray());
			Whitebox.invokeMethod(adl, "insertGap", gapIndex, gapLength);
			List<Object> elements = getElementsAsList(adl);
			assertThat(elements, is((List<Object>) (List<?>) expected));
			return adl;
		}
	}

	public static class DeleteRangeTests extends TestCase {
		public void testDeleteRange$caseA$1() throws Exception {
			List<String> list = asList(null, "a", "b", "extra", "c", null, null, null);
			List<String> expected = asList(null, "a", "b", "c", null, null, null, null);

			runDeleteTest(1, 5, list, expected, 3, 1);
		}
		public void testDeleteRange$caseA$2() throws Exception {
			List<String> list = asList(null, "a", "b", "extra1", "extra2", "c", null, null);
			List<String> expected = asList(null, "a", "b", "c", null, null, null, null);

			runDeleteTest(1, 6, list, expected, 3, 2);
		}
		public void testDeleteRange$caseA$3() throws Exception {
			List<String> list = asList(null, "a", "b", null, null, null, null, null);
			List<String> expected = asList(null, "a", null, null, null, null, null, null);

			runDeleteTest(1, 3, list, expected, 2, 1);
		}
		public void testDeleteRange$caseA$4() throws Exception {
			List<String> list = asList(null, "a", "b", "c", null, null, null, null);
			List<String> expected = asList(null, "a", null, null, null, null, null, null);

			runDeleteTest(1, 4, list, expected, 2, 2);
		}
		public void testDeleteRange$caseA$5() throws Exception {
			List<String> list = asList("a", null, null, null, null, null, null, null);
			List<String> expected = asList(null, null, null, null, null, null, null, null);

			runDeleteTest(0, 1, list, expected, 0, 1);
		}
		public void testDeleteRange$caseB$1() throws Exception {
			List<String> list = asList("e", "f", "g", "h", null, "b", "c", "d");
			List<String> expected = asList("e", "h", null, null, null, "b", "c", "d");

			runDeleteTest(5, 4, list, expected, 1, 2);
		}
		public void testDeleteRange$caseB$2() throws Exception {
			List<String> list = asList("e", "f", "g", null, null, "b", "c", "d");
			List<String> expected = asList("f", "g", null, null, null, "b", "c", "d");

			runDeleteTest(5, 3, list, expected, 0, 1);
		}
		public void testDeleteRange$caseB$3() throws Exception {
			List<String> list = asList("e", "f", "g", null, null, "b", "c", "d");
			List<String> expected = asList("e", "f", null, null, null, "b", "c", "d");

			runDeleteTest(5, 3, list, expected, 2, 1);
		}
		public void testDeleteRange$caseC$1() throws Exception {
			List<String> list = asList("e", "f", "g", null, null, "b", "c", "d");
			List<String> expected = asList("e", "f", "g", null, null, null, "b", "d");

			runDeleteTest(5, 3, list, expected, 6, 1);
		}
		public void testDeleteRange$caseC$2() throws Exception {
			List<String> list = asList("e", "f", "g", null, null, "b", "c", "d");
			List<String> expected = asList("e", "f", "g", null, null, null, null, "d");

			runDeleteTest(5, 3, list, expected, 5, 2);
		}
		public void testDeleteRange$caseC$3() throws Exception {
			List<String> list = asList("e", "f", "g", null, null, "b", "c", "d");
			List<String> expected = asList("e", "f", "g", null, null, null, "b", "c");

			runDeleteTest(5, 3, list, expected, 7, 1);
		}
		public void testDeleteRange$caseD$1() throws Exception {
			List<String> list = asList("e", "f", "g", null, null, "b", "c", "d");
			List<String> expected = asList("g", null, null, null, null, "b", "c", "f");

			runDeleteTest(5, 3, list, expected, 7, 2);
		}
		public void testDeleteRange$caseD$2() throws Exception {
			List<String> list = asList("e", "f", "g", "h", "i", "j", null, null, null, null, null, null, "a", "b", "c",
					"d");
			List<String> expected = asList("a", "b", "g", "h", "i", "j", null, null, null, null, null, null, null,
					null, null, null);

			runDeleteTest(12, 6, list, expected, 14, 4);
		}
		@SuppressWarnings("unchecked")
		private void runDeleteTest(int head, int tail, List<String> list, List<String> expected, int gapIndex,
				int gapLength) throws Exception {
			CircularArrayList<Object> adl = createADL(head, tail, list.toArray());
			Whitebox.invokeMethod(adl, "deleteRange", gapIndex, gapLength);
			List<Object> elements = getElementsAsList(adl);
			assertThat(elements, is((List<Object>) (List<?>) expected));
		}
	}

	/**
	 * Covers all cases of shiftPrefixLeft and shiftSuffixRight.
	 *
	 * @author Joe Kearney
	 */
	public static class ShiftTests extends TestCase {
		public void testShiftPrefixLeft$NoLength() throws Exception {
			List<String> start = asList(null, "a", "b", "c", null, null, null, null);
			List<String> expected = start;
			CircularArrayList<Object> adl = createADL(1, 4, start.toArray());
			Whitebox.invokeMethod(adl, "shiftPrefixLeft", 0, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 0, 4);
		}
		public void testShiftSuffixRight$NoLength() throws Exception {
			List<String> start = asList(null, "a", "b", "c", null, null, null, null);
			List<String> expected = start;
			CircularArrayList<Object> adl = createADL(1, 4, start.toArray());
			Whitebox.invokeMethod(adl, "shiftSuffixRight", 0, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 1, 5);
		}
		public void testShiftPrefixLeft$NoWrap() throws Exception {
			List<String> start = asList(null, "a", "b", "c", null, null, null, null);
			List<String> expected = asList("a", "a", "b", "c", null, null, null, null);
			CircularArrayList<Object> adl = createADL(1, 4, start.toArray());
			Whitebox.invokeMethod(adl, "shiftPrefixLeft", 1, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 0, 4);
		}
		public void testShiftPrefixLeft$1Wrap() throws Exception {
			List<String> start = asList(null, "a", "b", "c", null, null, null, null);
			List<String> expected = asList(null, "a", "b", "c", null, null, null, "a");
			CircularArrayList<Object> adl = createADL(1, 4, start.toArray());
			Whitebox.invokeMethod(adl, "shiftPrefixLeft", 1, 2);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 7, 4);
		}
		public void testShiftPrefixLeft$2Wrap() throws Exception {
			List<String> start = asList(null, "a", "b", "c", null, null, null, null);
			List<String> expected = asList("b", "a", "b", "c", null, null, null, "a");
			CircularArrayList<Object> adl = createADL(1, 4, start.toArray());
			Whitebox.invokeMethod(adl, "shiftPrefixLeft", 2, 2);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 7, 4);
		}
		public void testShiftPrefixLeft$3Wrap() throws Exception {
			List<String> start = asList("0", "a", "b", "c", null, null, null, null);
			List<String> expected = asList("b", "a", "b", "c", null, null, "0", "a");
			CircularArrayList<Object> adl = createADL(0, 4, start.toArray());
			Whitebox.invokeMethod(adl, "shiftPrefixLeft", 3, 2);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 6, 4);
		}
		public void testShiftSuffixRight$NoWrap() throws Exception {
			List<String> start = asList(null, "a", "b", "c", null, null, null, null);
			List<String> expected = asList(null, "a", "b", "c", "c", null, null, null);
			CircularArrayList<Object> adl = createADL(1, 4, start.toArray());
			Whitebox.invokeMethod(adl, "shiftSuffixRight", 1, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 1, 5);
		}
		public void testShiftSuffixRight$NoWrap$NewTail0() throws Exception {
			List<String> start = asList(null, null, null, null, "a", "b", "c", null);
			List<String> expected = asList(null, null, null, null, "a", "b", "c", "c");
			CircularArrayList<Object> adl = createADL(4, 7, start.toArray());
			Whitebox.invokeMethod(adl, "shiftSuffixRight", 1, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 4, 0);
		}
		public void testShiftSuffixRight$1Wrap() throws Exception {
			List<String> start = asList(null, null, null, null, "a", "b", "c", null);
			List<String> expected = asList("c", null, null, null, "a", "b", "c", null);
			CircularArrayList<Object> adl = createADL(4, 7, start.toArray());
			Whitebox.invokeMethod(adl, "shiftSuffixRight", 1, 2);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 4, 1);
		}
		public void testShiftSuffixRight$2Wrap() throws Exception {
			List<String> start = asList(null, null, null, null, "a", "b", "c", null);
			List<String> expected = asList("c", null, null, null, "a", "b", "c", "b");
			CircularArrayList<Object> adl = createADL(4, 7, start.toArray());
			Whitebox.invokeMethod(adl, "shiftSuffixRight", 2, 2);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 4, 1);
		}
		public void testShiftSuffixRight$Wrap() throws Exception {
			List<String> start = asList(null, null, null, null, "a", "b", "c", "d");
			List<String> expected = asList("c", "d", null, null, "a", "b", "c", "b");
			CircularArrayList<Object> adl = createADL(4, 0, start.toArray());
			Whitebox.invokeMethod(adl, "shiftSuffixRight", 3, 2);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 4, 2);
		}
		public void testShiftSuffixRight$AlreadyWrapped1() throws Exception {
			List<String> start = asList("e", null, null, null, null, null, "c", "d");
			List<String> expected = asList("e", "e", null, null, null, null, "c", "d");
			CircularArrayList<Object> adl = createADL(6, 1, start.toArray());
			Whitebox.invokeMethod(adl, "shiftSuffixRight", 1, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 6, 2);
		}
		public void testShiftSuffixRight$AlreadyWrapped2() throws Exception {
			List<String> start = asList("e", null, null, null, null, null, "c", "d");
			List<String> expected = asList("d", "e", null, null, null, null, "c", "d");
			CircularArrayList<Object> adl = createADL(6, 1, start.toArray());
			Whitebox.invokeMethod(adl, "shiftSuffixRight", 2, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 6, 2);
		}
		public void testShiftSuffixRight$AlreadyWrapped3() throws Exception {
			List<String> start = asList("e", null, null, null, null, null, "c", "d");
			List<String> expected = asList("d", "e", null, null, null, null, "c", "c");
			CircularArrayList<Object> adl = createADL(6, 1, start.toArray());
			Whitebox.invokeMethod(adl, "shiftSuffixRight", 3, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 6, 2);
		}
		public void testShiftSuffixRight$AlreadyWrapped4() throws Exception {
			List<String> start = asList("e", null, null, null, null, null, "c", "d");
			List<String> expected = asList("c", "d", "e", null, null, null, "c", "d");
			CircularArrayList<Object> adl = createADL(6, 1, start.toArray());
			Whitebox.invokeMethod(adl, "shiftSuffixRight", 3, 2);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 6, 3);
		}

		public void testShiftPrefixLeft$AlreadyWrapped1() throws Exception {
			List<String> start = asList("b", "c", null, null, null, null, null, "a");
			List<String> expected = asList("b", "c", null, null, null, null, "a", "a");
			CircularArrayList<Object> adl = createADL(7, 2, start.toArray());
			Whitebox.invokeMethod(adl, "shiftPrefixLeft", 1, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 6, 2);
		}
		public void testShiftPrefixLeft$AlreadyWrapped2() throws Exception {
			List<String> start = asList("b", "c", null, null, null, null, null, "a");
			List<String> expected = asList("b", "c", null, null, null, null, "a", "b");
			CircularArrayList<Object> adl = createADL(7, 2, start.toArray());
			Whitebox.invokeMethod(adl, "shiftPrefixLeft", 2, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 6, 2);
		}
		public void testShiftPrefixLeft$AlreadyWrapped3() throws Exception {
			List<String> start = asList("b", "c", null, null, null, null, null, "a");
			List<String> expected = asList("c", "c", null, null, null, null, "a", "b");
			CircularArrayList<Object> adl = createADL(7, 2, start.toArray());
			Whitebox.invokeMethod(adl, "shiftPrefixLeft", 3, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 6, 2);
		}
		public void testShiftPrefixLeft$AlreadyWrapped4() throws Exception {
			List<String> start = asList("b", "c", null, null, null, null, null, "a");
			List<String> expected = asList("b", "c", null, null, null, "a", "b", "c");
			CircularArrayList<Object> adl = createADL(7, 2, start.toArray());
			Whitebox.invokeMethod(adl, "shiftPrefixLeft", 3, 2);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 5, 2);
		}

		public void testLongShiftPrefixLeft$1() throws Exception {
			List<Integer> start = asList(2, 47, 47, 3, 4, 5, 6, 7, 8, 9, null, null, null, null, 0, 1);
			List<Integer> expected = asList(2, 47, 47, 3, 4, 5, 6, 7, 8, 9, null, null, null, 0, 1, 2);
			CircularArrayList<Object> adl = createADL(14, 10, start.toArray());
			Whitebox.invokeMethod(adl, "shiftPrefixLeft", 3, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 13, 10);
		}

		public void testShiftSuffixLeft$Simple() throws Exception {
			List<String> start = asList(null, "a", "b", "c", null, null, null, null);
			List<String> expected = asList(null, "a", "c", null, null, null, null, null);
			CircularArrayList<Object> adl = createADL(1, 4, start.toArray());
			Whitebox.invokeMethod(adl, "shiftSuffixLeft", 3, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 1, 3);
		}
		public void testShiftSuffixLeft$PartialWrap() throws Exception {
			List<String> start = asList("c", "d", null, null, null, null, "a", "b");
			List<String> expected = asList("d", null, null, null, null, null, "a", "c");
			CircularArrayList<Object> adl = createADL(6, 2, start.toArray());
			Whitebox.invokeMethod(adl, "shiftSuffixLeft", 0, 1);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 6, 1);
		}
		public void testShiftSuffixLeft$CompleteWrap() throws Exception {
			List<String> start = asList("c", "d", null, null, null, "0", "a", "b");
			List<String> expected = asList(null, null, null, null, null, "0", "c", "d");
			CircularArrayList<Object> adl = createADL(5, 2, start.toArray());
			Whitebox.invokeMethod(adl, "shiftSuffixLeft", 0, 2);
			assertCALMatches(adl, expected);
			assertHeadTail(adl, 5, 0);
		}
	}

	public static class CustomTests extends TestCase {

		public void testResizeByAdd() throws Exception {
			CircularArrayList<Integer> list = new CircularArrayList<Integer>(2);
			assertThat(getArray(list).length, is(8));
			for (int i = 0; i < 7; i++) {
				list.add(i);
			}
			assertThat(getArray(list).length, is(8));
			list.add(7);
			assertThat(getArray(list).length, is(16));

			Whitebox.invokeMethod(list, "checkInvariants");
		}
		public void testResizeByIterator() throws Exception {
			CircularArrayList<Integer> list = new CircularArrayList<Integer>(2);
			ListIterator<Integer> listIterator = list.listIterator();
			assertThat(getArray(list).length, is(8));
			for (int i = 0; i < 7; i++) {
				listIterator.add(i);
			}
			assertThat(getArray(list).length, is(8));
			listIterator.add(7);
			assertThat(getArray(list).length, is(16));
			assertThat(listIterator.previous(), is(7));
			listIterator.remove();
			assertThat(getArray(list).length, is(16));

			Whitebox.invokeMethod(list, "checkInvariants");
		}

		public void testAddAll$10() {
			CircularArrayList<Integer> list = new CircularArrayList<Integer>();
			ArrayList<Integer> toCopy = new ArrayList<Integer>();
			int size = 10;
			for (int i = 0; i < size; i++) {
				toCopy.add(i);
			}
			list.addAll(toCopy);
			assertThat(list.size(), is(size));
			for (int i = 0; i < size; i++) {
				assertThat(list.get(i), is(i));
			}
		}
		public void testAddAll$10atZero() {
			CircularArrayList<Integer> list = new CircularArrayList<Integer>();
			ArrayList<Integer> toCopy = new ArrayList<Integer>();
			int size = 10;
			for (int i = 0; i < size; i++) {
				toCopy.add(i);
			}
			list.addAll(0, toCopy);
			assertThat(list.size(), is(size));
			for (int i = 0; i < size; i++) {
				assertThat(list.get(i), is(i));
			}
		}
		public void testAddAll$10atHalf() {
			CircularArrayList<Integer> list = new CircularArrayList<Integer>();
			ArrayList<Integer> toCopy = new ArrayList<Integer>();
			int size = 10;
			for (int i = 0; i < size; i++) {
				toCopy.add(i);
			}
			list.addAll(0, toCopy);
			list.addAll(5, toCopy);
			assertThat(list.size(), is(size * 2));
			for (int i = 0; i < size; i++) {
				assertThat(list.get(i + 5), is(i));
			}
		}
		public void testAddAllAfterRemove() {
			CircularArrayList<Integer> list = new CircularArrayList<Integer>();
			int size = 10;
			list.clear();
			list.addAll(new CountingArrayList(size));
			removeFirstToDrain(list);
			assertTrue(list.isEmpty());
		}
		public void testAddAllWithNonZeroHeadTailPointers() {
			CircularArrayList<Integer> list = new CircularArrayList<Integer>();
			list.offer(1);
			list.removeFirst();
			list.addAll(new CountingArrayList(3));
			assertThat(list.size(), is(3));
		}
		public void testAddAllPopAllAddAll() {
			int size = 3;
			CircularArrayList<Integer> list = new CircularArrayList<Integer>();
			list.addAll(new CountingArrayList(size));
			removeFirstToDrain(list);
			list.addAll(new CountingArrayList(size));
			assertThat(list.size(), is(size));
			assertThat(list.get(0), is(0));
		}

		public void testSubListAddAffectsOriginal() {
			CircularArrayList<Integer> list = new CircularArrayList<Integer>(new CountingArrayList(5));
			List<Integer> subList = list.subList(1, 4);
			assertThat(subList, hasSize(3));
			subList.add(6);
			assertThat(list, is(asList(0, 1, 2, 3, 6, 4)));
		}
		public void testOffsetSubListAddAffectsOriginal() {
			CircularArrayList<Integer> list = new CircularArrayList<Integer>();
			list.add(1);
			list.removeFirst();
			list.addAll(new CountingArrayList(5));
			List<Integer> subList = list.subList(1, 4);
			assertThat(subList, hasSize(3));
			subList.add(6);
			assertThat(list, is(asList(0, 1, 2, 3, 6, 4)));
		}
		public void testOffsetZeroSubListAddAffectsOriginal() {
			CircularArrayList<Integer> list = new CircularArrayList<Integer>();
			list.add(1);
			list.removeFirst();
			list.addAll(new CountingArrayList(5));
			List<Integer> subList = list.subList(0, 0);
			assertThat(subList, hasSize(0));
			subList.add(6);
			assertThat(list, is(asList(6, 0, 1, 2, 3, 4)));
		}
		public void testOffsetZeroSubEmptyListAddAffectsOriginal() {
			CircularArrayList<Integer> list = new CircularArrayList<Integer>();
			list.add(1);
			list.removeFirst();
			List<Integer> subList = list.subList(0, 0);
			assertThat(subList, hasSize(0));
			subList.add(6);
			assertThat(list, is(asList(6)));
		}
	}

	static void removeFirstToDrain(CircularArrayList<Integer> list) {
		while (list.size() > 0) {
			list.removeFirst();
		}
	}
	static Object[] getArray(CircularArrayList<?> list) {
		return Whitebox.getInternalState(list, "elements");
	}
	static <E> CircularArrayList<E> createADL(int head, int tail, E ... elements) {
		CircularArrayList<E> arrayDequeList = new CircularArrayList<E>();
		Whitebox.setInternalState(arrayDequeList, "head", head);
		Whitebox.setInternalState(arrayDequeList, "tail", tail);
		Whitebox.setInternalState(arrayDequeList, "elements", (Object) elements);
		return arrayDequeList;
	}

	@SuppressWarnings("serial")
	public static class CountingArrayList extends ArrayList<Integer> {
		public CountingArrayList(int size) {
			ensureCapacity(size);
			for (int i = 0; i < size; ++i) {
				this.add(i);
			}
		}
	}

	static Matcher<List<?>> hasSize(final int size) {
		return new TypeSafeMatcher<List<?>>() {
			@Override
			public boolean matchesSafely(List<?> item) {
				return item.size() == size;
			}
			public void describeTo(Description arg0) {
				arg0.appendText("Size ").appendValue(size);
			}
		};
	}
	static List getElementsAsList(CircularArrayList<?> adl) {
		return asList((Object[]) Whitebox.getInternalState(adl, "elements"));
	}
	@SuppressWarnings("unchecked")
	static void assertCALMatches(CircularArrayList<?> adl, List<?> expected) {
		List elements = getElementsAsList(adl);
		assertThat(elements, is(expected));
	}
	static void assertHead(CircularArrayList<?> cal, int expected) {
		int head = Whitebox.getInternalState(cal, "head");
		assertThat(head, is(expected));
	}
	static void assertTail(CircularArrayList<?> cal, int expected) {
		int tail = Whitebox.getInternalState(cal, "tail");
		assertThat(tail, is(expected));
	}
	static void assertHeadTail(CircularArrayList<?> cal, int expectedHead, int expectedTail) {
		assertHead(cal, expectedHead);
		assertTail(cal, expectedTail);
	}
}
