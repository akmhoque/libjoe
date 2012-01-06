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
	
	/**
	 * Publishes properties to the System properties map.
	 * 
	 * @see SystemPropertyProvider
	 * @see System#getProperty(String)
	 */
	public static final PropertyProviderFactory<SystemPropertyProvider> systemProperties() {
		return SystemPropertyProvider.instance();
	}
	/**
	 * Publishes properties visible in the context of the specified {@link ThreadGroup} through the {@link ThreadGroupPropertyProvider}.
	 * 
	 * @param threadGroup thread group from which these properties should be visible
	 */
	public static final PropertyProviderFactory<ThreadGroupPropertyProvider> threadGroup(ThreadGroup threadGroup) {
		return ThreadGroupPropertyProvider.forThreadGroup(threadGroup);
	}
	/**
	 * Publishes properties to a fresh {@link MapBackedPropertyProvider}. This factory has no externally visible side-effects.
	 */
	public static PropertyProviderFactory<MapBackedPropertyProvider> newMap() {
		return MapBackedPropertyProvider.factory();
	}
}
