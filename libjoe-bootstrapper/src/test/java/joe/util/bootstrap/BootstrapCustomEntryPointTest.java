package joe.util.bootstrap;

import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_ENABLE_KEY;
import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_ENABLE_LOGGING_KEY;
import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_MAIN_CLASS_KEY;
import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_MAIN_METHOD_KEY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class BootstrapCustomEntryPointTest {
	@Test
	public void testCustomEntryPoint() throws Exception {
		final Map<String, String> props = ImmutableMap.of(
				BOOTSTRAP_ENABLE_KEY, "true",
				BOOTSTRAP_ENABLE_LOGGING_KEY, "true",
				BOOTSTRAP_MAIN_CLASS_KEY, MyEntryPointClass.class.getName(),
				BOOTSTRAP_MAIN_METHOD_KEY, "myEntryPointMethod");
		PropertySupplier propertySupplier = new TestPropertySupplier(props);

		try {
			BootstrapMain.withCustomPropertySupplier(propertySupplier).launchApplication();
		} finally {
			for (String key : props.keySet()) {
				System.clearProperty(key);
			}
		}

		assertTrue("Application didn't run", MyEntryPointClass.ran);
	}

	@Test
	public void testMainClassGetsSet() throws Exception {
		final Map<String, String> props = ImmutableMap.of(BootstrapMain.BOOTSTRAP_ENABLE_KEY, "true",
				BootstrapMain.BOOTSTRAP_ENABLE_LOGGING_KEY, "true", BootstrapMain.BOOTSTRAP_MAIN_METHOD_KEY,
				"myEntryPointMethod");
		PropertySupplier propertySupplier = new TestPropertySupplier(props);
		PropertyProvider pp = BootstrapMain.withCustomPropertySupplier(propertySupplier).publishTo(PropertyProviderFactories.newMap());

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		assertThat(pp.getProperty(BOOTSTRAP_MAIN_CLASS_KEY),
				is(stackTrace[stackTrace.length - 1].getClassName()));
	}

	public static final class MyEntryPointClass {
		private static boolean ran = false;
		public static void myEntryPointMethod(String[] args) {
			ran = true;
		}
	}
}
