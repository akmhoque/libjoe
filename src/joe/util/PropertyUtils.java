package joe.util;

import static com.google.common.base.Predicates.*;
import static com.google.common.base.Throwables.*;
import static com.google.common.collect.Maps.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;

public class PropertyUtils {
	private PropertyUtils() {}

	/** Function to transform from a file path to a property map based on the
	 * contents.
	 * 
	 * @param fileName name of the file to parse
	 * @return {@link ImmutableMap} of properties found in the file
	 * @throws FileNotFoundException if the file did not exist
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *         be parsed */
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

	/** Gets a live view over the entries in the current system properties map
	 * that map a {@link String} key to a {@code String} value. This will
	 * typically be everything, but typed more usefully than a
	 * {@code Hashtable<Object, Object>}.
	 * <p>
	 * Consider copying the result with {@link ImmutableMap#copyOf(Map)} in
	 * order to avoid the extra performance penalty on each lookup.
	 * 
	 * @return view of the {@code String} entries in the system property map */
	public static Map<String, String> getSystemPropertyStrings() {
		return getStringEntries(System.getProperties());
	}
	/** Gets a live view over the entries in the specified map that map a
	 * {@link String} key to a {@code String} value.
	 * <p>
	 * Consider copying the result with {@link ImmutableMap#copyOf(Map)} in
	 * order to avoid the extra performance penalty on each lookup.
	 * 
	 * @param map over which to provide a view
	 * @return view of the {@code String} entries in the map */
	@SuppressWarnings("unchecked")
	// casts are safe, we can only ever see String instances
	public static Map<String, String> getStringEntries(Map<?, ?> map) {
		return (Map<String, String>) (Map<?, ?>) filterValues(
				filterKeys(map, instanceOf(String.class)),
				instanceOf(String.class));
	}

	/** Loads a properties file into a map, returning a immutable view.
	 * 
	 * @param fileName name of the file to parse
	 * @return {@link ImmutableMap} of properties found in the file
	 * @throws FileNotFoundException if the file did not exist
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *         be parsed */
	public static Map<String, String> loadPropertiesFile(String fileName)
			throws FileNotFoundException, IOException, IllegalArgumentException {
		return loadPropertiesFile(new File(fileName));
	}
	/** Loads a properties file into a map, returning a immutable view.
	 * 
	 * @param file file to parse
	 * @return {@link ImmutableMap} of properties found in the file
	 * @throws FileNotFoundException if the file did not exist
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *         be parsed */
	public static Map<String, String> loadPropertiesFile(File file)
			throws FileNotFoundException, IOException, IllegalArgumentException {
		return loadPropertiesStream(new FileInputStream(file));
	}
	/** Loads a properties file into a map if, returning a immutable view, if the
	 * file exists. Returns an empty map otherwise.
	 * 
	 * @param file file to parse
	 * @return {@link ImmutableMap} of properties found in the file if the file
	 *         exists, or an empty map otherwise
	 * @throws FileNotFoundException if the file did not exist
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *         be parsed */
	public static Map<String, String> loadPropertiesFileIfExists(String fileName)
			throws FileNotFoundException, IOException {
		return loadPropertiesFileIfExists(fileName == null ? null : new File(
				fileName));
	}
	/** Loads a properties file into a map if, returning a immutable view, if the
	 * file exists. Returns an empty map otherwise.
	 * 
	 * @param file file to parse
	 * @return {@link ImmutableMap} of properties found in the file if the file
	 *         exists, or an empty map otherwise
	 * @throws FileNotFoundException if the file did not exist
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *         be parsed */
	public static Map<String, String> loadPropertiesFileIfExists(File file)
			throws FileNotFoundException, IOException {
		return file != null && file.exists() ? loadPropertiesFile(file)
				: emptyPropertiesMap();
	}
	/** Loads an input stream containing {@code key=value} pairs into a map,
	 * returning a immutable view. The parameter is typically an
	 * {@link InputStream} over a properties file.
	 * 
	 * @param inputStream stream to parse
	 * @return {@link ImmutableMap} of properties found in the stream
	 * @throws IOException if there was a problem reading from the stream
	 * @throws IllegalArgumentException if the stream was malformed and could
	 *         not be parsed
	 * @see #loadPropertiesFile(String) */
	public static Map<String, String> loadPropertiesStream(
			InputStream inputStream) throws IOException,
			IllegalArgumentException {
		Properties properties = new Properties();
		properties.load(inputStream);
		return ImmutableSortedMap.copyOf(getStringEntries(properties));
	}
	/** Returns an empty map, typed for ease of use. */
	public static Map<String, String> emptyPropertiesMap() {
		return ImmutableMap.of();
	}
}
