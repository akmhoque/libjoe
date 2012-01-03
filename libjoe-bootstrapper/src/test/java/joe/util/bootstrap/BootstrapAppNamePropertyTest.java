package joe.util.bootstrap;

import static com.google.common.collect.Maps.newHashMap;
import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_APPLICATION_NAME_KEY;
import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_ENABLE_KEY;
import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_ENABLE_LOGGING_KEY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import joe.util.PropertyUtils;

import org.junit.Test;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;

public class BootstrapAppNamePropertyTest extends BoostrapperTestPropertySupport {
	private static final String APP_NAME = "application-name";
	private static final String APP_NAME_2 = "application-name.other";
	private static final String KEY_TO_RESOLVE = "${key.${" + BOOTSTRAP_APPLICATION_NAME_KEY + "}}";
	private static final String RESOLVED_KEY = "key." + APP_NAME;
	private static final String VALUE_FOR_RESOLVED_KEY = "value";
	private static final String KEY_2 = "key2";
	private static final Map<String, String> PROPS_MAP = ImmutableMap.of(RESOLVED_KEY, VALUE_FOR_RESOLVED_KEY, KEY_2,
			KEY_TO_RESOLVE, BOOTSTRAP_ENABLE_KEY, "true", BOOTSTRAP_ENABLE_LOGGING_KEY, "true");

	@Test
	public void testResolvedProperty() throws Exception {
		BootstrapMain.withCustomPropertySupplier(new AbstractPropertySupplier() {
			@Override
			public Supplier<Map<String, String>> getSystemPropertiesSupplier() {
				Map<String, String> props = newHashMap();
				props.putAll(PropertyUtils.getSystemPropertyStrings());
				props.putAll(PROPS_MAP);
				return Suppliers.<Map<String, String>> ofInstance(ImmutableMap.copyOf(props));
			}
		}).withApplicationName(APP_NAME).loadProperties().publishTo(PropertyProviderFactories.systemProperties());

		assertThat(System.getProperty(BOOTSTRAP_APPLICATION_NAME_KEY), is(APP_NAME));
		assertThat(System.getProperty(KEY_2), is(VALUE_FOR_RESOLVED_KEY));
	}
	
	@Test
	public void testAppNameOverrideableFromProps() throws Exception {
		BootstrapMain.withCustomPropertySupplier(new AbstractPropertySupplier() {
			@Override
			public Supplier<Map<String, String>> getSystemPropertiesSupplier() {
				Map<String, String> props = newHashMap();
				props.putAll(PropertyUtils.getSystemPropertyStrings());
				props.putAll(PROPS_MAP);
				props.put(BOOTSTRAP_APPLICATION_NAME_KEY, APP_NAME_2);
				return Suppliers.<Map<String, String>> ofInstance(ImmutableMap.copyOf(props));
			}
		}).withApplicationName(APP_NAME).prepareProperties();

		assertThat(System.getProperty(BOOTSTRAP_APPLICATION_NAME_KEY), is(APP_NAME_2));
	}
}
