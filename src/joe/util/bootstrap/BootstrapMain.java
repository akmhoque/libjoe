package joe.util.bootstrap;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Throwables.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static java.lang.reflect.Modifier.*;
import static joe.util.PropertyUtils.*;
import static joe.util.bootstrap.BootstrappedEntryPoint.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import joe.util.PropertyUtils;
import joe.util.SystemUtils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * TODO usage
 * <p>
 * 
 * 
 * @author Joe Kearney
 */
public final class BootstrapMain {
	/*
	 * Priority:
	 * * system properties
	 * * user properties (<user_name>.properties, user.properties)
	 * * ide.properties
	 * * <environment>.properties
	 */

	public static final String BOOTSTRAP_NO_LOGGING_KEY = "bootstrap.logging.disable";

	public static final String PROPERTIES_FILE_ROOT_LOCATIONS_KEY = "bootstrap.properties.root.dir";
	public static final String USER_PROPERTIES_FILE_LOCATIONS_OVERRIDE_KEY = "bootstrap.properties.user.file";
	public static final String IDE_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY = "bootstrap.properties.ide.file";
	public static final String ENVIRONMENT_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY = "bootstrap.properties.env.file";

	static final String PROPERTIES_FILE_ROOT_LOCATION_DEFAULT = "config";
	static final String USER_PROPERTIES_FILES_DEFAULT = SystemUtils.getUserName() + ".properties, user.properties";
	static final String IDE_PROPERTIES_FILE_DEFAULT = "ide.properties";

	private Class<?> mainClass;
	private String[] mainArgs = new String[0];
	private PropertySupplier propertySupplier = new DefaultPropertySupplier();

	/** the system properties to be provided to the application */
	private final Map<String, String> applicationProperties = newTreeMap();
	/**
	 * List of suppliers of property maps, where each supplier may depend indirectly on the results of previous
	 * suppliers by way of the the {@link #applicationProperties} map.
	 */
	private final List<Supplier<Map<String, String>>> rawPropertiesReferenceList = newLinkedList();
	/** ordered list of the component properties sets to be provided to the application */
	private final List<Map<String, String>> applicationPropertiesComponents = newLinkedList();

	/** path to the root of all config, defaulting to {@link #PROPERTIES_FILE_ROOT_LOCATION_DEFAULT} */
	private String rootPropertiesDirectory = PROPERTIES_FILE_ROOT_LOCATION_DEFAULT;

	final void setMainClass(Class<?> mainClass) {
		this.mainClass = mainClass;
	}
	final void setMainArgs(String ... mainArgs) {
		this.mainArgs = mainArgs;
	}
	final void setPropertySupplier(PropertySupplier propertySupplier) {
		this.propertySupplier = propertySupplier;
	}

	public static final BootstrapBuilder withCustomPropertySupplier(PropertySupplier propertySupplier) {
		return new BootstrapBuilder().withCustomPropertySupplier(propertySupplier);
	}

	public static final BootstrapBuilder withMainArgs(String ... args) {
		return new BootstrapBuilder().withMainArgs(args);
	}
	/**
	 * Launches the application with the given entry point and no main arguments.
	 * 
	 * @param mainClass entry point to the application
	 */
	public static final void launchApplication(Class<?> mainClass) {
		checkNotNull(mainClass, "Entry point class type token may not be null");
		withMainArgs().launchApplication(mainClass);
	}

	/**
	 * Starts the application, loading the main class by name from the first of the supplied arguments.
	 * 
	 * @param args the name of the main class to be executed followed by the main arguments
	 * @throws ClassNotFoundException if the class does not exist
	 */
	public static void main(String ... args) throws ClassNotFoundException {
		checkArgument(args.length > 0, "Usage: " + BootstrapMain.class.getName()
				+ " <app_main_class> [<app_main_args> ...]");
		String mainClassName = args[0];
		String[] applicationArgs = Arrays.copyOfRange(args, 1, args.length, String[].class);
		main(mainClassName, applicationArgs);
	}
	public static void main(String mainClassName, String ... args) throws ClassNotFoundException {
		withMainArgs(args).launchApplication(Class.forName(mainClassName));
	}
	public static void main(Class<?> mainClass, String ... args) {
		withMainArgs(args).launchApplication(mainClass);
	}
	/**
	 * Launches the application, using as the entry point the class calling this method. Classes calling this method
	 * must implement the {@link BootstrappedEntryPoint} market interface, and implement the {@code bootstrapMain} entry
	 * point method.
	 * 
	 * @param args main arguments
	 */
	public static void launchCurrentClass(String ... args) {
		// find name of caller class
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		if (stackTrace == null || stackTrace.length < 3) {
			throw new IllegalStateException("Could not obtain valid stack trace to reflectively find main class");
		}
		final String mainClassName = stackTrace[2].getClassName();

		// check caller class implements BootstrappedEntryPoint, otherwise we StackOverflow through main()
		final Class<?> mainClass;
		try {
			mainClass = Class.forName(mainClassName);
		} catch (ClassNotFoundException e) {
			AssertionError ae = new AssertionError(
					"Couldn't find type token for a type name that came from a stack trace");
			ae.initCause(e);
			throw ae;
		}
		checkState(BootstrappedEntryPoint.class.isAssignableFrom(mainClass),
				"Classes calling #launchCurrentClass() must implement %s", BootstrappedEntryPoint.class);

		main(mainClass, args);
	}

