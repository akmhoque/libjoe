package joe.collect.perf;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class TestParam {
	public final int size;
	public final int loops;

	public TestParam(int size, int loops) {
		this.size = size;
		this.loops = loops;
	}

	// Create an array of TestParam from a varargs sequence:
	public static ImmutableList<TestParam> from(int ... values) {
		checkArgument(values.length % 2 == 0);
		int size = values.length / 2;
		
		Builder<TestParam> builder = ImmutableList.builder();
		
		int n = 0;
		for (int i = 0; i < size; i++) {
			builder.add(new TestParam(values[n++], values[n++])); // size, reps
		}
		return builder.build();
	}

	// Convert a String array to a TestParam array:
	public static ImmutableList<TestParam> array(String[] values) {
		int[] vals = new int[values.length];
		for (int i = 0; i < vals.length; i++)
			vals[i] = Integer.decode(values[i]);
		return from(vals);
	}
}
