package joe.collect.perf;

public abstract class Test<C> {
	String name;
	public Test(String name) {
		this.name = name;
	}
	// Override this Template Method for different tests.
	// Returns actual number of repetitions of test.
	abstract int test(C container, TestParam tp);
}
