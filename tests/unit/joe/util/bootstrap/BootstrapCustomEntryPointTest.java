package joe.util.bootstrap;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;

public class BootstrapCustomEntryPointTest {
	@Test
	public void test() throws Exception {
		AbstractPropertySupplier propertySupplier = new AbstractPropertySupplier() {
			@Override
			public Supplier<Map<String, String>> getSystemPropertiesSupplier() {
				Map<String, String> props = ImmutableMap.of(BootstrapMain.BOOTSTRAP_ENABLE_KEY, "true",
						BootstrapMain.BOOTSTRAP_ENABLE_LOGGING_KEY, "true", BootstrapMain.BOOTSTRAP_MAIN_CLASS_KEY,
						MyEntryPointClass.class.getName(), BootstrapMain.BOOTSTRAP_MAIN_METHOD_KEY,
						"myEntryPointMethod");
				return Suppliers.ofInstance(props);
			}
		};

		BootstrapMain.withCustomPropertySupplier(propertySupplier).launchApplication();

		assertTrue("Application didn't run", MyEntryPointClass.ran);
	}

	public static final class MyEntryPointClass {
		private static boolean ran = false;
		public static void myEntryPointMethod(String[] args) {
			ran = true;
		}
	}
}
