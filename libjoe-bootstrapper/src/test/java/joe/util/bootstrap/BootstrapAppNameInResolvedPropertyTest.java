package joe.util.bootstrap;

import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_APPLICATION_NAME_KEY;
import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_ENABLE_KEY;
import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_ENABLE_LOGGING_KEY;
import static joe.util.bootstrap.PropertyProviderFactories.newMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class BootstrapAppNameInResolvedPropertyTest extends BoostrapperTestPropertySupport {
	private static final String APP_NAME = "application-name";
	private static final String KEY_TO_RESOLVE = "${key.${" + BOOTSTRAP_APPLICATION_NAME_KEY + "}}";
	private static final String RESOLVED_KEY = "key." + APP_NAME;
	private static final String VALUE_FOR_RESOLVED_KEY = "value";
	private static final String KEY_2 = "key2";
	private static final Map<String, String> PROPS_MAP = ImmutableMap.of(RESOLVED_KEY, VALUE_FOR_RESOLVED_KEY, KEY_2, KEY_TO_RESOLVE,
			BOOTSTRAP_ENABLE_KEY, "true", BOOTSTRAP_ENABLE_LOGGING_KEY, "true");
	
	@Test
	public void test() throws Exception {
		PropertyProvider pp = BootstrapMain.withCustomPropertySupplier(new TestPropertySupplier(PROPS_MAP))
				.withApplicationName(APP_NAME)
				.publishTo(
				newMap());
		
		assertThat(pp.getProperty(BOOTSTRAP_APPLICATION_NAME_KEY), is(APP_NAME));
		assertThat(pp.getProperty(KEY_2), is(VALUE_FOR_RESOLVED_KEY));
	}
}
