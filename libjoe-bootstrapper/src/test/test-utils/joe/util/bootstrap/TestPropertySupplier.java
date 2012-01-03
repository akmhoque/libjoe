package joe.util.bootstrap;

import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_ENABLE_KEY;
import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_ENABLE_LOGGING_KEY;

import java.util.Map;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;

final class TestPropertySupplier extends
		AbstractPropertySupplier {
	private final Map<String, String> properties;
	
	public TestPropertySupplier() {
		this.properties = ImmutableMap.of();
	}
	public TestPropertySupplier(Map<String, String> properties) {
		this.properties = properties;
	}

	@Override
	public Supplier<Map<String, String>> getSystemPropertiesSupplier() {
		return Suppliers.ofInstance(properties);
	}
	@Override
	public Iterable<Supplier<Map<String, String>>> getCommonPropertiesSupplier() {
		return suppliersForMap(ImmutableMap.of(BOOTSTRAP_ENABLE_KEY, "true", BOOTSTRAP_ENABLE_LOGGING_KEY, "true"));
	}
}