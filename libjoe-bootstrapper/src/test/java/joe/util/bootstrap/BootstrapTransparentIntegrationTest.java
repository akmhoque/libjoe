package joe.util.bootstrap;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static joe.util.bootstrap.PropertyProviderFactories.newMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.TimeZone;

import joe.util.PropertyUtils;

import org.junit.Test;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class BootstrapTransparentIntegrationTest {
	static {
		// this sets user.timezone, which happens in the bootstrapper too. So we're not quite transparent, we see this
		// value change from empty to "Europe/London", for example.
		TimeZone.getDefault();
	}
	Map<String, String> priorProperties;

	@Test
	public void test() throws Exception {
		PropertySupplier tps = new AbstractPropertySupplier() {
			@Override
			public Iterable<Supplier<Map<String, String>>> getEnvironmentPropertiesSupplier() {
				return suppliersForMap(ImmutableMap.of("user.name", "blah"));
			}
		};

		priorProperties = newLinkedHashMap(PropertyUtils.getSystemPropertyStrings());
		MapBackedPropertyProvider pp = BootstrapMain.withCustomPropertySupplier(tps).loadProperties().publishTo(newMap());
		Map<String, String> publishedProperties = Maps.newLinkedHashMap(pp.asMap());

		// but don't complain about the other ones added by the bootstrapper
		for (String computedPropertyKey : BootstrapMain.COMPUTED_PROPERTIES.keySet()) {
			publishedProperties.remove(computedPropertyKey);
			priorProperties.remove(computedPropertyKey);
		}
		assertThat("Difference: " + Maps.difference(priorProperties, publishedProperties),
				priorProperties, is(publishedProperties));
	}
}
