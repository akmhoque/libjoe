package joe.util.bootstrap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link PropertyProvider} for which properties are determined by the current thread group. Think of this like slightly more fine-grained
 * system properties; if you run two logical applications in a single process and want them to see separate property contexts, you can
 * confine them to a parent thread group each and get properties through this provider.
 * <p>
 * Properties can be overridden in sub-thread-groups. When looking for a property, the current thread group will be checked first, followed
 * by parent thread groups in ascending order.
 * 
 * @author Joe Kearney
 */
public final class ThreadGroupPropertyProvider extends AbstractPropertyProvider implements
		PropertyProviderFactory<ThreadGroupPropertyProvider> {
	private static final ThreadGroupPropertyProvider INSTANCE = new ThreadGroupPropertyProvider();
	private static final Map<ThreadGroup, PropertyProvider> providersByThreadGroup = new ConcurrentHashMap<ThreadGroup, PropertyProvider>();
	
	private ThreadGroupPropertyProvider() {}
	
	/**
	 * Gets the thread group-context property provider.
	 */
	public static ThreadGroupPropertyProvider instance() {
		return INSTANCE;
	}
	
	@Override
	public String getProperty(String key) {
		// look for the most local one first
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
		while (threadGroup != null) {
			PropertyProvider ppForThreadGroup = providersByThreadGroup.get(threadGroup);
			if (ppForThreadGroup != null) {
				String property = ppForThreadGroup.getProperty(key);
				if (property != null) {
					return property;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public ThreadGroupPropertyProvider providerFor(BootstrapResult bootstrapResult) {
		MapBackedPropertyProvider contextPP = MapBackedPropertyProvider.forMap(bootstrapResult.getPublishedSystemProperties());
		providersByThreadGroup.put(Thread.currentThread().getThreadGroup(), contextPP);
		return this;
	}
	
	/**
	 * Creates a {@code PropertyProviderFactory} that writes {@link BootstrapResult}s into the specified {@code ThreadGroup} for the
	 * {@link ThreadGroupPropertyProvider}.
	 * 
	 * @param threadGroup thread group into which to inject property context
	 * @return the provider factory
	 */
	public static PropertyProviderFactory<ThreadGroupPropertyProvider> forThreadGroup(final ThreadGroup threadGroup) {
		return new PropertyProviderFactory<ThreadGroupPropertyProvider>() {
			@SuppressWarnings("synthetic-access")
			@Override
			public ThreadGroupPropertyProvider providerFor(BootstrapResult bootstrapResult) {
				MapBackedPropertyProvider contextPP = MapBackedPropertyProvider.forMap(bootstrapResult.getPublishedSystemProperties());
				providersByThreadGroup.put(threadGroup, contextPP);
				return instance();
			}
		};
	}
}
