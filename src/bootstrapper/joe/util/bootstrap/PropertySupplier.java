package joe.util.bootstrap;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Supplier;

public interface PropertySupplier {
	Supplier<Map<String, String>> getSystemPropertiesSupplier();
	Collection<Supplier<Map<String, String>>> getUserPropertiesSuppliers();
	Supplier<Map<String, String>> getIdePropertiesSupplier();
	Supplier<Map<String, String>> getEnvironmentPropertiesSupplier();
}
