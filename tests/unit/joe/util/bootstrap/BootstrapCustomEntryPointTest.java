package joe.util.bootstrap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;

public class BootstrapCustomEntryPointTest {
	@Test
	public void testCustomEntryPoint() throws Exception {
		final Map<String, String> props = ImmutableMap.of(BootstrapMain.BOOTSTRAP_ENABLE_KEY, "true",
				BootstrapMain.BOOTSTRAP_ENABLE_LOGGING_KEY, "true", BootstrapMain.BOOTSTRAP_MAIN_CLASS_KEY,
				MyEntryPointClass.class.getName(), BootstrapMain.BOOTSTRAP_MAIN_METHOD_KEY, "myEntryPointMethod");
		AbstractPropertySupplier propertySupplier = new AbstractPropertySupplier() {
			@Override
			public Supplier<Map<String, String>> getSystemPropertiesSupplier() {
				return Suppliers.ofInstance(props);
			}
		};

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
		AbstractPropertySupplier propertySupplier = new AbstractPropertySupplier() {
			@Override
			public Supplier<Map<String, String>> getSystemPropertiesSupplier() {
				return Suppliers.ofInstance(props);
			}
		};
		BootstrapResult bootstrapResult = BootstrapMain.withCustomPropertySupplier(propertySupplier).prepareProperties();
		try {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			assertThat(System.getProperty(BootstrapMain.BOOTSTRAP_MAIN_CLASS_KEY),
					is(stackTrace[stackTrace.length - 1].getClassName()));
		} finally {
			Map<String, String> priorSystemProperties = bootstrapResult.getPriorSystemProperties();
			System.getProperties().clear();
			System.getProperties().putAll(priorSystemProperties);
		}
	}

	public static final class MyEntryPointClass {
		private static boolean ran = false;
		public static void myEntryPointMethod(String[] args) {
			ran = true;
		}
	}
}
