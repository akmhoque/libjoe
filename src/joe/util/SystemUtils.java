package joe.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class SystemUtils {
	public static final String JAVA_PRESENT_WORKING_DIRECTORY_SYSPROP_KEY = "user.dir";
	public static final String JAVA_USER_NAME_SYSPROP_KEY = "user.name";
	public static final String JAVA_CLASS_PATH_SYSPROP_KEY = "java.class.path";
	public static final String JAVA_FILE_SEPARATOR_SYSPROP_KEY = "file.separator";
	
	/**
	 * Attempts to get the PID of this process, by inspecting the runtime management MX bean. This may not be supported
	 * on all systems, in which case this method throws {@link UnsupportedOperationException}.
	 * 
	 * @return the process ID
	 */
	public static int getPid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		int index = name.indexOf('@');
		if (index < 0) {
			throw new UnsupportedOperationException(); // we expect this to be consistent in the lifetime of the JVM
		}
		return Integer.parseInt(name.substring(0, index));
	}

	/**
	 * Gets a more-or-less unique name for this JVM, typically based on the process ID and machine name, though this is
	 * platform-specific.
	 * <p>
	 * This is equivalent to {@code ManagementFactory.getRuntimeMXBean().getName()}.
	 * 
	 * @return the JVM name
	 * @see RuntimeMXBean#getName()
	 */
	public static String getJvmName() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}

	public static String getPresentWorkingDirectory() {
		return System.getProperty(JAVA_PRESENT_WORKING_DIRECTORY_SYSPROP_KEY);
	}
	
	public static String getUserName() {
		return System.getProperty(JAVA_USER_NAME_SYSPROP_KEY);
	}
	
	/**
	 * Gets the separater between components of a filename. On Windows this is '\', on Unix '/'.
	 * 
	 * @return the platform file separator
	 */
	public static String getFileSeparator() {
		return System.getProperty(JAVA_FILE_SEPARATOR_SYSPROP_KEY);
	}
}
