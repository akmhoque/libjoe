package joe.collect.perf;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import joe.collect.SortedArrayBackedMap;

import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;

public class ArrayBackedMapPerfTest {
	static final class Key implements Comparable<Key> {
		@Override
		public int compareTo(Key that) {
			return this.hashCode() - that.hashCode();
		}
		// default hashcode()
		// default equals()
	}
	static final class Value {}
	
	private static final int REPS = 50000;
	private static final int RUNS = 10000000 / REPS;
	
	@Test
	public void testHashMap() throws Exception {
		Map<Key, Value> entries = fillMap(new HashMap<Key, Value>());
		Stopwatch sw = new Stopwatch();
		sw.start();
		Map<Key, Value> map = new HashMap<Key, Value>(entries);
		sw.stop();
		System.out.println("Filling HashMap took " + sw.elapsedTime(TimeUnit.MILLISECONDS) + "ms");
		runLookupTest(map);
	}
	private Map<Key, Value> fillMap(Map<Key, Value> map) {
		System.out.println("Filling map " + map.getClass());
		
		for (int i = 0; i < REPS; i++) {
			map.put(new Key(), new Value());
		}
		return map;
	}
	@Test
	public void testSortedArrayBackedMap() throws Exception {
		System.out.println("Filling map SABM");
		Stopwatch sw = new Stopwatch();
		Map<Key, Value> entries = fillMap(new HashMap<Key, Value>());
		sw.start();
		Map<Key, Value> map = new SortedArrayBackedMap<Key, Value>(entries);
		sw.stop();
		System.out.println("Filling SABM took " + sw.elapsedTime(TimeUnit.MILLISECONDS) + "ms");
		runLookupTest(map);
	}
	
	static void runLookupTest(Map<Key, Value> map) {
		System.out.println("Reps: " + REPS);
		System.out.println("Runs: " + RUNS);
		
		System.out.println("Lookup test: " + map.getClass());
		
		List<Key> keys = newArrayList(map.keySet());
		Collections.shuffle(keys);

		System.out.println("Warmup...");
		for (Key key : Iterables.concat(Collections.nCopies(RUNS, keys))) {
			map.get(key);
		}
		
		System.out.println("Real run...");
		Stopwatch sw = new Stopwatch();
		sw.start();
		
		for (Key key : Iterables.concat(Collections.nCopies(RUNS, keys))) {
			map.get(key);
		}
		
		sw.stop();
		
		System.out.println(sw.elapsedMillis() + "ms total");
		System.out.println((sw.elapsedTime(TimeUnit.NANOSECONDS) / (REPS * RUNS)) + " nanos per rep");
	}
	
	public static void main(String[] args) {
		
	}
}
