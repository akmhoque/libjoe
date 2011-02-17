package joe.util.bootstrap;

import java.util.Map;

import com.google.common.base.Supplier;

/**
 * Abstraction over property generators. Properties belong to a property group, and property groups have a priority
 * ordering. In descending priority, these are:
 * 
 * <ol>
 * <li>System properties
 * <li>User properties
 * <li>Machine-local properties
 * <li>IDE properties
 * <li>Environment properties (dev, prod, ...)
 * </ol>
 * 
 * @author Joe Kearney
 */
public interface PropertySupplier {
	Supplier<Map<String, String>> getSystemPropertiesSupplier();
	Iterable<Supplier<Map<String, String>>> getUserPropertiesSuppliers();
	Iterable<Supplier<Map<String, String>>> getMachinePropertiesSupplier();
	Iterable<Supplier<Map<String, String>>> getIdePropertiesSupplier();
	Iterable<Supplier<Map<String, String>>> getEnvironmentPropertiesSupplier();
	Iterable<Supplier<Map<String, String>>> getCommonPropertiesSupplier();
}
