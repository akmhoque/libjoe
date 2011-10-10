package joe.collect.perf;

import com.google.common.collect.testing.AbstractContainerTester;

public abstract class AbstractTest<C, E> {
	String name;
	public AbstractTest(String name) {
		this.name = name;
	}
	// Override this Template Method for different tests.
	// Returns actual number of repetitions of test.
	abstract int test(C container, TestParam tp);
}
