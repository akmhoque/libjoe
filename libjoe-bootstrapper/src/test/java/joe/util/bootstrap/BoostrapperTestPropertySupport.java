package joe.util.bootstrap;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Map;

import joe.util.PropertyUtils;
import joe.util.SystemUtils;

import org.junit.After;
import org.junit.Before;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;

public abstract class BoostrapperTestPropertySupport {
	private Map<String, String> priorSystemProperties;

	@Before
	public void clearProps() {
		priorSystemProperties = ImmutableMap.copyOf(PropertyUtils.getSystemPropertyStrings());
		Collection<String> propsToKeep = newHashSet();
		propsToKeep.addAll(Collections2.filter(priorSystemProperties.keySet(), new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return input.startsWith("bootstrap.");
			}
		}));
		propsToKeep.add(SystemUtils.JAVA_OS_NAME_SYSPROP_KEY);
		propsToKeep.add(SystemUtils.JAVA_PRESENT_WORKING_DIRECTORY_SYSPROP_KEY);
		propsToKeep.add(SystemUtils.JAVA_USER_NAME_SYSPROP_KEY);
		propsToKeep.add(SystemUtils.JAVA_FILE_SEPARATOR_SYSPROP_KEY);
		
		System.getProperties().keySet().retainAll(propsToKeep);
	}
	@After
	public void reinstateProps() throws Throwable {
		System.getProperties().clear();
		System.getProperties().putAll(priorSystemProperties);
		priorSystemProperties = null;
	}
}
