package joe.util.bootstrap;

/**
 * General property context for an application. This can be thought of as similar to Java System properties,
 * along with operations such as {@link Integer#getInteger(String)} backed by those system properties. This
 * allows the notion of separate contexts co-existing within a single Java process.
 * <p>
 * A property provider should be injected into other objects as a source of configuration properties.
 * 
 * @author Joe Kearney
 * @see SystemPropertyProvider SystemPropertyProvider, a {@code PropertyProvider} backed by System properties
 */
public interface PropertyProvider {
	/**
	 * Gets the {@link String} value of the property associated with this key, or
	 * {@code null} if there is no entry for this key.
	 * 
	 * @param key the property key
	 * @return the {@link String} value of the property
	 * @see System#getProperty(String)
	 */
	String getProperty(String key);
	
	/**
	 * Gets the {@code boolean} value of the property associated with this key, or
	 * {@code false} if there is no entry for this key.
	 * 
	 * @param key the property key
	 * @return the {@code boolean} value of the property
	 * @see Boolean#getBoolean(String)
	 */
	boolean getBoolean(String key);
	
	/**
	 * Gets the {@code int} value of the property associated with this key, or
	 * throws {@link IllegalArgumentException} if there is no entry for this key.
	 * 
	 * @param key the property key
	 * @return the {@link String} value of the property
	 * @throws NumberFormatException if the property is present but cannot be parsed
	 * @see Integer#getInteger
	 */
	int getInteger(String key);
	/**
	 * Gets the {@code int} value of the property associated with this key, or
	 * returns the specified default if there is no entry for this key.
	 * 
	 * @param key the property key
	 * @param defaultValue the default value to return if there is no entry for this key
	 * @return the {@code long} value of the property
	 * @throws NumberFormatException if the property is present but cannot be parsed
	 * @see Integer#getInteger
	 */
	int getIntegerOrDefault(String key, int defaultValue);
	
	/**
	 * Gets the {@code long} value of the property associated with this key, or
	 * throws {@link IllegalArgumentException} if there is no entry for this key.
	 * 
	 * @param key the property key
	 * @return the {@code long} value of the property
	 * @throws NumberFormatException if the property is present but cannot be parsed
	 * @see Long#getLong(String)
	 */
	long getLong(String key);
	/**
	 * Gets the {@code long} value of the property associated with this key, or
	 * returns the specified default if there is no entry for this key.
	 * 
	 * @param key the property key
	 * @param defaultValue the default value to return if there is no entry for this key
	 * @return the {@code long} value of the property
	 * @throws NumberFormatException if the property is present but cannot be parsed
	 * @see Long#getLong(String)
	 */
	long getLongOrDefault(String key, int defaultValue);
	
	/**
	 * Gets the {@code double} value of the property associated with this key, or
	 * throws {@link IllegalArgumentException} if there is no entry for this key.
	 * 
	 * @param key the property key
	 * @return the {@code double} value of the property
	 * @throws NumberFormatException if the property is present but cannot be parsed
	 * @see Double#getDouble(String)
	 */
	double getDouble(String key);
	/**
	 * Gets the {@code double} value of the property associated with this key, or
	 * returns the specified default if there is no entry for this key.
	 * 
	 * @param key the property key
	 * @param defaultValue the default value to return if there is no entry for this key
	 * @return the {@code double} value of the property
	 * @throws NumberFormatException if the property is present but cannot be parsed
	 * @see Double#getDouble(String)
	 */
	double getDoubleOrDefault(String key, int defaultValue);
}
