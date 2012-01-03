package joe.util.bootstrap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ThreadGroupPropertyProvider extends
		ForwardingPropertyProvider implements PropertyProviderFactory<ThreadGroupPropertyProvider> {
	private static final class ThreadGroupContextInjector implements
			PropertyProviderFactory<ThreadGroupPropertyProvider> {
		private final ThreadGroup threadGroup;

		private ThreadGroupContextInjector(ThreadGroup threadGroup) {
			this.threadGroup = threadGroup;
		}

		@Override
		public ThreadGroupPropertyProvider providerFor(BootstrapResult bootstrapResult) {
			MapBackedPropertyProvider contextPP = MapBackedPropertyProvider.forMap(bootstrapResult.getPublishedSystemProperties());
			map.put(threadGroup, contextPP);
			return instance();
		}
	}

	private static final ThreadGroupPropertyProvider INSTANCE = new ThreadGroupPropertyProvider();
	private static final Map<ThreadGroup, PropertyProvider> map = new ConcurrentHashMap<ThreadGroup, PropertyProvider>();

	private ThreadGroupPropertyProvider() {}
	
	public static ThreadGroupPropertyProvider instance() {
		return INSTANCE;
	}

	@Override
	protected PropertyProvider delegate() {
		// look for the most local one first
		final Thread currentThread = Thread.currentThread();
		ThreadGroup threadGroup = currentThread.getThreadGroup();
		while (threadGroup != null) {
			PropertyProvider ppForThreadGroup = map.get(threadGroup);
			if (ppForThreadGroup != null) {
				return ppForThreadGroup;
			}
		}
		return SystemPropertyProvider.instance();
	}
	
	@Override
	public ThreadGroupPropertyProvider providerFor(BootstrapResult bootstrapResult) {
		MapBackedPropertyProvider contextPP = MapBackedPropertyProvider.forMap(bootstrapResult.getPublishedSystemProperties());
		map.put(Thread.currentThread().getThreadGroup(), contextPP);
		return this;
	}

	/**
	 * Creates a {@code PropertyProviderFactory} that writes {@link BootstrapResult}s into the specified {@code ThreadGroup}
	 * for the {@link ThreadGroupPropertyProvider}.
	 * 
	 * @param threadGroup thread group into which to inject property context
	 * @return the provider factory
	 */
	public static PropertyProviderFactory<ThreadGroupPropertyProvider> forThreadGroup(final ThreadGroup threadGroup) {
		return new ThreadGroupContextInjector(threadGroup);
	}
}
