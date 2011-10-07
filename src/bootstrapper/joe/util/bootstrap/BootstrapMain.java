package joe.util.bootstrap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.collect.Maps.transformValues;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static joe.util.PropertyUtils.getSystemPropertyStrings;
import static joe.util.StringUtils.UNESCAPE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import joe.util.PropertyUtils;
import joe.util.StringUtils;
import joe.util.SystemUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

/**
 * A general application entry point that loads and merges properties from different files, based on username and other
 * properties. It can be invoked transparently: invocation without being explicitly enabled will have no effect other
 * than to delegate through to the bootstrapped application with no changes to the environment.
 * <p>
 * <h3>Why would you want to do this?</h3>
 * This library is designed to help manage using different property sets for different environments. For example, it supports different
 * runtime environments (production, development, others in between), specific properties for when running in an IDE, specific properties
 * for different users as well as for different machines and operating systems.
 * <p>
 * This gives flexibility in the property sets that are loaded into the runtime, whether in development or deployment. It also allows
 * changes to be made very cheaply, simply by changing system properties and restarting, rather than having to recompile code with new
 * values for constants.
 * <p>
 * <h3>Priority Ordering of Property Sets</h3>
 * With highest priority property sets first. Properties specified in a higher priority property set will override those defined in lower
 * priority property sets.
 * <p>
 * <ol>
 * <li><b>System properties</b> These override everything else.
 * <li><b>User properties</b> taken from files defined by {@code bootstrap.properties.user.file}. These often define the environment for the
 * running process, by setting the {@code bootstrap.environment} property.
 * <li><b>Machine properties</b> taken from files defined by {@code bootstrap.properties.machine.file}.
 * <li><b>Operating-system properties</b> taken from files defined by {@code bootstrap.properties.os.file}, defaulting to
 * {@code windows.properties} and {@code unix.properties}. For example, set templated paths to common shared file locations, which will be
 * used elsewhere. Consider {@code my.path.to.stuff=C:/foo} or {@code my.path.to.stuff=/mnt/foo}, for example.
 * <li><b>IDE properties</b> taken from files defined by {@code bootstrap.properties.ide.file}. This might set properties to turn on debug
 * modes, or turn off features like emails that should not be sent while debugging.
 * <li><b>Environment properties</b> taken from files defined by {@code bootstrap.properties.env.file}. This defines the environment, for
 * example the production/development database URLs, paths to environment-specific files etc.
 * <li><b>Common properties</b> taken from files defined by {@code bootstrap.properties.common.file}. Define here those properties that are
 * common to all environments, or sensible defaults.
 * </ol>
 * Note that between all property sets, properties are resolved where values contain keys to other properties. The syntax for this looks
 * like the following.
 * 
 * <pre>
 * some.property=some.value
 * my.template.property=abc.${some.property}
 * </pre>
 * 
 * Here, the value {@code my.template.property} will be resolved to {@code abc.some.value}.
 * <p>
 * Note, also, that these two lines can be defined in different places. The property resolution step happens very late in order to catch
 * this. As usual, higher priority properties take precedence when looking for the resolved values. Nested properties are supported in the
 * usual manner.
 * <p>
 * <h3>Usage</h3>
 * There are two usage patterns.
 * <ul>
 * <li />
 * <h4>Calling into {@code BootstrapMain} to configure properties</h4>
 * Call {@link BootstrapMain#prepareProperties()} from your main method. This will configure all properties in the environment, if
 * bootstrapping is enabled by such a property. Note that since properties will not be configured before this method is invoked, they will
 * not be visible from static initialisers of the main class.
 * <li />
 * <h4>{@code BootstrapMain} as a main class</h4>
 * Specify the application entry point class using the {@code bootstrap.main.class} property and the main method name using the
 * {@code bootstrap.main.method} property. These must be defined somewhere within the property hierarchy.
 * <p>
 * The application main thread will run within the scope of bootstrapper stack frames, and the main class will be this class. Note that
 * tools such as {@code jps} will report on this main class name, which may make it more difficult to disambiguate running bootstrapped
 * processes.
 * </ul>
 * <h3>Summary of Understood Properties</h3>
 * 
 * <table border>
 * <tr>
 * <th>Property</th>
 * <th>Behaviour</th>
 * <th>Default value</th>
 * <tr valign=top>
 * <tr align=left>
 * <td>{@code bootstrap.enable}
 * <td>{@code true} to enable bootstrapping behaviour. No changes will be made to the application environment without this property set.</td>
 * <td>{@code false}</td>
 * </tr>
 * <tr align=left>
 * <td>
 * {@code bootstrap.logging.enable}
 * <td>Turns on verbose logging of properties found and set.</td>
 * <td>{@code false}</td>
 * </tr>
 * <tr align=left>
 * <td> {@code bootstrap.logging.jul}
 * <td>Logs through {@code java.util.logging} instead of {@code System.out}.</td>
 * <td>{@code false} (uses {@code System.out})</td>
 * </tr>
 * <tr align=left>
 * <td> {@code bootstrap.environment}
 * <td>Specifies the name of the environment.</td>
 * <td>(none)</td>
 * </tr>
 * <tr align=left>
 * <td> {@code bootstrap.application.name}
 * <td>Specifies the name of the application. This is not used within the bootstrapper itself, but may be a useful discriminator in
 * resolving properties.</td>
 * <td>(none)</td>
 * </tr>
 * <tr align=left>
 * <td> {@code bootstrap.main.class}
 * <td>Specifies the name of the entry point class for the application. This is used only by the {@link #launchApplication()} family of
 * methods. If not set, then the bootstrapper will attempt to set this property to the detected main class, by scanning the stack trace of
 * the bootstrapping thread.</td>
 * <td>(none)</td>
 * </tr>
 * <tr align=left>
 * <td> {@code bootstrap.main.method}
 * <td>Specifies the name of the entry point method for the application. This is used only by the {@link #launchApplication()} family of
 * methods.</td>
 * <td>{@code main}</td>
 * </tr>
 * <tr align=left>
 * <td>{@code bootstrap.properties.root.dir}</td>
 * <td>Root directory in which to look for properties files. This may only be specified in system properties.</td>
 * <td>{@code config}</td>
 * </tr>
 * <tr align=left>
 * <td>{@code bootstrap.properties.user.file}</td>
 * <td>Comma separated list of file names, relative to the root directory specified by {@code bootstrap.properties.root.dir}, in which to
 * look for user properties files.</td>
 * <td>{@code <user_name>.properties,} {@code user.properties}</td>
 * </tr>
 * <tr align=left>
 * <td>{@code bootstrap.properties.machine.file}</td>
 * <td>File name, relative to the root directory specified by {@code bootstrap.properties.root.dir}, in which to look for a machine-specific
 * properties file.</td>
 * <td>{@code <hostname>.properties}, determined by {@code InetAddress.getLocalHost().getHostName()}</td>
 * </tr>
 * <tr align=left>
 * <td>{@code bootstrap.properties.os.file}</td>
 * <td>File name, relative to the root directory specified by {@code bootstrap.properties.root.dir}, in which to look for a OS-specific
 * properties file.</td>
 * <td>{@code windows.properties} or {@code unix.properties}, determined by parsing the system property {@code os.name}. See
 * {@link SystemUtils#getOperatingSystem()}</td>
 * </tr>
 * <tr align=left>
 * <td>{@code bootstrap.properties.ide.file}</td>
 * <td>File name, relative to the root directory specified by {@code bootstrap.properties.root.dir}, in which to look for an IDE properties
 * file. Note that if this file is present, its properties will be loaded. So to prevent these properties from being loaded into your
 * production runtime, either set {@code bootstrap.properties.ide.file} to empty or add a build step to remove this file from deployments.</td>
 * <td>{@code ide.properties}</td>
 * </tr>
 * <tr align=left>
 * <td>{@code bootstrap.properties.env.file}</td>
 * <td>File name, relative to the root directory specified by {@code bootstrap.properties.root.dir}, in which to look for a
 * environment-specific properties file.</td>
 * <td><tt> ${bootstrap.environment}.properties</tt></td>
 * </tr>
 * </tr>
 * <tr align=left>
 * <td>{@code bootstrap.properties.common.file}</td>
 * <td>File name, relative to the root directory specified by {@code bootstrap.properties.root.dir}, in which to look for a common
 * properties file.</td>
 * <td><tt> common.properties</tt></td>
 * </tr>
 * </tr>
 * </table>
 * <p>
 * 
 * @author Joe Kearney
 * @see #prepareProperties()
 * @see #withApplicationName(String)
 */
