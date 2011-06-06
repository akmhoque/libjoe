package joe.util;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Maps.*;
import static java.util.regex.Pattern.quote;
import static joe.util.StringUtils.UNESCAPE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;

public class PropertyUtils {
	private PropertyUtils() {}

	/**
	 * Function to transform from a file path to a property map based on the contents. The map returned from the
	 * function is immutable.
	 * 
	 * @see #loadPropertiesFile(String)
	 */
	public static final Function<String, Map<String, String>> READ_PROP_FILE_TO_MAP = new Function<String, Map<String, String>>() {
		@Override
		public Map<String, String> apply(String path) {
			try {
				return loadPropertiesFile(path);
			} catch (IOException e) {
				throw propagate(e);
			}
		}
	};

	/**
	 * Resolves properties found in values that are defined as keys in the map. The input map is unchanged.
	 * 
	 * @param unresolvedProperties a map of properties
	 * @return an immutable copy of the input where values have nested properties resolved
	 */
	public static Map<String, String> resolvePropertiesInternally(final Map<String, String> unresolvedProperties) {
		Map<String, String> initialProperties;
		Map<String, String> resolvedCopy = unresolvedProperties;
		
		do {
			initialProperties = resolvedCopy;
			resolvedCopy = ImmutableMap.copyOf(transformValues(initialProperties,
					propertyResolverFromMap(initialProperties)));
		} while (!resolvedCopy.equals(initialProperties));
	
		return resolvedCopy;
	}
	/**
	 * Resolves properties found in values that are defined as keys in another map. The input maps are unchanged.
	 * Consider {@linkplain #resolvePropertiesInternally(Map) resolving properties internally} before or after doing
	 * this, depending on the intended priority ordering.
	 * 
	 * @param unresolvedProperties a map of properties including some that need to be resolved
	 * @param context a map of properties from which to resolve property references
	 * @return an immutable copy of the input where values have nested properties resolved
	 */
	public static Map<String, String> resolvePropertiesExternally(final Map<String, String> unresolvedProperties, final Map<String, String> context) {
		Function<String, String> propertyResolver = propertyResolverFromMap(context);
		
		Map<String, String> initialProperties;
		Map<String, String> resolvedCopy = unresolvedProperties;
		
		do {
			initialProperties = resolvedCopy;
			resolvedCopy = ImmutableMap.copyOf(transformValues(initialProperties,
					propertyResolver));
		} while (!resolvedCopy.equals(initialProperties));
		
		return resolvedCopy;
	}
	/**
	 * Gets a live view over the entries in the current system properties map
	 * that map a {@link String} key to a {@code String} value. This will
	 * typically be everything, but typed more usefully than a {@code Hashtable<Object, Object>}.
	 * <p>
	 * Consider copying the result with {@link ImmutableMap#copyOf(Map)} in order to avoid the extra performance penalty
	 * on each lookup.
	 * 
	 * @return view of the {@code String} entries in the system property map
	 */
	public static Map<String, String> getSystemPropertyStrings() {
		return getStringEntries(System.getProperties());
	}
	/**
	 * Gets a live view over the entries in the specified map that map a {@link String} key to a {@code String} value.
	 * <p>
	 * Consider copying the result with {@link ImmutableMap#copyOf(Map)} in order to avoid the extra performance penalty
	 * on each lookup.
	 * 
	 * @param map over which to provide a view
	 * @return view of the {@code String} entries in the map
	 */
	@SuppressWarnings("unchecked")
	// casts are safe, we can only ever see String instances
	public static Map<String, String> getStringEntries(Map<?, ?> map) {
		return (Map<String, String>) (Map<?, ?>) filterValues(filterKeys(map, instanceOf(String.class)),
				instanceOf(String.class));
	}

