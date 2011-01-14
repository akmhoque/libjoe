package joe.util.bootstrap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import joe.util.PropertyUtils;
import joe.util.bootstrap.BootstrapMain.BootstrapBuilder;

import org.junit.Test;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

public class BootstrapPropertyPriorityOrderingTest {
	static final String SYSTEM_PROPERTY = "system";
	static final String USER_PROPERTY = "jkearne";
	static final String MACHINE_PROPERTY = "machine";
	static final String IDE_PROPERTY = "ide";
	static final String PROD_PROPERTY = "prod";

	private static final ImmutableSet<String> ENV_NAMES_IN_ORDER = ImmutableSet.of(PROD_PROPERTY, IDE_PROPERTY,
			MACHINE_PROPERTY, USER_PROPERTY, SYSTEM_PROPERTY);
	private static final String PROPERTY_UNDER_TEST_KEY = "prop";
	private static final Ordering<String> PROPERTY_SET_ORDERING = Ordering.explicit(ENV_NAMES_IN_ORDER.asList());
	private static final Set<Set<String>> ENV_NAMES_POWER_SET = Sets.powerSet(ENV_NAMES_IN_ORDER);

	@Test
	public void testThroughMainMethod() throws Exception {
		for (Set<String> set : ENV_NAMES_POWER_SET) {
			Iterable<List<String>> combinations = new Permutator<String>(ImmutableList.copyOf(set));

			String expectedValue = set.isEmpty() ? null : PROPERTY_SET_ORDERING.max(set);

			for (List<String> combination : combinations) {
				System.clearProperty(PROPERTY_UNDER_TEST_KEY);
				try {
					BootstrapBuilder builder = BootstrapMain.withCustomPropertySupplier(new ExplicitPropertySupplier(
							combination));
					if (expectedValue != null) {
						builder.withMainArgs(expectedValue);
					}
					builder.launchApplication(MyClass.class);
				} catch (Throwable e) {
					AssertionError assertionError = new AssertionError("Failed with combination " + combination);
					assertionError.initCause(e);
					throw assertionError;
				} finally {
					System.clearProperty(PROPERTY_UNDER_TEST_KEY);
				}
			}
		}
	}
	/**
	 * @throws Exception
	 */
	@Test
	public void testPropertyPreparationOnly() throws Exception {
		for (Set<String> set : ENV_NAMES_POWER_SET) {
			Iterable<List<String>> combinations = new Permutator<String>(ImmutableList.copyOf(set));
			
			String expectedValue = set.isEmpty() ? null : PROPERTY_SET_ORDERING.max(set);
			
			for (List<String> combination : combinations) {
				System.clearProperty(PROPERTY_UNDER_TEST_KEY);
				final ImmutableMap<String, String> propsToReinstate = ImmutableMap.copyOf(PropertyUtils.getSystemPropertyStrings());
				try {
					
					BootstrapBuilder builder = BootstrapMain.withCustomPropertySupplier(new ExplicitPropertySupplier(
							combination));
					if (expectedValue != null) {
						builder.withMainArgs(expectedValue);
					}
					builder.prepareProperties();
					MyClass.main(expectedValue);
				} catch (Throwable e) {
					AssertionError assertionError = new AssertionError("Failed with combination " + combination);
					assertionError.initCause(e);
					throw assertionError;
				} finally {
					Properties p = new Properties();
					p.putAll(propsToReinstate);
					System.setProperties(p);
				}
			}
		}
	}

	private static class ExplicitPropertySupplier implements PropertySupplier {
		final Collection<String> envs;
		static final Supplier<Map<String, String>> EMPTY_MAP_SUPPLIER = Suppliers.<Map<String, String>> ofInstance(ImmutableMap.<String, String> of());

		ExplicitPropertySupplier(Collection<String> envs) {
			this.envs = envs;
		}

		@Override
		public Supplier<Map<String, String>> getSystemPropertiesSupplier() {
			return Iterables.getOnlyElement(getEnvPropOrEmpty(SYSTEM_PROPERTY));
		}
		@Override
		public Iterable<Supplier<Map<String, String>>> getUserPropertiesSuppliers() {
			return getEnvPropOrEmpty(USER_PROPERTY);
		}
		@Override
		public Iterable<Supplier<Map<String, String>>> getMachinePropertiesSupplier() {
			return getEnvPropOrEmpty(MACHINE_PROPERTY);
		}
		@Override
		public Iterable<Supplier<Map<String, String>>> getIdePropertiesSupplier() {
			return getEnvPropOrEmpty(IDE_PROPERTY);
		}
		@Override
		public Iterable<Supplier<Map<String, String>>> getEnvironmentPropertiesSupplier() {
			return getEnvPropOrEmpty(PROD_PROPERTY);
		}

		private Iterable<Supplier<Map<String, String>>> getEnvPropOrEmpty(String env) {
			if (envs.contains(env)) {
				return ImmutableList.of(Suppliers.<Map<String, String>> ofInstance(ImmutableMap.of(
						PROPERTY_UNDER_TEST_KEY, env, BootstrapMain.BOOTSTRAP_ENABLE_KEY, "true")));
			} else {
				return ImmutableList.of(EMPTY_MAP_SUPPLIER);
			}
		}
	}

