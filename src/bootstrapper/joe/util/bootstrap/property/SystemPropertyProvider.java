package joe.util.bootstrap.property;

import joe.util.PropertyUtils;

/**
 * {@link PropertyProvider} backed by the system properties. This provider uses a live view over system properties,
 * so any changes will be reflected in this provider.
 * 
 * @author Joe Kearney
 */
public final class SystemPropertyProvider extends MapBackedPropertyProvider {
	private static final PropertyProvider INSTANCE = new SystemPropertyProvider();

	private SystemPropertyProvider() {
		super(PropertyUtils.getSystemPropertyStrings());
	}
	
	public static PropertyProvider instance() {
		return INSTANCE;
	}
}
