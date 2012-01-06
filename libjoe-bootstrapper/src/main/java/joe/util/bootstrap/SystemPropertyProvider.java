package joe.util.bootstrap;

import java.util.Map;

/**
 * {@link PropertyProvider} backed by the system properties. This provider uses System properties directly,
 * so any changes will be reflected in this provider.
 * 
 * @author Joe Kearney
 */
public final class SystemPropertyProvider extends AbstractPropertyProvider implements PropertyProviderFactory<SystemPropertyProvider> {
	private static final SystemPropertyProvider INSTANCE = new SystemPropertyProvider();
	
	@Override
	public String getProperty(String key) {
		return System.getProperty(key);
	}

	private SystemPropertyProvider() {}
	
	/**
	 * Gets the {@link SystemPropertyProvider}.
	 */
	public static SystemPropertyProvider instance() {
		return INSTANCE;
	}
	
	@Override
	public SystemPropertyProvider providerFor(BootstrapResult bootstrapResult) {
		standardPublish(bootstrapResult);
		return this;
	}
	@Override
	protected Map<? super String, ? super String> asMutableMap() {
		return System.getProperties();
	}
}