public final class BootstrapMain {
	/*
	 * Priority:
	 * * system properties
	 * * user properties (<user_name>.properties, user.properties)
	 * * machine.properties
	 * * os.properties
	 * * ide.properties
	 * * <environment>.properties
	 * * common.properties
	 */

	public static final String BOOTSTRAP_ENABLE_KEY = "bootstrap.enable";
	public static final String BOOTSTRAP_ENABLE_LOGGING_KEY = "bootstrap.logging.enable";
	public static final String BOOTSTRAP_ENABLE_JAVA_UTIL_LOGGING_KEY = "bootstrap.logging.jul";
	public static final String BOOTSTRAP_ENVIRONMENT_KEY = "bootstrap.environment";
	public static final String BOOTSTRAP_APPLICATION_NAME_KEY = "bootstrap.application.name";
	public static final String BOOTSTRAP_MAIN_METHOD_KEY = "bootstrap.main.method";
	public static final String BOOTSTRAP_MAIN_CLASS_KEY = "bootstrap.main.class";

	public static final String PROPERTIES_FILE_ROOT_LOCATIONS_KEY = "bootstrap.properties.root.dir";
	public static final String USER_PROPERTIES_FILE_LOCATIONS_OVERRIDE_KEY = "bootstrap.properties.user.file";
	public static final String MACHINE_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY = "bootstrap.properties.machine.file";
	public static final String OS_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY = "bootstrap.properties.os.file";
	public static final String IDE_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY = "bootstrap.properties.ide.file";
	public static final String ENVIRONMENT_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY = "bootstrap.properties.env.file";
	public static final String COMMON_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY = "bootstrap.properties.common.file";

