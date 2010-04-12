package joe.collect.perf;

import static com.google.common.collect.Lists.*;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import joe.collect.CircularArrayList;
import joe.collect.CircularArrayListTest;

/**
 * Class to exercise various {@link List} and {@link Deque} implementations. Taken from <a
 * href="http://www.artima.com/weblogs/viewpost.jsp?thread=122295">this page</a>.
 *
 * @author Bruce Eckel
 * @author adapted by Joe Kearney
 */
public class ListPerformance {
	static Random rand;
	public static void reset() {
		rand = new Random(System.nanoTime());
	}

	static final int reps = 1000;
	static final List<Test<List<Integer>>> tests = newArrayList();
	static final List<Test<Deque<Integer>>> qTests = newArrayList();
	static {
		tests.add(new ListAddTester("add"));
		tests.add(new ListGetTester("get"));
		tests.add(new ListSetTester("set"));
		tests.add(new ListIterThirdAddTester("iterAdd/3"));
		tests.add(new ListIterMidAddTester("iterAdd/2"));
		tests.add(new ListInsertTester("insert(5)"));
		tests.add(new ListRemoveRandomTester("removeRnd"));
		tests.add(new ListRemoveMidTester("removeMid"));
		tests.add(new ListRemoveFirstTester("remove(0)"));
	}
	static {
		qTests.add(new QueueAddFirstTester("addFirst"));
		qTests.add(new QueueAddLastTester("addLast"));
		qTests.add(new QueueRemoveFirstTester("rmFirst"));
		qTests.add(new QueueRemoveLastTester("rmLast"));
	}

	private static final class QueueRemoveLastTester extends Test<Deque<Integer>> {
		/**
		 * @param name
		 */
		private QueueRemoveLastTester(String name) {
			super(name);
		}

		@Override
		int test(Deque<Integer> list, TestParam tp) {
			int loops = tp.loops;
			int size = tp.size;
			for (int i = 0; i < loops; i++) {
				list.clear();
				list.addAll(new CircularArrayListTest.CountingArrayList(size));
				while (list.size() > 0)
					list.removeLast();
			}
			return loops * size;
		}
	}

	private static final class QueueRemoveFirstTester extends Test<Deque<Integer>> {
		/**
		 * @param name
		 */
		private QueueRemoveFirstTester(String name) {
			super(name);
		}

		@Override
		int test(Deque<Integer> list, TestParam tp) {
			int loops = tp.loops;
			int size = tp.size;
			for (int i = 0; i < loops; i++) {
				list.clear();
				list.addAll(new CircularArrayListTest.CountingArrayList(size));
				while (list.size() > 0)
					list.removeFirst();
			}
			return loops * size;
		}
	}

	private static final class QueueAddLastTester extends Test<Deque<Integer>> {
		/**
		 * @param name
		 */
		private QueueAddLastTester(String name) {
			super(name);
		}

		@Override
		int test(Deque<Integer> list, TestParam tp) {
			int loops = tp.loops;
			int size = tp.size;
			for (int i = 0; i < loops; i++) {
				list.clear();
				for (int j = 0; j < size; j++)
					list.addLast(47);
			}
			return loops * size;
		}
	}

	private static final class QueueAddFirstTester extends Test<Deque<Integer>> {
		/**
		 * @param name
		 */
		private QueueAddFirstTester(String name) {
			super(name);
		}

		@Override
		int test(Deque<Integer> list, TestParam tp) {
			int loops = tp.loops;
			int size = tp.size;
			for (int i = 0; i < loops; i++) {
				list.clear();
				for (int j = 0; j < size; j++)
					list.addFirst(47);
			}
			return loops * size;
		}
	}

	private static final class ListRemoveFirstTester extends Test<List<Integer>> {
		/**
		 * @param name
		 */
		private ListRemoveFirstTester(String name) {
			super(name);
		}

		@Override
		int test(List<Integer> list, TestParam tp) {
			int loops = tp.loops;
			int size = tp.size;
			for (int i = 0; i < loops; i++) {
				list.clear();
				list.addAll(new CircularArrayListTest.CountingArrayList(size));
				for (int j = 0; j < size; ++j) {
					list.remove(0);
				}
			}
			return loops * size;
		}
	}

	private static final class ListRemoveMidTester extends Test<List<Integer>> {
		/**
		 * @param name
		 */
		private ListRemoveMidTester(String name) {
			super(name);
		}

		@Override
		int test(List<Integer> list, TestParam tp) {
			int loops = tp.loops;
			int size = tp.size;
			for (int i = 0; i < loops; i++) {
				list.clear();
				list.addAll(new CircularArrayListTest.CountingArrayList(size));
				int s;
				while ((s = list.size()) != 0) {
					list.remove(s / 2); // Minimize random access cost
				}
			}
			return loops * size;
		}
	}