	public static final class BootstrapBuilder {
		private final BootstrapMain bootstrapMain = new BootstrapMain();

		/**
		 * Prepares and launches the application by invoking the {@code main} method on the provided class. If the
		 * application is to be run in a separate classloader, the provided type token is used only to extract the class
		 * name.
		 * 
		 * @param mainClassName entry point of the application to launch
		 * @throws ClassNotFoundException
		 */
		public void launchApplication(String mainClassName) throws ClassNotFoundException {
			launchApplication(Class.forName(mainClassName));
		}
		/**
		 * Prepares and launches the application by invoking the {@code main} method on the provided class. If the
		 * application is to be run in a separate classloader, the provided type token is used only to extract the class
		 * name.
		 * 
		 * @param mainClass entry point of the application to launch
		 */
		public void launchApplication(Class<?> mainClass) {
			checkNotNull(mainClass, "Entry point class type token may not be null");
			bootstrapMain.setMainClass(mainClass);
			bootstrapMain.preparePropertiesAndLaunch();
		}
		public BootstrapBuilder withMainArgs(String ... args) {
			bootstrapMain.setMainArgs(checkNotNull(args, "Main argument list may not be null"));
			return this;
		}
		public BootstrapBuilder withCustomPropertySupplier(PropertySupplier propertySupplier) {
			bootstrapMain.setPropertySupplier(checkNotNull(propertySupplier, "PropertySupplier may not be null"));
			return this;
		}
	}

	final void preparePropertiesAndLaunch() {
		findRootConfigDirectory();
		generatePropertiesReferenceList();
		generateProperties();
		setSystemProperties();

		if (isLoggingEnabled()) {
			MapJoiner joiner = Joiner.on("\n    ").withKeyValueSeparator(" => ");
			log("Running application with ");
			log("  main class        [" + mainClass.getName() + "]");
			log("  main args         [" + Joiner.on(", ").join(mainArgs) + "]");
			log("  system properties\n    " + joiner.join(ImmutableSortedMap.copyOf(System.getProperties())));
		}

		launch();
	}

	void log(String string) {
		if (isLoggingEnabled()) {
			System.out.println(string);
		}
	}
	private boolean isLoggingEnabled() {
		return !("true".equalsIgnoreCase(applicationProperties.get(BOOTSTRAP_NO_LOGGING_KEY)) || Boolean.getBoolean(BOOTSTRAP_NO_LOGGING_KEY));
	}

	private void generatePropertiesReferenceList() {
		rawPropertiesReferenceList.add(propertySupplier.getSystemPropertiesSupplier());
		rawPropertiesReferenceList.addAll(propertySupplier.getUserPropertiesSuppliers());
		rawPropertiesReferenceList.add(propertySupplier.getIdePropertiesSupplier());
		rawPropertiesReferenceList.add(propertySupplier.getEnvironmentPropertiesSupplier());
	}
	private void generateProperties() {
		for (Supplier<Map<String, String>> propertiesSupplier : rawPropertiesReferenceList) {
			Map<String, String> componentProperties = propertiesSupplier.get();
			applicationPropertiesComponents.add(componentProperties);

			componentProperties = Maps.transformValues(componentProperties,
					propertyResolverFromMap(applicationProperties));
			componentProperties = Maps.transformValues(componentProperties,
					propertyResolverFromMap(applicationProperties));
			putAllIfAbsent(applicationProperties, componentProperties);
		}
	}

	private void setSystemProperties() {
		System.getProperties().putAll(applicationProperties);
	}

	/**
	 * Looks up the root properties directory from the real system properties.
	 */
	private void findRootConfigDirectory() {
		String rootPropertiesDirectory = getSystemProperty(PROPERTIES_FILE_ROOT_LOCATIONS_KEY);
		if (rootPropertiesDirectory != null) {
			this.rootPropertiesDirectory = rootPropertiesDirectory;
		}
	}

	final String createPropertyFileRelativePath(String fileName) {
		return rootPropertiesDirectory + SystemUtils.getFileSeparator() + fileName;
	}

	/*
	 * default property supplier
	 */
	final class DefaultPropertySupplier implements PropertySupplier {
		DefaultPropertySupplier() {}

