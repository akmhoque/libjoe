package joe.util.bootstrap;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Throwables.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static java.lang.reflect.Modifier.*;
import static joe.util.PropertyUtils.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import joe.util.PropertyUtils;
import joe.util.SystemUtils;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

public final class BootstrapMain {
	/*
	 * Priority:
	 * * system properties
	 * * user properties (<user_name>.properties, user.properties)
	 * * ide.properties
	 * * <environment>.properties
	 */

	static final String PROPERTIES_FILE_ROOT_LOCATIONS_KEY = "bootstrap.properties.root.dir";
	static final String USER_PROPERTIES_FILE_LOCATIONS_OVERRIDE_KEY = "bootstrap.properties.user.file";
	static final String IDE_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY = "bootstrap.properties.ide.file";
	static final String ENVIRONMENT_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY = "bootstrap.properties.env.file";

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

	public static final class BootstrapBuilder {
		private BootstrapMain bootstrapMain = new BootstrapMain();

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

		launch();
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
			putAllIfAbsent(applicationProperties, componentProperties);
		}
	}
	private void setSystemProperties() {
		System.getProperties().clear();
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
	 * property suppliers
	 */
	private final class DefaultPropertySupplier implements PropertySupplier {
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

		Method mainMethod;
		try {
			mainMethod = mainClass.getMethod("main", String[].class);
			int mainMethodModifiers = mainMethod.getModifiers();
			checkArgument(isStatic(mainMethodModifiers), "main method must be static");
			checkArgument(isPublic(mainMethodModifiers), "main method must be public"); // TODO is this true?
			checkArgument(Void.TYPE.isAssignableFrom(mainMethod.getReturnType()),
					"main method must have void return type");
		} catch (SecurityException e) {
			throw new RuntimeException("BootstrapMain must have permissions to reflectively find the main method", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(
					"Class to be bootstrapped does not have a public static void main(String[]) method", e);
		}

		try {
			mainMethod.invoke(null, (Object) mainArgs);
		} catch (IllegalArgumentException e) {
			throw new AssertionError(e);
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (InvocationTargetException e) {
			throw new BootstrapException("Bootstrapped application main thread threw an exception", e.getCause());
		}
	}

}
