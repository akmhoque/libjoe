package joe.util;

import static joe.util.SystemUtils.JAVA_OS_NAME_SYSPROP_KEY;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Enumeration of operating system types, intended to be used to determine platform-specific behaviour.
 * 
 * @author Joe Kearney
 */
public enum OperatingSystem {
	WINDOWS, UNIX;

	/** The value of the system property {@code os.name} at initialisation time of this class. */
	public static final String THIS_OS_NAME = System.getProperty(JAVA_OS_NAME_SYSPROP_KEY);
	
	private static final Set<String> UNIX_OS_NAMES = ImmutableSet.of("Linux", "Solaris", "SunOS");

	/**
	 * Attempts to parse the type of the operating system from the supplied {@code os.name} string.
	 * 
	 * @param osNameProperty string to parse
	 * @return type of the OS
	 * @throws IllegalArgumentException if the parameter cannot be parsed for any reason
	 */
	public static OperatingSystem parse(String osNameProperty) {
		if (osNameProperty.contains("Windows")) {
			return WINDOWS;
		} else {
			for (String name : UNIX_OS_NAMES) {
				if (osNameProperty.contains(name)) {
					return UNIX;
				}
			}
		}

		throw new IllegalArgumentException("Unknown os.name property value: " + osNameProperty);
	}
	/**
	 * Attempts to determine the type of the operating system by parsing the value of the {@code os.name} system
	 * property.
	 * 
	 * @return type of the OS
	 * @throws IllegalArgumentException if the {@code os.nama} cannot be parsed for any reason
	 */
	public static OperatingSystem thisOperatingSystem() throws IllegalArgumentException {
		return parse(THIS_OS_NAME);
	}
}
