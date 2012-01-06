package joe.util.bootstrap;

import static joe.util.bootstrap.BootstrapMain.ADDITIONAL_PROPERTIES_GROUP_KEY;
import static joe.util.bootstrap.PropertyProviderFactories.newMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class BootstrapAdditionalPropertyGroupTest {
	@Test
	public void testSimple() throws Exception {
		PropertyProvider pp = BootstrapMain.withApplicationName("parent").withPropertyOverrides(
				ImmutableMap.of(ADDITIONAL_PROPERTIES_GROUP_KEY, "additional-test", BootstrapMain.BOOTSTRAP_ENABLE_KEY, "true")).publishTo(
				newMap());
		
		assertThat(pp.getProperty("additional-foo"), is("additional-bar"));
	}
}
