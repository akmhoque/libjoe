package joe.util.bootstrap;

import java.util.Collections;
import java.util.Map;

/**
 * {@link PropertyProvider} around a map of String properties.
 * 
 * @author Joe Kearney
 */
public final class MapBackedPropertyProvider extends AbstractPropertyProvider {
	public static MapBackedPropertyProvider forMap(Map<String, String> propertyMap) {
		return new MapBackedPropertyProvider(propertyMap);
	}

	private final Map<String, String> propertyMap;
	private MapBackedPropertyProvider(Map<String, String> propertyMap) {
		this.propertyMap = propertyMap;
	}

	@Override
	public String getProperty(String key) {
		return propertyMap.get(key);
	}

	@Override
	protected Map<? super String, ? super String> asMutableMap() {
		return propertyMap;
	}
	/**
	 * Gets an unmodifiable map view of the properties in this provider.
	 * 
	 * @return map of properties
	 */
	public Map<String, String> asMap() {
		return Collections.unmodifiableMap(propertyMap);
	}

	public static PropertyProviderFactory<MapBackedPropertyProvider> factory() {
		return new PropertyProviderFactory<MapBackedPropertyProvider>() {
			@Override
			public MapBackedPropertyProvider providerFor(BootstrapResult bootstrapResult) {
				return MapBackedPropertyProvider.forMap(bootstrapResult.getPublishedSystemProperties());
			}
		};
	}
}