	static final class MyClass {
		public static void main(String ... args) {
			String actualValue = System.getProperty(PROPERTY_UNDER_TEST_KEY);
			String expectedValue = args.length == 0 ? null : args[0];
			assertThat(actualValue, is(expectedValue));
		}
	}

	static class Permutator<E> implements Iterable<List<E>> {
		private List<E> list;

		public Permutator(List<E> list) {
			this.list = list;
		}

		@Override
		public Iterator<List<E>> iterator() {
			return new Permutations<E>(list);
		}
		private static final class Permutations<E> implements Iterator<List<E>> {
			private List<E> inList;
			private int n, m;
			private int[] index;
			private boolean hasMore = true;

			/**
			 * Create a Permutation to iterate through all possible lineups
			 * of the supplied array of Objects.
			 * 
			 * @param Object[] inArray the group to line up
			 * @exception CombinatoricException Should never happen
			 *                with this interface
			 * 
			 */
			public Permutations(List<E> inList) {
				this(inList, inList.size());
			}

			/**
			 * Create a Permutation to iterate through all possible lineups
			 * of the supplied array of Objects.
			 * 
			 * @param Object[] inArray the group to line up
			 * @param inArray java.lang.Object[], the group to line up
			 * @param m int, the number of objects to use
			 * @exception CombinatoricException if m is greater than
			 *                the length of inArray, or less than 0.
			 */
			public Permutations(List<E> inList, int m) {
				this.inList = inList;
				this.n = inList.size();
				this.m = m;

				assert this.n >= m && m >= 0;

				/**
				 * index is an array of ints that keep track of the next
				 * permutation to return. For example, an index on a permutation
				 * of 3 things might contain {1 2 0}. This index will be followed
				 * by {2 0 1} and {2 1 0}.
				 * Initially, the index is {0 ... n - 1}.
				 */

				this.index = new int[this.n];
				for (int i = 0; i < this.n; i++) {
					this.index[i] = i;
				}

				/**
				 * The elements from m to n are always kept ascending right
				 * to left. This keeps the dip in the interesting region.
				 */
				reverseAfter(m - 1);
			}

			/**
			 * @return true, unless we have already returned the last permutation.
			 */
			@Override
			public boolean hasNext() {
				return this.hasMore;
			}

			/**
			 * Move the index forward a notch. The algorithm first finds the
			 * rightmost index that is less than its neighbor to the right. This
			 * is the dip point. The algorithm next finds the least element to
			 * the right of the dip that is greater than the dip. That element is
			 * switched with the dip. Finally, the list of elements to the right
			 * of the dip is reversed.
			 * <p>
			 * For example, in a permutation of 5 items, the index may be {1, 2, 4, 3, 0}. The dip is 2 the rightmost
			 * element less than its neighbor on its right. The least element to the right of 2 that is greater than 2
			 * is 3. These elements are swapped, yielding {1, 3, 4, 2, 0}, and the list right of the dip point is
			 * reversed, yielding {1, 3, 0, 2, 4}.
			 * <p>
			 * The algorithm is from Applied Combinatorics, by Alan Tucker.
			 * 
			 */
			private void moveIndex() {
				// find the index of the first element that dips
				int i = rightmostDip();
				if (i < 0) {
					this.hasMore = false;
					return;
				}

				// find the least greater element to the right of the dip
				int leastToRightIndex = i + 1;
				for (int j = i + 2; j < this.n; j++) {
					if (this.index[j] < this.index[leastToRightIndex] && this.index[j] > this.index[i]) {
						leastToRightIndex = j;
					}
				}

				// switch dip element with least greater element to its right
				int t = this.index[i];
				this.index[i] = this.index[leastToRightIndex];
				this.index[leastToRightIndex] = t;

				if (this.m - 1 > i) {
					// reverse the elements to the right of the dip
					reverseAfter(i);
					// reverse the elements to the right of m - 1
					reverseAfter(this.m - 1);
				}
			}

			/**
			 * @return java.lang.Object, the next permutation of the original Object array.
			 *         <p>
			 *         Actually, an array of Objects is returned. The declaration must say just Object, because the
			 *         Permutations class implements Iterator, which declares that the next() returns a plain Object.
			 *         Users must cast the returned object to (Object[]).
			 */
			@Override
			public List<E> next() {
				if (!this.hasMore) {
					return null;
				}
				List<E> list = new ArrayList<E>(this.m);
				for (int i = 0; i < this.m; i++) {
					int thisIndexI = this.index[i];
					E element = this.inList.get(thisIndexI);
					list.add(element);
				}
				moveIndex();
				return list;
			}

			/**
			 * Reverse the index elements to the right of the specified index.
			 */
			private void reverseAfter(int i) {
				int start = i + 1;
				int end = this.n - 1;
				while (start < end) {
					int t = this.index[start];
					this.index[start] = this.index[end];
					this.index[end] = t;
					start++;
					end--;
				}

			}

			/**
			 * @return int the index of the first element from the right
			 *         that is less than its neighbor on the right.
			 */
			private int rightmostDip() {
				for (int i = this.n - 2; i >= 0; i--) {
					if (this.index[i] < this.index[i + 1]) {
						return i;
					}
				}
				return -1;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		}
	}
}
