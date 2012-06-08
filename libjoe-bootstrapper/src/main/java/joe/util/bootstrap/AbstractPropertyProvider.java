package joe.util.bootstrap;

import static com.google.common.collect.Maps.transformValues;
import static joe.util.StringUtils.UNESCAPE;

import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.MapDifference;

/**
 * Support implementation of {@link PropertyProvider} that implements all methods in terms of {@link #getProperty(String)}.
 * 
 * @author Joe Kearney
 */
public abstract class AbstractPropertyProvider implements PropertyProvider {
	private static NumberFormatException wrapWithExplanation(String key, String stringValue, NumberFormatException originalNfe) {
		NumberFormatException newNfe = new NumberFormatException("Found but couldn't parse property [" + key + "] " +
				"with value [" + stringValue + "]");
		newNfe.initCause(originalNfe);
		return newNfe;
	}

	@Override
	public final boolean getBoolean(String key) {
		return "true".equalsIgnoreCase(getProperty(key));
	}

	// TODO reimplement with Ints.tryParse etc
	
	@Override
	public final int getInteger(String key) {
		final String value = getProperty(key);
		Preconditions.checkArgument(value != null, "No value found for property [%s]", key);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw wrapWithExplanation(key, value, e);
		}
	}
	@Override
	public final int getIntegerOrDefault(String key, int defaultValue) {
		final String value = getProperty(key);
		
		if (value == null) {
			return defaultValue;
		}
		
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw wrapWithExplanation(key, value, e);
		}
	}

	@Override
	public final long getLong(String key) {
		final String value = getProperty(key);
		Preconditions.checkArgument(value != null, "No value found for property [%s]", key);
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw wrapWithExplanation(key, value, e);
		}
	}
	@Override
	public final long getLongOrDefault(String key, int defaultValue) {
		final String value = getProperty(key);
		
		if (value == null) {
			return defaultValue;
		}
		
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw wrapWithExplanation(key, value, e);
		}
	}

	@Override
	public final double getDouble(String key) {
		final String value = getProperty(key);
		Preconditions.checkArgument(value != null, "No value found for property [%s]", key);
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw wrapWithExplanation(key, value, e);
		}
	}
	@Override
	public final double getDoubleOrDefault(String key, int defaultValue) {
		final String value = getProperty(key);
		
		if (value == null) {
			return defaultValue;
		}
		
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw wrapWithExplanation(key, value, e);
		}
	}

	/**
	 * Gets a mutable map view of this provider, such that modifications to map are reflected through the provider.&nbsp;(Optional)
	 * <p>
	 * This is an optional operation. The default implementation throws {@link UnsupportedOperationException}.
	 * 
	 * @return mutable map view of the properties
	 */
	protected Map<? super String, ? super String> asMutableMap() {
		throw new UnsupportedOperationException("This property provider is immutable");
	}
	/**
	 * Adds all properties from the parameter map to this provider, such that future requests for properties from this provider
	 * will reflect these updates.
	 * 
	 * @param newProperties properties to add
	 */
	protected final void addPropertiesToContext(Map<String, String> newProperties) {
		asMutableMap().putAll(newProperties);
	}

	private static final MapJoiner MAP_JOINER = Joiner.on("\n  ").withKeyValueSeparator(" => ");
	private static final MapJoiner MAP_JOINER_INDENTED = Joiner.on("\n    ").withKeyValueSeparator(" => ");
	/**
	 * A standard implementation of publishing from a {@link BootstrapResult} into a {@link PropertyProviderFactory}.
	 * This is intended for use when the {@code PropertyProvider} <i>is</i> the {@code PropertyProviderFactory}.
	 * <p>
	 * It uses the {@link #addPropertiesToContext(Map)} mechanism, which requires there to be a valid {@link #asMutableMap()}
	 * view available; {@link UnsupportedOperationException} will be thrown if this is not the case.
	 * <p>
	 * Note that most of this code is logging. If you don't want logging you can just use {@link #addPropertiesToContext(Map)}
	 * directly.
	 * 
	 * @param bootstrapResult properties to publish
	 */
	protected final void standardPublish(BootstrapResult bootstrapResult) {
		final BootstrapLogger logger = bootstrapResult.getLogger();
		final Map<String, String> propertiesToPublish = bootstrapResult.getPublishedSystemProperties();
		final MapDifference<String, String> difference = bootstrapResult.getDifference();
		
		logger.log("Setting application system properties");
		addPropertiesToContext(propertiesToPublish);
	
		logger.log("Application system properties set");
	
		logger.log("Properties changed by the bootstrapper:"
				+ (difference.entriesDiffering().isEmpty() ? " (none)"
						: ("\n    "
								+ MAP_JOINER.join(ImmutableSortedMap.copyOf(difference.entriesDiffering())) + "\n")));
		logger.log("Properties added by the bootstrapper (not including system properties set by the launcher):"
				+ (difference.entriesOnlyOnRight().isEmpty() ? " (none)"
						: ("\n    "
								+ MAP_JOINER.join(ImmutableSortedMap.copyOf(difference.entriesOnlyOnRight())) + "\n")));
	
		String mainClass = propertiesToPublish.get(BootstrapMain.BOOTSTRAP_MAIN_CLASS_KEY);
		String mainArgsString = propertiesToPublish.get(BootstrapMain.BOOTSTRAP_MAIN_ARGS_STRING_KEY);
		logger.log("Running application with\n" //
				+ "  main class        [" + (mainClass == null ? "not specified" : mainClass) + "]\n"
				+ "  main args         [" + mainArgsString + "]\n"
				+ "  system properties\n    "
				+ MAP_JOINER_INDENTED.join(ImmutableSortedMap.copyOf(transformValues(propertiesToPublish, UNESCAPE)))
				+ "\n\n"
				+ BootstrapMain.BOOTSTRAP_ENVIRONMENT_KEY + " => " + propertiesToPublish.get(BootstrapMain.BOOTSTRAP_ENVIRONMENT_KEY) + "\n");
	}
}
