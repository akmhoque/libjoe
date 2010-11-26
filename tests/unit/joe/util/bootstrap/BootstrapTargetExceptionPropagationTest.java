package joe.util.bootstrap;

import org.junit.Test;

public class BootstrapTargetExceptionPropagationTest {
	static String EXCEPTION_MESSAGE = "expected";
	private static class ThrowsRuntimeException {
		@SuppressWarnings("unused")
		public static void main(String[] args) {
			throw new RuntimeException(EXCEPTION_MESSAGE);
		}
	}
	private static class ThrowsException {
		@SuppressWarnings("unused")
		public static void main(String[] args) throws Exception {
			throw new Exception(EXCEPTION_MESSAGE);
		}
	}
	private static class ThrowsThrowable {
		@SuppressWarnings("unused")
		public static void main(String[] args) throws Throwable {
			throw new Throwable(EXCEPTION_MESSAGE);
		}
	}
	private static class ThrowsNFE{
		@SuppressWarnings("unused")
		public static void main(String[] args) {
			throw new NumberFormatException(EXCEPTION_MESSAGE);
		}
	}
	
	@Test
	public void testThrowsRuntimeException() throws Exception {
		runTest(ThrowsRuntimeException.class, RuntimeException.class);
	}
	@Test
	public void testThrowsException() throws Exception {
		runTest(ThrowsException.class, Exception.class);
	}
	@Test
	public void testThrowsThrowable() throws Exception {
		runTest(ThrowsThrowable.class, Throwable.class);
	}
	@Test
	public void testThrowsNFE() throws Exception {
		runTest(ThrowsNFE.class, NumberFormatException.class);
	}

	private void runTest(Class<?> mainClass, Class<? extends Throwable> expectedType) {
		try {
			BootstrapMain.launchApplication(mainClass);
		} catch (Throwable e) {
			if (!(e instanceof BootstrapException && expectedType == e.getCause().getClass()
					&& EXCEPTION_MESSAGE.equals(e.getCause().getMessage()))) {
				fail("Unexpected exception thrown", e);
			}
		}
	}

	static void fail(String message, Throwable cause) {
		AssertionError assertionError = new AssertionError(message);
		assertionError.initCause(cause);
		throw assertionError;
	}
}
