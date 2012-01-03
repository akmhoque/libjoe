package joe.util.bootstrap;

/**
 * Standard {@link PropertyProvider}s and {@link PropertyProviderFactory} implementations. Consider using
 * static imports, as in:
 * 
 * <pre>
 * BootstrapMain.loadProperties().publishTo(systemProperties());
 * </pre>
 * 
 * @author Joe Kearney
 */
public final class PropertyProviderFactories {
	private PropertyProviderFactories() {}
	
	public static final PropertyProviderFactory<SystemPropertyProvider> systemProperties() {
		return SystemPropertyProvider.instance();
	}
	public static final PropertyProviderFactory<ThreadGroupPropertyProvider> threadGroup(ThreadGroup threadGroup) {
		return ThreadGroupPropertyProvider.forThreadGroup(threadGroup);
	}
	public static PropertyProviderFactory<MapBackedPropertyProvider> newMap() {
		return MapBackedPropertyProvider.factory();
	}
}