	/**
	 * Loads a properties file into a map, returning a immutable view.
	 * 
	 * @param fileName name of the file to parse
	 * @return {@link ImmutableMap} of properties found in the file
	 * @throws FileNotFoundException if the file did not exist
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *             be parsed
	 */
	public static Map<String, String> loadPropertiesFile(String fileName) throws FileNotFoundException, IOException,
			IllegalArgumentException {
		return loadPropertiesFile(new File(fileName));
	}
	/**
	 * Loads a properties file into a map, returning a immutable view.
	 * 
	 * @param file file to parse
	 * @return {@link ImmutableMap} of properties found in the file
	 * @throws FileNotFoundException if the file did not exist
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *             be parsed
	 */
	public static Map<String, String> loadPropertiesFile(File file) throws FileNotFoundException, IOException,
			IllegalArgumentException {
		FileInputStream inputStream = new FileInputStream(file);
		try {
			return loadPropertiesStream(inputStream);
		} finally {
			inputStream.close();
		}
	}
	/**
	 * Loads a properties file into a map if, returning a immutable view, if the
	 * file exists. Returns an empty map otherwise.
	 * 
	 * @param fileName file to parse
	 * @return {@link ImmutableMap} of properties found in the file if the file
	 *         exists, or an empty map otherwise
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *             be parsed
	 */
	public static Map<String, String> loadPropertiesFileIfExists(String fileName) throws IOException {
		return loadPropertiesFileIfExists(fileName == null ? null : new File(fileName));
	}
	/**
	 * Loads a properties file into a map if, returning a immutable view, if the
	 * file exists. Returns an empty map otherwise.
	 * 
	 * @param file file to parse
	 * @return {@link ImmutableMap} of properties found in the file if the file
	 *         exists, or an empty map otherwise
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *             be parsed
	 */
	public static Map<String, String> loadPropertiesFileIfExists(File file) throws IOException {
		try {
			return file != null && file.exists() ? loadPropertiesFile(file) : emptyPropertiesMap();
		} catch (FileNotFoundException e) {
			throw new IOException("File was thought to exist, but couldn't be found when attempting to read", e);
		}
	}
	/**
	 * Loads an input stream containing {@code key=value} pairs into a map,
	 * returning a immutable view. The parameter is typically an {@link InputStream} over a properties file.
	 * 
	 * @param inputStream stream to parse
	 * @return {@link ImmutableMap} of properties found in the stream
	 * @throws IOException if there was a problem reading from the stream
	 * @throws IllegalArgumentException if the stream was malformed and could
	 *             not be parsed
	 * @see #loadPropertiesFile(String)
	 */
	public static Map<String, String> loadPropertiesStream(InputStream inputStream) throws IOException,
			IllegalArgumentException {
		Properties properties = new Properties();
		properties.load(inputStream);
		return ImmutableSortedMap.copyOf(getStringEntries(properties));
	}
	/** Returns an empty map, typed for ease of use. */
	public static Map<String, String> emptyPropertiesMap() {
		return ImmutableMap.of();
	}

	static final String PROPERTY_KEY_START_MARKER = "${";
	static final String PROPERTY_KEY_END_MARKER = "}";
	static final Pattern PROPERTY_KEY_PATTERN = Pattern.compile(quote(PROPERTY_KEY_START_MARKER) + "([^"
			+ quote(PROPERTY_KEY_START_MARKER) + quote(PROPERTY_KEY_END_MARKER) + "]*)"
			+ quote(PROPERTY_KEY_END_MARKER));
	public static Function<String, String> propertyResolverFromMap(final Map<String, String> properties) {
		return new PropertyResolverFromMap(properties);
	}
	
	private static final class PropertyResolverFromMap implements Function<String, String> {
		private final Map<String, String> properties;
		private PropertyResolverFromMap(Map<String, String> properties) {
			this.properties = properties;
		}
		@Override
		public String apply(final String input) {
			String result = input;
			boolean changed = false;
	
			do {
				StringBuffer sb = new StringBuffer();
				if (changed = resolvePropertyName(result, false, sb)) {
					result = sb.toString();
				}
			} while (changed);
	
			return result;
		}
		private boolean resolvePropertyName(String input, boolean isKey, StringBuffer sb) {
			if (isKey) {
				String value = properties.get(input);
				if (value != null) {
					sb.append(value);
					return true;
				} else {
					sb.append(PROPERTY_KEY_START_MARKER);
					sb.append(input);
					sb.append(PROPERTY_KEY_END_MARKER);
					return false;
				}
			}
	
			Matcher matcher = PROPERTY_KEY_PATTERN.matcher(input);
	
			boolean changed = false;
			int furthestIndexOfInputCopied = 0;
			while (matcher.find()) {
				sb.append(input.substring(furthestIndexOfInputCopied, matcher.start()));
				changed |= resolvePropertyName(matcher.group(1), true, sb);
				furthestIndexOfInputCopied = matcher.end(); // not end(1), want to skip the '}'
			}
			
			sb.append(input.substring(furthestIndexOfInputCopied));
	
			return changed;
		}
	}
	private static final MapJoiner PRINT_FRIENDLY_MAP_JOINER = Joiner.on("\n  ").withKeyValueSeparator(" => ").useForNull(
			"(null)");
	public static final String toLogFriendlyString(Map<String, String> properties) {
		return PRINT_FRIENDLY_MAP_JOINER.join(Maps.transformValues(properties, UNESCAPE));
	}
	public static final StringBuilder toLogFriendlyString(StringBuilder sb, Map<String, String> properties) {
		return PRINT_FRIENDLY_MAP_JOINER.appendTo(sb, Maps.transformValues(properties, UNESCAPE));
	}
}