		@Override
		public Supplier<Map<String, String>> getEnvironmentPropertiesSupplier() {
			return new Supplier<Map<String, String>>() {
				@Override
				public Map<String, String> get() {
					try {
						String envPropsFile = getApplicationProperty(ENVIRONMENT_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY);
						return loadPropertiesFileIfExists(createPropertyFileRelativePath(envPropsFile));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				@Override
				public String toString() {
					return "environment properties supplier";
				}
			};
		}
		@Override
		public Supplier<Map<String, String>> getIdePropertiesSupplier() {
			return new Supplier<Map<String, String>>() {
				@Override
				public Map<String, String> get() {
					try {
						String idePropsFileName = getApplicationProperty(IDE_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY,
								IDE_PROPERTIES_FILE_DEFAULT);
						return loadPropertiesFileIfExists(createPropertyFileRelativePath(idePropsFileName));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				@Override
				public String toString() {
					return "ide properties supplier";
				}
			};
		}
		@Override
		public Collection<Supplier<Map<String, String>>> getUserPropertiesSuppliers() {
			String userPropertyLocationsString = getApplicationProperty(USER_PROPERTIES_FILE_LOCATIONS_OVERRIDE_KEY,
					USER_PROPERTIES_FILES_DEFAULT);

			final Iterable<String> userPropertyLocations = Splitter.on(',').trimResults().omitEmptyStrings().split(
					userPropertyLocationsString);

			return ImmutableList.copyOf(Iterables.transform(userPropertyLocations,
					new Function<String, Supplier<Map<String, String>>>() {
						@Override
						public Supplier<Map<String, String>> apply(final String fileName) {
							return new Supplier<Map<String, String>>() {
								@Override
								public Map<String, String> get() {
									try {
										return loadPropertiesFileIfExists(createPropertyFileRelativePath(fileName));
									} catch (IOException e) {
										throw propagate(e);
									}
								}
								@Override
								public String toString() {
									return "user properties supplier: " + fileName;
								}
							};
						}
					}));
		}
		@Override
		public Supplier<Map<String, String>> getSystemPropertiesSupplier() {
			return new Supplier<Map<String, String>>() {
				@Override
				public Map<String, String> get() {
					return ImmutableMap.copyOf(PropertyUtils.getSystemPropertyStrings());
				}
				@Override
				public String toString() {
					return "SystemProperties supplier";
				}
			};
		}
	}

	/*
	 * map utilities
	 */
	private static <K, V> void putAllIfAbsent(Map<K, V> destination, Map<? extends K, ? extends V> source) {
		for (Entry<? extends K, ? extends V> entry : source.entrySet()) {
			K key = entry.getKey();
			if (!destination.containsKey(key)) {
				destination.put(key, entry.getValue());
			}
		}
	}
	/**
	 * Gets a "system property" from the set of properties already set for the application.
	 */
	final String getApplicationProperty(String key) {
		return applicationProperties.get(key);
	}
	/**
	 * Gets a "system property" from the set of properties already set for the application, or the default if no such
	 * value is present.
	 */
	final String getApplicationProperty(String key, String defaultValue) {
		String string = getApplicationProperty(key);
		return string == null ? defaultValue : key;
	}
	/**
	 * Gets a system property from the real sysprop map.
	 */
	final String getSystemProperty(String key) {
		return System.getProperty(key);
	}

	/**
	 * Launches the application. After all properties have been set in the appropriate places, this does the reflective
	 * work or actually invoking the main method of the target application.
	 */
	private void launch() {
		checkNotNull(mainClass, "Main class entry point for the application must be set");

		final Method mainMethod = getMainMethod();

		try {
			mainMethod.invoke(null, (Object) mainArgs); // new String[0] by default
		} catch (IllegalArgumentException e) {
			throw new AssertionError(e);
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (InvocationTargetException e) {
			throw new BootstrapException("Bootstrapped application main thread threw an exception", e.getCause());
		}
	}
	private Method getMainMethod() {
		String methodName = BootstrappedEntryPoint.class.isAssignableFrom(mainClass) ? BOOTSTRAP_MAIN_METHOD_NAME
				: "main";

		Method mainMethod;
		try {
			mainMethod = mainClass.getMethod(methodName, String[].class);
			int mainMethodModifiers = mainMethod.getModifiers();
			checkArgument(isStatic(mainMethodModifiers), "main method must be static");
			checkArgument(isPublic(mainMethodModifiers), "main method must be public"); // TODO is this true?
			checkArgument(Void.TYPE.isAssignableFrom(mainMethod.getReturnType()),
					"main method must have void return type");
		} catch (SecurityException e) {
			throw new RuntimeException("BootstrapMain must have permissions to reflectively find the main method", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Class to be bootstrapped does not have a public static void " + methodName
					+ "(String[]) method", e);
		}
		return mainMethod;
	}
}
