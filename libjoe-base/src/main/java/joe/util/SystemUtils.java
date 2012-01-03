package joe.util;


import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SystemUtils {
	public static final String JAVA_PRESENT_WORKING_DIRECTORY_SYSPROP_KEY = "user.dir";
	public static final String JAVA_USER_NAME_SYSPROP_KEY = "user.name";
	public static final String JAVA_CLASS_PATH_SYSPROP_KEY = "java.class.path";
	public static final String JAVA_FILE_SEPARATOR_SYSPROP_KEY = "file.separator";
	public static final String JAVA_OS_NAME_SYSPROP_KEY = "os.name";

	public static final String HOST_NAME_KEY = "host.name";

	private static final int PID = doGetPid();
	/**
	 * Attempts to get the PID of this process, by inspecting the runtime management MX bean. This may not be supported
	 * on all systems, in which case this method throws {@link UnsupportedOperationException}.
	 * 
	 * @return the process ID, or {@code -1} if not available
	 */
	private static int doGetPid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		int index = name.indexOf('@');
		if (index < 0) {
			return -1; // unavailable
		}
		return Integer.parseInt(name.substring(0, index));
	}
	/**
	 * Gets the PID of this process, by inspecting the runtime management MX bean. This may not be supported
	 * on all systems, in which case this method throws {@link UnsupportedOperationException}.
	 * 
	 * @return the process ID
	 * @throws UnsupportedOperationException if not available
	 */
	public static int getPid() {
		if (PID == -1) {
			throw new UnsupportedOperationException();
		}
		return PID;
	}
	/**
	 * Attempts to get the host name of the machine on which this process is running, through
	 * {@link InetAddress#getLocalHost()}. This may not be supported on all systems, in which case this method throws
	 * {@link UnsupportedOperationException}.
	 * 
	 * @return the local host name
	 * @throws UnsupportedOperationException if not available
	 */
	public static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new UnsupportedOperationException("Host name not available (through InetAddress)", e);
		}
	}

	/**
	 * Attempts to determine the type of the operating system by parsing the value of the {@code os.name} system
	 * property.
	 * 
	 * @return type of the OS
	 * @throws IllegalArgumentException if the parameter cannot be parsed for any reason
	 */
	public static OperatingSystem getOperatingSystem() {
		return OperatingSystem.thisOperatingSystem();
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
