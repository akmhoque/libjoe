package joe.util.bootstrap;

import java.util.Map;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

/**
 * Structure holding the state of the system properties before and after bootstrapping.
 * 
 * @author Joe Kearney
 */
@Immutable
public final class BootstrapResult {
	/** Left: prior system properties. Right: new system properties */
	private final MapDifference<String, String> difference;
	private final Map<String, String> priorSystemProperties;
	private final Map<String, String> publishedSystemProperties;
	private final BootstrapLogger logger;

	BootstrapResult(Map<String, String> priorSystemProperties, Map<String, String> publishedSystemProperties, BootstrapLogger logger) {
		this.priorSystemProperties = ImmutableMap.copyOf(priorSystemProperties);
		this.publishedSystemProperties = ImmutableMap.copyOf(publishedSystemProperties);
		this.difference = Maps.difference(priorSystemProperties, publishedSystemProperties);
		
		this.logger = logger;
	}

	/**
	 * Gets a copy of the system property map as seen prior to bootstrapping.
	 * 
	 * @return immutable copy of pre-bootstrapping properties
	 */
	public Map<String, String> getPriorSystemProperties() {
		return priorSystemProperties;
	}

	/**
	 * Gets a copy of the full property set after bootstrapping. This contains all system properties
	 * and all properties loaded or computed by the bootstrapper. Note that these properties will be
	 * present in the System property map if {@link BootstrapMain#prepareProperties()} was used; if
	 * properties were loaded through {@link BootstrapMain#loadPropertiesForEnvironment(String)
	 * loadPropertiesForEnvironment} then this will be the only record of the loaded property set.
	 * 
	 * @return immutable copy of the post-bootstrapping properties
	 */
	public Map<String, String> getPublishedSystemProperties() {
		return publishedSystemProperties;
	}
	
	/**
	 * Gets the {@link BootstrapLogger} used for this bootstrap.
	 * 
	 * @return logger
	 */
	public BootstrapLogger getLogger() {
		return logger;
	}

	/**
	 * Gets a {@link Maps#difference(Map, Map)} of the {@linkplain #getPriorSystemProperties() pre-} and
	 * {@linkplain #getPublishedSystemProperties() post-}bootstrapping properties.
	 * 
	 * @return difference effected by the bootstrapper to produce this result
	 */
	public MapDifference<String, String> getDifference() {
		return difference;
	}
	
	public PropertyProvider asPropertyProvider() {
		return MapBackedPropertyProvider.forMap(publishedSystemProperties);
	}

	/**
	 * Publishes the {@code BootstrapResult} to get a {@link PropertyProvider} based on its generated 
	 * properties.
	 *  
	 * @param factory factory to which to publish
	 * @return the {@code PropertyProvider}
	 */
	public <T extends PropertyProvider> T publishTo(PropertyProviderFactory<T> factory) {
		return factory.providerFor(this);
	}
}