	static final String PROPERTIES_FILE_ROOT_LOCATION_DEFAULT = "config";
	static final String USER_PROPERTIES_FILES_DEFAULT = SystemUtils.getUserName() + ".properties, user.properties";
	static final String MACHINE_PROPERTIES_FILE_DEFAULT = SystemUtils.getHostName() + ".properties";
	// windows.properties or unix.properties
	static final String OS_PROPERTIES_FILE_DEFAULT = SystemUtils.getOperatingSystem().toString().toLowerCase()
			+ ".properties";
	static final String IDE_PROPERTIES_FILE_DEFAULT = "ide.properties";
	static final String COMMON_PROPERTIES_FILE_DEFAULT = "common.properties";

	private Class<?> mainClass; // no default
	private String[] mainArgs = new String[0]; // default to no args
	private String applicationName; // no default
	private PropertySupplier propertySupplier = new DefaultPropertySupplier(this);

	/** the system properties to be provided to the application */
	private Map<String, String> applicationProperties = newTreeMap();
	/** logging support */
	private final BootstrapLogger logger = new BootstrapLogger(applicationProperties);
	/** path to the root of all config, defaulting to {@link #PROPERTIES_FILE_ROOT_LOCATION_DEFAULT} */
	private String rootPropertiesDirectory;

	BootstrapMain() {}

