package joe.collect.perf;

import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;

public class PerformanceTester<C, E> {
	private static int fieldWidth = 12;
	private static ImmutableList<TestParam> defaultParams = TestParam.from(10, 5000, 100, 5000, 1000, 5000, 10000, 500);
	// Override this to modify pre-test initialization:
	protected C initialize(int size) {
		return container;
	}
	protected C container;
	private String headline = "";
	private List<AbstractTest<C, E>> tests;
	private static String stringField() {
		return "%" + fieldWidth + "s";
	}
	private static String numberField() {
		return "%" + fieldWidth + "d";
	}
	private static int sizeWidth = 6;
	private static String sizeField = "%" + sizeWidth + "s";
	private List<TestParam> paramList;
	
	public PerformanceTester(C container, List<? extends AbstractTest<C, E>> tests) {
		this(container, tests, defaultParams);
	}
	public PerformanceTester(C container, List<? extends AbstractTest<C, E>> tests, List<TestParam> paramList) {
		this.container = container;
		this.tests = Collections.unmodifiableList(tests);
		if (container != null) {
			headline = container.getClass().getSimpleName();
		}
		this.paramList = paramList;
	}
	public void setHeadline(String newHeadline) {
		headline = newHeadline;
	}
	public static <C, E> void run(C cntnr, List<? extends AbstractTest<C, E>> tests) {
		new PerformanceTester<C, E>(cntnr, tests).timedTest();
	}
	public static <C, E> void run(C cntnr, List<? extends AbstractTest<C, E>> tests, List<TestParam> paramList) {
		new PerformanceTester<C, E>(cntnr, tests, paramList).timedTest();
	}
	private void displayHeader() {
		// Calculate width and pad with '-':
		int width = fieldWidth * tests.size() + sizeWidth;
		int dashLength = width - headline.length() - 1;
		StringBuilder head = new StringBuilder(width);
		for (int i = 0; i < dashLength / 2; i++)
			head.append('-');
		head.append(' ');
		head.append(headline);
		head.append(' ');
		for (int i = 0; i < dashLength / 2; i++)
			head.append('-');
		System.out.println(head);
		// Print column headers:
		System.out.format(sizeField, "size");
		for (AbstractTest<?, ?> test : tests)
			System.out.format(stringField(), test.name);
		System.out.println();
	}
	// Run the tests for this container:
	public void timedTest() {
		displayHeader();
		for (TestParam param : paramList) {
			System.out.format(sizeField, param.size);
			for (AbstractTest<C, E> test : tests) {
				/* warmup */ {
					C kontainer = initialize(param.size);
					test.test(kontainer, param);
				}
				
				C kontainer = initialize(param.size);
				long start = System.nanoTime();
				// Call the template method:
				int reps = test.test(kontainer, param);
				long duration = System.nanoTime() - start;
				long timePerRep = duration / reps; // Nanoseconds
				System.out.format(numberField(), timePerRep);
			}
			System.out.println();
		}
	}
}
