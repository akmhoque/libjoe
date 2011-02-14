package joe.util.bootstrap;

import java.util.Map;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Skeleton implementation of {@link PropertySupplier} that yields no properties.
 * 
 * @author Joe Kearney
 */
abstract class AbstractPropertySupplier implements PropertySupplier {
	private static final Map<String, String> EMPTY_STRING_MAP = ImmutableMap.of();
	private static final Supplier<Map<String, String>> EMPTY_STRING_MAP_SUPPLIER = Suppliers.ofInstance(EMPTY_STRING_MAP);
	
	@Override
	public Supplier<Map<String, String>> getSystemPropertiesSupplier() {
		return EMPTY_STRING_MAP_SUPPLIER;
	}
	@Override
	public Iterable<Supplier<Map<String, String>>> getUserPropertiesSuppliers() {
		return ImmutableList.of();
	}
	@Override
	public Iterable<Supplier<Map<String, String>>> getMachinePropertiesSupplier() {
		return ImmutableList.of();
	}
	@Override
	public Iterable<Supplier<Map<String, String>>> getIdePropertiesSupplier() {
		return ImmutableList.of();
	}
	@Override
	public Iterable<Supplier<Map<String, String>>> getEnvironmentPropertiesSupplier() {
		return ImmutableList.of();
	}
}