	final void setMainClass(Class<?> mainClass) {
		this.mainClass = mainClass;
	}
	final void setMainArgs(String ... mainArgs) {
		this.mainArgs = mainArgs;
	}
	final void setPropertySupplier(PropertySupplier propertySupplier) {
		this.propertySupplier = propertySupplier;
	}
	final void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * Specifies a custom property supplier for the application bootstrapper. Only use this if you want to override the
	 * default behaviour which reads from files determined by the current system property values. See the {@link BootstrapMain} class-level
	 * documentation for details of these.
	 * 
	 * @param propertySupplier the property supplier
	 * @return the builder
	 */
	public static BootstrapBuilder withCustomPropertySupplier(PropertySupplier propertySupplier) {
		return new BootstrapBuilder().withCustomPropertySupplier(propertySupplier);
	}
	/**
	 * Specifies main arguments for the application.
	 * 
	 * @param args program arguments
	 * @return the builder
	 */
	public static BootstrapBuilder withMainArgs(String ... args) {
		return new BootstrapBuilder().withMainArgs(args);
	}
	/**
	 * Sets an application name for this process, which will be subsequently accessible through the
	 * {@link BootstrapMain#BOOTSTRAP_APPLICATION_NAME_KEY} system property. If a value is already set in any other
	 * property, this value will not override it.
	 * 
	 * @param appName application name to set
	 * @return the builder
	 * @see BootstrapMain#BOOTSTRAP_APPLICATION_NAME_KEY
	 */
	public static BootstrapBuilder withApplicationName(String appName) {
		return new BootstrapBuilder().withApplicationName(appName);
	}

