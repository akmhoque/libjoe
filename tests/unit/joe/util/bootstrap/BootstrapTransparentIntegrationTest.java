package joe.util.bootstrap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.TimeZone;

import joe.util.PropertyUtils;
import joe.util.SystemUtils;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class BootstrapTransparentIntegrationTest {
	static {
		// this sets user.timezone, which happens in the bootstrapper too. So we're not quite transparent, we see this
		// value change from empty to "Europe/London", for example.
		TimeZone.getDefault();
	}
	static Map<String, String> priorProperties;

	@Test
	public void test() throws Exception {
		final String userName = SystemUtils.getUserName();
		System.setProperty("user.name", "blah");
		try {
		System.clearProperty(BootstrapMain.BOOTSTRAP_ENABLE_KEY);
		priorProperties = ImmutableMap.copyOf(PropertyUtils.getSystemPropertyStrings());
			BootstrapMain.prepareProperties();
			Map<String, String> systemProperties = PropertyUtils.getSystemPropertyStrings();
			// but don't complain about the other ones added by the bootstrapper
			for (String computedPropertyKey : BootstrapMain.COMPUTED_PROPERTIES.keySet()) {
				systemProperties.remove(computedPropertyKey);
			}
			assertThat("Difference: " + Maps.difference(priorProperties, systemProperties), priorProperties,
					is(systemProperties));
		} finally {
			System.setProperty("user.name", userName);
			System.clearProperty(BootstrapMain.BOOTSTRAP_ENABLE_KEY);
			priorProperties = null;
		}
	}
}