	private static final class ListRemoveRandomTester extends Test<List<Integer>> {
		private final Random RANDOM = new Random();

		/**
		 * @param name
		 */
		private ListRemoveRandomTester(String name) {
			super(name);
		}

		@Override
		int test(List<Integer> list, TestParam tp) {
			int loops = tp.loops;
			int size = tp.size;
			for (int i = 0; i < loops; i++) {
				list.clear();
				list.addAll(new CircularArrayListTest.CountingArrayList(size));
				for (int j = list.size(); j > 0; j--) {
					list.remove(RANDOM.nextInt(j));
				}
			}
			return loops * size;
		}
	}

	private static final class ListInsertTester extends Test<List<Integer>> {
		/**
		 * @param name
		 */
		private ListInsertTester(String name) {
			super(name);
		}

		@Override
		int test(List<Integer> list, TestParam tp) {
			int loops = tp.loops;
			for (int i = 0; i < loops; i++)
				list.add(5, 47); // Minimize random access cost
			return loops;
		}
	}

	private static final class ListIterMidAddTester extends Test<List<Integer>> {
		/**
		 * @param name
		 */
		private ListIterMidAddTester(String name) {
			super(name);
		}

		@Override
		int test(List<Integer> list, TestParam tp) {
			final int LOOPS = 1000000;
			int half = list.size() / 2;
			ListIterator<Integer> it = list.listIterator(half);
			for (int i = 0; i < LOOPS; i++)
				it.add(47);
			return LOOPS;
		}
	}

	private static final class ListIterThirdAddTester extends Test<List<Integer>> {
		/**
		 * @param name
		 */
		private ListIterThirdAddTester(String name) {
			super(name);
		}

		@Override
		int test(List<Integer> list, TestParam tp) {
			final int LOOPS = 1000000;
			int third = list.size() / 3;
			ListIterator<Integer> it = list.listIterator(third);
			for (int i = 0; i < LOOPS; i++)
				it.add(47);
			return LOOPS;
		}
	}

	private static final class ListSetTester extends Test<List<Integer>> {
		/**
		 * @param name
		 */
		private ListSetTester(String name) {
			super(name);
		}

		@Override
		int test(List<Integer> list, TestParam tp) {
			int loops = tp.loops * reps;
			int listSize = list.size();
			for (int i = 0; i < loops; i++)
				list.set(rand.nextInt(listSize), 47);
			return loops;
		}
	}

	private static final class ListGetTester extends Test<List<Integer>> {
		/**
		 * @param name
		 */
		private ListGetTester(String name) {
			super(name);
		}

		@Override
		int test(List<Integer> list, TestParam tp) {
			int loops = tp.loops * reps;
			int listSize = list.size();
			for (int i = 0; i < loops; i++)
				list.get(rand.nextInt(listSize));
			return loops;
		}
	}

	private static final class ListAddTester extends Test<List<Integer>> {
		/**
		 * @param name
		 */
		private ListAddTester(String name) {
			super(name);
		}

		@Override
		int test(List<Integer> list, TestParam tp) {
			int loops = tp.loops;
			int listSize = tp.size;
			for (int i = 0; i < loops; i++) {
				list.clear();
				for (int j = 0; j < listSize; j++)
					list.add(j);
			}
			return loops * listSize;
		}
	}

	static class ListTester extends Tester<List<Integer>> {
		public ListTester(List<Integer> container, List<Test<List<Integer>>> tests) {
			super(container, tests);
		}

		// Fill to the appropriate size before each test:
		@Override
		protected List<Integer> initialize(int size) {
			container.clear();
			container.addAll(new CircularArrayListTest.CountingArrayList(size));
			return container;
		}

		// Convenience method:
		public static void run(List<Integer> list, List<Test<List<Integer>>> tests) {
			new ListTester(list, tests).timedTest();
		}
	}

	public static void main(String[] args) {
		System.out.println("All times in nanoseconds per rep");

		System.out.println("Running test suite to warm up...");
		doMain(args); // warm up
		System.out.println("Completed warmup run");
		System.out.println();

		System.out.println("Running test suite for real...");
		doMain(args);
		System.out.println("Completed real run");
	}

	/**
	 * @param args
	 */
	private static void doMain(String[] args) {
		Tester.fieldWidth = 12;
		// Tester.defaultParams = TestParam.array(10, 75000, 100, 50000, 1000, 20000, 10000, 2000);
		Tester.defaultParams = TestParam.array(10, 750, 100, 500, 1000, 200, 10000, 20);

		ListTester.run(new CircularArrayList<Integer>(), tests);
		ListTester.run(new ArrayList<Integer>(), tests);
	}
}
