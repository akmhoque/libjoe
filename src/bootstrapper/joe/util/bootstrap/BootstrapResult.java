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

	public BootstrapResult(Map<String, String> priorSystemProperties, Map<String, String> publishedSystemProperties) {
		this.priorSystemProperties = ImmutableMap.copyOf(priorSystemProperties);
		this.publishedSystemProperties = ImmutableMap.copyOf(publishedSystemProperties);
		difference = Maps.difference(priorSystemProperties, publishedSystemProperties);
	}

	public Map<String, String> getPriorSystemProperties() {
		return priorSystemProperties;
	}
	public Map<String, String> getPublishedSystemProperties() {
		return publishedSystemProperties;
	}
	public MapDifference<String, String> getDifference() {
		return difference;
	}
}
