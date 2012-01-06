package joe.util.bootstrap;

import java.util.Map;

import com.google.common.base.Supplier;

/**
 * Abstraction over property generators. Properties belong to a property group, and property groups have a priority
 * ordering. See class documentation in {@link BootstrapMain} for details.
 * 
 * @author Joe Kearney
 * @see BootstrapMain
 */
public interface PropertySupplier {
	Supplier<Map<String, String>> getSystemPropertiesSupplier();
	Iterable<Supplier<Map<String, String>>> getUserPropertiesSuppliers();
	Iterable<Supplier<Map<String, String>>> getMachinePropertiesSupplier();
	Iterable<Supplier<Map<String, String>>> getOsPropertiesSupplier();
	Iterable<Supplier<Map<String, String>>> getIdePropertiesSupplier();
	Iterable<Supplier<Map<String, String>>> getAdditionalPropertiesSupplier();
	Iterable<Supplier<Map<String, String>>> getEnvironmentPropertiesSupplier();
	Iterable<Supplier<Map<String, String>>> getCommonPropertiesSupplier();
}