	/**
	 * Finds property sources for this application and builds the application's runtime property set, but does not
	 * publish the properties into the System properties map.
	 * 
	 * @return an object providing access to the state of the properties before and after bootstrapping
	 * @see #prepareProperties()
	 */
	public static BootstrapResult loadPropertiesForEnvironment(String bootstrapEnvironment) {
		return new BootstrapBuilder().loadPropertiesForEnvironment(bootstrapEnvironment);
	}
	/**
	 * Finds property sources for this application, builds the application's runtime property set and publishes them to
	 * the System property map.
	 * <p>
	 * This does all of the work of the bootstrapper except for actually launching the application, for which you should consider using
	 * {@link BootstrapMain#launchApplication(Class)} or the builder API through {@link BootstrapMain#withMainArgs(String...)}.
	 * 
	 * @return an object providing access to the state of the properties before and after bootstrapping
	 */
	public static BootstrapResult prepareProperties() {
		return new BootstrapBuilder().prepareProperties();
	}
	/**
	 * Launches the application with the given entry point and no main arguments.
	 * 
	 * @param mainClass entry point to the application
	 */
	public static void launchApplication(Class<?> mainClass) {
		checkNotNull(mainClass, "Entry point class type token may not be null");
		withMainArgs().launchApplication(mainClass);
	}
	/**
	 * Launches the application with entry point defined in properties and no main arguments.
	 * <p>
	 * <strong>Note:</strong> this launch method is applicable only when properties have been set to determine entry point method and class.
	 * 
	 * @see BootstrapMain#BOOTSTRAP_MAIN_CLASS_KEY
	 * @see BootstrapMain#BOOTSTRAP_MAIN_METHOD_KEY
	 */
	public static void launchApplication() {
		withMainArgs().launchApplication();
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
	/**
	 * Starts the application, loading the main class by name from the first of the supplied parameters, using the
	 * entire args list as the program args.
	 * 
	 * @param mainClassName name of the main class
	 * @param args program arguments
	 * @throws ClassNotFoundException if the class does not exist
	 */
	public static void main(String mainClassName, String ... args) throws ClassNotFoundException {
		withMainArgs(args).launchApplication(Class.forName(mainClassName));
	}
	/**
	 * Starts the application, loading the main class from the first of the supplied parameters, using the entire args
	 * list as the program args.
	 * 
	 * @param mainClass main class type token
	 * @param args program arguments
	 * @throws ClassNotFoundException if the class does not exist
	 */
	public static void main(Class<?> mainClass, String ... args) {
		withMainArgs(args).launchApplication(mainClass);
	}

	/**
	 * Builder class providing the fluent bootstrapping API. Start with the static methods on {@link BootstrapMain}.
	 * 
	 * @author Joe Kearney
	 */
	public static final class BootstrapBuilder {
		private final BootstrapMain bootstrapMain = new BootstrapMain();

		/**
		 * Prepares and launches the application by invoking the entry point method on the provided class. If the
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
		 * Prepares and launches the application by invoking the entry point method on the provided class. If the
		 * application is to be run in a separate classloader, the provided type token is used only to extract the class
		 * name.
		 * 
		 * @param mainClass entry point of the application to launch
		 */
		public void launchApplication(Class<?> mainClass) {
			checkNotNull(mainClass, "Entry point class type token may not be null");
			bootstrapMain.setMainClass(mainClass);
			bootstrapMain.preparePropertiesInternal(true);
			bootstrapMain.launch();
		}
		/**
		 * Prepares and launches the application by invoking the entry point method on the entry point class. If the
		 * application is to be run in a separate classloader, the provided type token is used only to extract the class
		 * name.
		 * <p>
		 * <strong>Note:</strong> this launch method is applicable only when properties have been set to determine entry point method and
		 * class.
		 * 
		 * @see BootstrapMain#BOOTSTRAP_MAIN_CLASS_KEY
		 * @see BootstrapMain#BOOTSTRAP_MAIN_METHOD_KEY
		 */
		public void launchApplication() {
			bootstrapMain.preparePropertiesInternal(true);
			bootstrapMain.launch();
		}
		/**
		 * Finds property sources for this application and builds the application's runtime property set.
		 * <p>
		 * This does all of the work of the bootstrapper except for actually launching the application, for which you
		 * should consider using {@link BootstrapMain#launchApplication(Class)} or the builder API through
		 * {@link BootstrapMain#withMainArgs(String...)}.
		 */
		public BootstrapResult prepareProperties() {
			return bootstrapMain.preparePropertiesInternal(true);
		}
		/**
		 * Finds property sources for this application and builds the application's runtime property set, but does not
		 * publish the properties into the System properties map.
		 * 
		 * @return an object providing access to the state of the properties before and after bootstrapping
		 * @see #prepareProperties()
		 */
		public BootstrapResult loadPropertiesForEnvironment(String bootstrapEnvironment) {
			bootstrapMain.applicationProperties.put(BOOTSTRAP_ENVIRONMENT_KEY, bootstrapEnvironment);
			bootstrapMain.logger.log("Loading properties for environment [" + bootstrapEnvironment + "]");
			return bootstrapMain.preparePropertiesInternal(false);
		}

		/**
		 * Specifies main arguments for the application.
		 * 
		 * @param args program arguments
		 * @return this builder
		 */
		public BootstrapBuilder withMainArgs(String ... args) {
			bootstrapMain.setMainArgs(checkNotNull(args, "Main argument list may not be null"));
			return this;
		}
		/**
		 * Specifies a custom property supplier for the application bootstrapper. Only use this if you want to override
		 * the default behaviour which reads from files determined by the current system property values. See the {@link BootstrapMain}
		 * class-level documentation for details of these.
		 * 
		 * @param propertySupplier the property supplier
		 * @return this builder
		 */
		public BootstrapBuilder withCustomPropertySupplier(PropertySupplier propertySupplier) {
			bootstrapMain.setPropertySupplier(checkNotNull(propertySupplier, "PropertySupplier may not be null"));
			return this;
		}
		/**
		 * Sets an application name for this process, which will be subsequently accessible through the
		 * {@link BootstrapMain#BOOTSTRAP_APPLICATION_NAME_KEY} system property. If a value is already set in any other
		 * property, this value will not override it.
		 * 
		 * @param appName application name to set
		 * @return this builder
		 * @see BootstrapMain#BOOTSTRAP_APPLICATION_NAME_KEY
		 */
		public BootstrapBuilder withApplicationName(String appName) {
			bootstrapMain.setApplicationName(checkNotNull(appName, "Application name may not be null"));
			return this;
		}
	}

	/**
	 * Finds property sources for this application and builds the application's runtime property set.
	 * <p>
	 * This does all of the work of the bootstrapper except for actually launching the application, for which you should consider using
	 * {@link #launchApplication(Class)} or the builder API through {@link #withMainArgs(String...)}.
	 * 
	 * @param publish TODO
	 * 
	 * @return an object providing access to the state of the properties before and after bootstrapping
	 */
	final BootstrapResult preparePropertiesInternal(boolean publish) {
		if (bootstrapResult != null) {
			logger.log("Bootstrapping has already been completed, and will not be re-run.");
			return bootstrapResult;
		}

		addComputedProperties();
		findRootConfigDirectory();
		generateProperties();
		return setSystemProperties(publish);
	}

	private static final MapJoiner MAP_JOINER = Joiner.on("\n  ").withKeyValueSeparator(" => ");
	private static final MapJoiner MAP_JOINER_INDENTED = Joiner.on("\n    ").withKeyValueSeparator(" => ");
	private void generateProperties() {
		if (!processPropertySupplier(propertySupplier.getSystemPropertiesSupplier())) {
			return;
		}
		if (!processPropertySupplierGroup(propertySupplier.getUserPropertiesSuppliers())) {
			return;
		}
		if (!processPropertySupplierGroup(propertySupplier.getMachinePropertiesSupplier())) {
			return;
		}
		if (!processPropertySupplierGroup(propertySupplier.getOsPropertiesSupplier())) {
			return;
		}
		if (!processPropertySupplierGroup(propertySupplier.getIdePropertiesSupplier())) {
			return;
		}
		if (!processPropertySupplierGroup(propertySupplier.getEnvironmentPropertiesSupplier())) {
			return;
		}
		if (!processPropertySupplierGroup(propertySupplier.getCommonPropertiesSupplier())) {
			return;
		}

		if (applicationName != null) {
			String alreadySetAppName = putIfAbsent(applicationProperties, BOOTSTRAP_APPLICATION_NAME_KEY,
					applicationName);
			if (alreadySetAppName != null) {
				logger.log(String.format(
						"Application name [%s] set programmatically was overridden from some property as [%s]",
						applicationName, alreadySetAppName));
			}
		}

		Map<String, String> resolvedProperties = PropertyUtils.resolvePropertiesInternally(applicationProperties);
		MapDifference<String, String> mapDifference = Maps.difference(transformValues(applicationProperties, UNESCAPE),
				transformValues(resolvedProperties, UNESCAPE));
		if (mapDifference.areEqual()) {
			logger.log("No properties found to resolve between property groups");
		} else {
			logger.log("Resolved property values between property groups as follows:\n  "
					+ MAP_JOINER.join(mapDifference.entriesDiffering()));
		}


		if (!resolvedProperties.containsKey(BOOTSTRAP_MAIN_CLASS_KEY)) {
			String mainClassName = detectMainClass();
			if (mainClassName != null) {
				resolvedProperties = ImmutableMap.<String, String> builder().putAll(resolvedProperties).put(
						BOOTSTRAP_MAIN_CLASS_KEY, mainClassName).build();
			}
		}

		applicationProperties = resolvedProperties;
	}
	private boolean processPropertySupplierGroup(Iterable<Supplier<Map<String, String>>> groupPropertiesSuppliers) {
		Iterator<Supplier<Map<String, String>>> propSuppliersIterator = groupPropertiesSuppliers.iterator();
		boolean allowContinue = true;
		while (allowContinue && propSuppliersIterator.hasNext()) {
			allowContinue &= processPropertySupplier(propSuppliersIterator.next());
		}
		return allowContinue;
	}
	private boolean processPropertySupplier(Supplier<Map<String, String>> propertySupplier) {
		logger.log("Loading properties from [" + propertySupplier.toString() + "]");
		Map<String, String> componentProperties = propertySupplier.get();

		putAllIfAbsent(applicationProperties, componentProperties);
		if (!componentProperties.isEmpty()) {
			logger.log("Loaded (non-overriding) properties from ["
					+ propertySupplier.toString()
					+ "]:\n  "
					+ MAP_JOINER.join(ImmutableSortedMap.copyOf(transformValues(componentProperties,
							StringUtils.UNESCAPE))));
		} else {
			logger.log("Loaded no properties from [" + propertySupplier.toString() + "]");
		}

		if (isBootstrappingDisabled()) {
			logger.log("Bootstrapping disabled by the " + BOOTSTRAP_ENABLE_KEY + " property supplied by ["
					+ propertySupplier.toString() + "]. To override, specify " + BOOTSTRAP_ENABLE_KEY
					+ "=true in this or a higher-priority property set.");
			return false;
		}
		return true;
	}

	private boolean isBootstrappingDisabled() {
		String prop = getApplicationProperty(BOOTSTRAP_ENABLE_KEY);
		return prop != null && !"true".equalsIgnoreCase(prop);
	}
	private boolean isBootstrappingEnabled() {
		return "true".equalsIgnoreCase(getApplicationProperty(BOOTSTRAP_ENABLE_KEY));
	}

	/** Properties computed by the bootstrapper and registered prior to  */
	@VisibleForTesting
	// TODO and others?
	static final Map<String, String> COMPUTED_PROPERTIES = ImmutableMap.of(SystemUtils.HOST_NAME_KEY, SystemUtils.getHostName());
	private void addComputedProperties() {
		putAllIfAbsent(applicationProperties, COMPUTED_PROPERTIES);
	}

	private BootstrapResult bootstrapResult = null;
	private BootstrapResult setSystemProperties(boolean publish) {
		final Map<String, String> priorSystemProperties = ImmutableMap.copyOf(getSystemPropertyStrings());
		if (isBootstrappingEnabled()) {
			logger.log("Setting application system properties");

			final MapDifference<String, String> difference = Maps.difference(priorSystemProperties,
					applicationProperties);
			if (publish) {
				System.getProperties().putAll(applicationProperties);
			}

			logger.log("Application system properties set");

			logger.log("Properties changed by the bootstrapper:"
					+ (difference.entriesDiffering().isEmpty() ? " (none)"
							: ("\n    "
									+ MAP_JOINER.join(ImmutableSortedMap.copyOf(difference.entriesDiffering())) + "\n")));
			logger.log("Properties added by the bootstrapper (not including system properties set by the launcher):"
					+ (difference.entriesOnlyOnRight().isEmpty() ? " (none)"
							: ("\n    "
									+ MAP_JOINER.join(ImmutableSortedMap.copyOf(difference.entriesOnlyOnRight())) + "\n")));

			logger.log("Running application with\n" //
					+ "  main class        ["
					+ (mainClass == null ? "not specified" : mainClass.getName())
					+ "]\n"
					+ "  main args         ["
					+ Joiner.on(", ").join(mainArgs)
					+ "]\n"
					+ "  system properties\n    "
					+ MAP_JOINER_INDENTED.join(ImmutableSortedMap.copyOf(transformValues(
							(publish ? PropertyUtils.getSystemPropertyStrings() : applicationProperties),
							StringUtils.UNESCAPE))) + "\n");

			return bootstrapResult = new BootstrapResult(priorSystemProperties, applicationProperties);
		} else {
			logger.log("Bootstrapping disabled, system properties will not be set for the application; "
					+ "it will be launched with no changes to its environment.");
			return bootstrapResult = new BootstrapResult(priorSystemProperties, priorSystemProperties);
		}
	}

	/**
	 * Looks up the root properties directory from the real system properties.
	 */
	private void findRootConfigDirectory() {
		String rootPropertiesDirectory = System.getProperty(PROPERTIES_FILE_ROOT_LOCATIONS_KEY,
				PROPERTIES_FILE_ROOT_LOCATION_DEFAULT);
		if (rootPropertiesDirectory != null) {
			this.rootPropertiesDirectory = rootPropertiesDirectory;
		}
	}

	final String createPropertyFileRelativePath(String fileName) {
		return rootPropertiesDirectory + SystemUtils.getFileSeparator() + fileName;
	}

	/*
	 * map utilities
	 */
	private static <K, V> void putAllIfAbsent(Map<? super K, ? super V> destination, Map<? extends K, ? extends V> source) {
		for (Entry<? extends K, ? extends V> entry : source.entrySet()) {
			putIfAbsent(destination, entry.getKey(), entry.getValue());
		}
	}
	/**
	 * Adds the key-value pair to the map if there is currently no mapping for that key.
	 * 
	 * @param destination the map
	 * @param key
	 * @param value
	 * @return the old value if there was one, or {@code null} if the new mapping was added to the map
	 */
	private static <K, V> V putIfAbsent(Map<K, V> destination, K key, V value) {
		V oldValue = destination.get(key);
		if (oldValue == null) {
			destination.put(key, value);
		}
		return oldValue;
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
		String value = getApplicationProperty(key);
		return value == null ? defaultValue : value;
	}

	/**
	 * Launches the application. After all properties have been set in the appropriate places, this does the reflective
	 * work of actually invoking the main method of the target application.
	 */
	private void launch() {
		final Class<?> mainClass = getMainClass();
		final Method mainMethod = getMainMethod(mainClass);

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
	private Class<?> getMainClass() {
		/*
		 * main class set explicitly takes precedence.
		 */
		if (mainClass != null) {
			return mainClass;
		}

		/*
		 * Then look for main class name specified in properties
		 */
		String mainClassNameFromProperties = getApplicationProperty(BOOTSTRAP_MAIN_CLASS_KEY);
		if (mainClassNameFromProperties != null) {
			try {
				Class<?> mainClassFromProperties = Class.forName(mainClassNameFromProperties);
				setMainClass(mainClassFromProperties);
				return mainClassFromProperties;
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Bad class name [" + mainClassNameFromProperties
						+ "] specified in property [" + BOOTSTRAP_MAIN_CLASS_KEY + "]", e);
			}
		}

		/*
		 * finally break, there's no main class specified.
		 */
		throw new NullPointerException("Main class entry point for the application must be set");
	}
	private Method getMainMethod(Class<?> mainClass) {
		String methodName = getApplicationProperty(BOOTSTRAP_MAIN_METHOD_KEY, "main");

		Method mainMethod;
		try {
			/*
			 * JLS 3 12.1.4 defines a main method as one of the following:
			 * public static void main(String[])
			 * public static void main(String ...)
			 */

			mainMethod = mainClass.getMethod(methodName, String[].class);
			int mainMethodModifiers = mainMethod.getModifiers();
			checkArgument(isPublic(mainMethodModifiers), "main method must be public");
			checkArgument(isStatic(mainMethodModifiers), "main method must be static");
			checkArgument(Void.TYPE.isAssignableFrom(mainMethod.getReturnType()),
					"main method must have void return type");
			checkArgument(mainMethod.getParameterTypes().length == 1, "main method must take one String array argument");
			checkArgument(mainMethod.getParameterTypes()[0].isArray(),
					"main method must take one String array argument");
			checkArgument(mainMethod.getParameterTypes()[0].getComponentType() == String.class,
					"main method must take one String array argument");
		} catch (SecurityException e) {
			throw new IllegalStateException("BootstrapMain must have permissions to find the main method reflectively",
					e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Class to be bootstrapped does not have a public static void "
					+ methodName + "(String[]) method", e);
		}
		return mainMethod;
	}
	/**
	 * Attempts to infer the name of the main class, the entry point in this process. This will work only if the current
	 * thread is the main thread. If the current thread is any other thread, or if the thread's stack trace is
	 * unavailable for any reason, {@code null} will be returned.
	 * 
	 * @return the name of the main class, or {@code null} if failed to infer the main class
	 */
	private static String detectMainClass() {
		Thread currentThread = Thread.currentThread();
		if ("main".equals(currentThread.getName())) {
			StackTraceElement[] stackTrace = currentThread.getStackTrace();
			if (stackTrace.length == 0) {
				return null;
			} else {
				return stackTrace[stackTrace.length - 1].getClassName();
			}
		} else {
			return null;
		}
	}
}
