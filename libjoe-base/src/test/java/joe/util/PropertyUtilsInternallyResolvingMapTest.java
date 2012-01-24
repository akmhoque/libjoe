package joe.util;

import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;

public class PropertyUtilsInternallyResolvingMapTest {
	public static Test suite() {
	    return MapTestSuiteBuilder
	        .using(new TestStringMapGenerator() {
	            @Override protected Map<String, String> create(
	                Entry<String, String>[] entries) {
	            	Builder<String, String> builder = ImmutableMap.builder();
	            	for (Entry<String, String> entry : entries) {
						builder.put(entry);
					}
	              return PropertyUtils.resolvingPropertiesInternallyView(builder.build());
	            }
	          })
	        .named("Resolving-internally properties map")
	        .withFeatures(
	            MapFeature.ALLOWS_NULL_QUERIES,
	            MapFeature.REJECTS_DUPLICATES_AT_CREATION,
	            CollectionFeature.KNOWN_ORDER,
	            CollectionSize.ANY)
	        .createTestSuite();
	}
}
