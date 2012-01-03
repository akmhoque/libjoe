package joe.util.bootstrap;

import static joe.util.bootstrap.BootstrapMain.BOOTSTRAP_APPLICATION_NAME_KEY;
import static joe.util.bootstrap.PropertyProviderFactories.threadGroup;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.junit.Test;


public class ThreadGroupPropertyProviderTest {
	@Test
	public void testSimple() throws Exception {
		final Thread currentThread = Thread.currentThread();
		final ThreadGroup parentGroup = currentThread.getThreadGroup();
		
		BootstrapMain.withApplicationName("parent").withCustomPropertySupplier(new TestPropertySupplier()).loadPropertiesForEnvironment("env")
				.publishTo(ThreadGroupPropertyProvider.forThreadGroup(parentGroup));

		PropertyProvider propertyProvider = ThreadGroupPropertyProvider.instance();
		assertAppNameCorrect(propertyProvider, "parent");
	}
	void assertAppNameCorrect(PropertyProvider propertyProvider, String expected) {
		assertThat(propertyProvider.getProperty(BOOTSTRAP_APPLICATION_NAME_KEY), is(expected));
	}
	@Test
	public void testChildThread() throws Exception {
		final Thread currentThread = Thread.currentThread();
		final ThreadGroup parentGroup = currentThread.getThreadGroup();
		final ThreadGroup childGroup = new ThreadGroup(parentGroup, "subGroup");
		
		TestPropertySupplier tps = new TestPropertySupplier();
		
		BootstrapMain.withApplicationName("parent").withCustomPropertySupplier(tps).loadPropertiesForEnvironment("env")
				.publishTo(threadGroup(parentGroup));
		BootstrapMain.withApplicationName("child").withCustomPropertySupplier(tps).loadPropertiesForEnvironment("env")
				.publishTo(threadGroup(childGroup));
		
		ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(childGroup, r, "child-thread");
			}
		});
		try {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					assertAppNameCorrect(ThreadGroupPropertyProvider.instance(), "child");
				}
			}).get();
		} finally {
			executor.shutdownNow();
		}
	}
}
