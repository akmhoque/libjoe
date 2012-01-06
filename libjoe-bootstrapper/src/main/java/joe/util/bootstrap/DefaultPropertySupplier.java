package joe.util.bootstrap;

import static com.google.common.base.Throwables.propagate;
import static joe.util.PropertyUtils.loadPropertiesFileIfExists;
import static joe.util.bootstrap.BootstrapMain.ADDITIONAL_GROUP_NAME_TO_FILE_PROP_KEY;
import static joe.util.bootstrap.BootstrapMain.ADDITIONAL_PROPERTIES_GROUP_KEY;
import static joe.util.bootstrap.BootstrapMain.COMMON_PROPERTIES_FILE_DEFAULT;
import static joe.util.bootstrap.BootstrapMain.COMMON_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY;
import static joe.util.bootstrap.BootstrapMain.IDE_PROPERTIES_FILE_DEFAULT;
import static joe.util.bootstrap.BootstrapMain.IDE_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY;
import static joe.util.bootstrap.BootstrapMain.MACHINE_PROPERTIES_FILE_DEFAULT;
import static joe.util.bootstrap.BootstrapMain.MACHINE_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY;
import static joe.util.bootstrap.BootstrapMain.OS_PROPERTIES_FILE_DEFAULT;
import static joe.util.bootstrap.BootstrapMain.OS_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY;
import static joe.util.bootstrap.BootstrapMain.USER_PROPERTIES_FILES_DEFAULT;
import static joe.util.bootstrap.BootstrapMain.USER_PROPERTIES_FILE_LOCATIONS_OVERRIDE_KEY;

import java.io.IOException;
import java.util.Map;

import joe.util.PropertyUtils;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

class DefaultPropertySupplier implements PropertySupplier {
	private final BootstrapMain bootstrapper;

	DefaultPropertySupplier(BootstrapMain bootstrapMain) {
		bootstrapper = bootstrapMain;
	}

	@Override
	public Supplier<Map<String, String>> getSystemPropertiesSupplier() {
		return new Supplier<Map<String, String>>() {
			@Override
			public Map<String, String> get() {
				return PropertyUtils.getSystemPropertyStrings();
			}
			@Override
			public String toString() {
				return "SystemProperties supplier";
			}
		};
	}

	@Override
	public Iterable<Supplier<Map<String, String>>> getUserPropertiesSuppliers() {
		return fileBasedPropertyCollection("user properties supplier: %s", USER_PROPERTIES_FILE_LOCATIONS_OVERRIDE_KEY,
				USER_PROPERTIES_FILES_DEFAULT);
	}
	@Override
	public Iterable<Supplier<Map<String, String>>> getMachinePropertiesSupplier() {
		return fileBasedPropertyCollection("machine properties supplier: %s",
				MACHINE_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY, MACHINE_PROPERTIES_FILE_DEFAULT);
	}
	@Override
	public Iterable<Supplier<Map<String, String>>> getOsPropertiesSupplier() {
		return fileBasedPropertyCollection("OS properties supplier: %s",
				OS_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY, OS_PROPERTIES_FILE_DEFAULT);
	}
	@Override
	public Iterable<Supplier<Map<String, String>>> getIdePropertiesSupplier() {
		return fileBasedPropertyCollection("IDE properties supplier: %s", IDE_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY,
				IDE_PROPERTIES_FILE_DEFAULT);
	}
	@Override
	public Iterable<Supplier<Map<String, String>>> getAdditionalPropertiesSupplier() {
		final String additionalPropertyGroups = bootstrapper.getApplicationProperty(ADDITIONAL_PROPERTIES_GROUP_KEY);
		if (Strings.isNullOrEmpty(additionalPropertyGroups)) {
			return ImmutableList.of();
		} else {
			Iterable<String> groupNames = Splitter.on(',').trimResults().omitEmptyStrings().split(additionalPropertyGroups);
			Iterable<String> fileLocationKeys = Iterables.transform(groupNames, ADDITIONAL_GROUP_NAME_TO_FILE_PROP_KEY);
			
			return ImmutableList.copyOf(Iterables.transform(fileLocationKeys,
					new Function<String, Supplier<Map<String, String>>>() {
						@Override
						public Supplier<Map<String, String>> apply(final String fileNameKey) {
							return new Supplier<Map<String, String>>() {
								@Override
								public Map<String, String> get() {
									try {
										return loadPropertiesFileIfExists(bootstrapper.createPropertyFileRelativePath(bootstrapper.getApplicationProperty(fileNameKey)));
									} catch (IOException e) {
										throw propagate(e);
									}
								}
								@Override
								public String toString() {
									return String.format("Additional properties supplier: %s", fileNameKey);
								}
							};
						}
					}));
		}
	}
	@Override
	public Iterable<Supplier<Map<String, String>>> getEnvironmentPropertiesSupplier() {
		String env = bootstrapper.getApplicationProperty(BootstrapMain.BOOTSTRAP_ENVIRONMENT_KEY);
		String defaultEnvFile = (env == null) ? null : env + ".properties";
		return fileBasedPropertyCollection("environment properties supplier: %s",
				BootstrapMain.ENVIRONMENT_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY, defaultEnvFile);
	}
	@Override
	public Iterable<Supplier<Map<String, String>>> getCommonPropertiesSupplier() {
		return fileBasedPropertyCollection("common properties supplier: %s",
				COMMON_PROPERTIES_FILE_LOCATION_OVERRIDE_KEY, COMMON_PROPERTIES_FILE_DEFAULT);
	}

	private Iterable<Supplier<Map<String, String>>> fileBasedPropertyCollection(final String supplierName,
			String locationPropertyKey, String locationPropertyDefault) {
		final String locationsString = bootstrapper.getApplicationProperty(locationPropertyKey, locationPropertyDefault);
		if (locationsString == null) {
			return ImmutableList.of();
		}

		final Iterable<String> userPropertyLocations = Splitter.on(',').trimResults().omitEmptyStrings().split(
				locationsString);
		return ImmutableList.copyOf(Iterables.transform(userPropertyLocations,
				new Function<String, Supplier<Map<String, String>>>() {
					@Override
					public Supplier<Map<String, String>> apply(final String fileName) {
						return new Supplier<Map<String, String>>() {
							@Override
							public Map<String, String> get() {
								try {
									return loadPropertiesFileIfExists(bootstrapper.createPropertyFileRelativePath(fileName));
								} catch (IOException e) {
									throw propagate(e);
								}
							}
							@Override
							public String toString() {
								return String.format(supplierName, fileName);
							}
						};
					}
				}));
	}

	/**
	 * Loads properties from the specified file. This is a separate method to allow testing.
	 * <p>
	 * Default implementation uses {@link PropertyUtils#loadPropertiesFileIfExists(String)}.
	 * 
	 * @param filePath path to the file
	 * @return map of key-value associations from the properties file, or an empty map if the file does not exist
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *             be parsed
	 */
	protected Map<String, String> loadPropertiesFile(String filePath) throws IOException {
		return loadPropertiesFileIfExists(filePath);
	}
}
