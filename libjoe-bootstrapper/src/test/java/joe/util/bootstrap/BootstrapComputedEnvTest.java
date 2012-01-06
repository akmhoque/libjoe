package joe.util.bootstrap;

import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_ENABLE_KEY;
import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_ENABLE_LOGGING_KEY;
import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_ENVIRONMENT_KEY;
import static joe.util.bootstrap.PropertyProviderFactories.newMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import joe.util.bootstrap.BootstrapMain.BootstrapBuilder;

import org.junit.Test;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class BootstrapComputedEnvTest {
	private static final class EnvironmentPropertySupplier extends AbstractPropertySupplier {
		private final BootstrapMain bootstrap;

		EnvironmentPropertySupplier(BootstrapBuilder bootstrap) {
			this.bootstrap = bootstrap.getBootstrapper();
		}

		@Override
		public Supplier<Map<String, String>> getSystemPropertiesSupplier() {
			return Suppliers.ofInstance(BOOTSTRAP_ENABLE_MAP);
		}
		@Override
		public Iterable<Supplier<Map<String, String>>> getEnvironmentPropertiesSupplier() {
			if (ENVIRONMENT.equals(bootstrap.getApplicationProperty(BOOTSTRAP_ENVIRONMENT_KEY))) {
				return ImmutableList.of(Suppliers.ofInstance(ENV_PROPS));
			}
			return super.getEnvironmentPropertiesSupplier();
		}
		@Override
		public Iterable<Supplier<Map<String, String>>> getUserPropertiesSuppliers() {
			return ImmutableList.of(Suppliers.ofInstance(USER_PROPS));
		}
	}

	private static final String ENVIRONMENT = "abcde";
	private static final String KEY = "blah";
	private static final String VALUE = "resolved";
	private static final String KEY_TO_RESOLVE = "key2";
	private static final String VALUE_TO_RESOLVE = "value.${blah}.something";
	private static final String RESOLVED_VALUE = "value.resolved.something";
	private static final Map<String,String> BOOTSTRAP_ENABLE_MAP = ImmutableMap.of(BOOTSTRAP_ENABLE_KEY, "true", BOOTSTRAP_ENABLE_LOGGING_KEY, "true");
	private static final Map<String, String> USER_PROPS = ImmutableMap.of(BOOTSTRAP_ENVIRONMENT_KEY, ENVIRONMENT, KEY_TO_RESOLVE, VALUE_TO_RESOLVE);
	private static final Map<String, String> ENV_PROPS = ImmutableMap.of(KEY, VALUE);

	@Test
	public void testComputedEnvFound() throws Exception {
			BootstrapBuilder bootstrap = BootstrapMain.newBuilder();
			PropertySupplier propertySupplier = new EnvironmentPropertySupplier(bootstrap);
			bootstrap.withCustomPropertySupplier(propertySupplier);
			PropertyProvider pp = bootstrap.publishTo(newMap());
			
			assertThat(pp.getProperty(BOOTSTRAP_ENVIRONMENT_KEY), is(ENVIRONMENT));
			assertThat(pp.getProperty(KEY), is(VALUE));
	}
	
	@Test
	public void testPropertiesResolvedFromComputedEnv() throws Exception {
			BootstrapBuilder bootstrap = BootstrapMain.newBuilder();
			PropertySupplier propertySupplier = new EnvironmentPropertySupplier(bootstrap);
			bootstrap.withCustomPropertySupplier(propertySupplier);
			PropertyProvider pp = bootstrap.publishTo(newMap());
			assertThat(pp.getProperty(KEY_TO_RESOLVE), is(RESOLVED_VALUE));
	}
}
