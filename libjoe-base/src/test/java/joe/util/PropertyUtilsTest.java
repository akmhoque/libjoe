package joe.util;


import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import joe.util.PropertyUtils;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

public class PropertyUtilsTest {
	@Test
	public void testPropertyResolveTrivial() throws Exception {
		Function<String, String> propertyResolverFromMap = PropertyUtils.propertyResolverFromMap(ImmutableMap.<String, String> of());
		assertThat(propertyResolverFromMap.apply("abc"), is("abc"));
	}
	@Test
	public void testPropertyResolveSingle() throws Exception {
		Function<String, String> propertyResolverFromMap = PropertyUtils.propertyResolverFromMap(ImmutableMap.of("k", "v"));
		assertThat(propertyResolverFromMap.apply("abc${k}def"), is("abcvdef"));
	}
	@Test
	public void testPropertyResolveNested() throws Exception {
		Function<String, String> propertyResolverFromMap = PropertyUtils.propertyResolverFromMap(ImmutableMap.of("key1", "${key2}", "key2", "v"));
		assertThat(propertyResolverFromMap.apply("abc${key1}def"), is("abcvdef"));
	}
	@Test
	public void testPropertyResolveNestedDirect() throws Exception {
		Function<String, String> propertyResolverFromMap = PropertyUtils.propertyResolverFromMap(ImmutableMap.of("key1", "${key2}", "key2", "v2", "key1.v2", "x"));
		assertThat(propertyResolverFromMap.apply("abc${key1.${key2}}def"), is("abcxdef"));
	}
	@Test
	public void testPropertyResolveTwo() throws Exception {
		Function<String, String> propertyResolverFromMap = PropertyUtils.propertyResolverFromMap(ImmutableMap.of("key1", "v1", "key2", "v2"));
		assertThat(propertyResolverFromMap.apply("abc${key1}de${key2}f"), is("abcv1dev2f"));
	}
	@Test
	public void testPropertyResolveUnknown() throws Exception {
		Function<String, String> propertyResolverFromMap = PropertyUtils.propertyResolverFromMap(ImmutableMap.<String, String> of());
		assertThat(propertyResolverFromMap.apply("abc${key}"), is("abc${key}"));
	}
	@Test
	public void testPropertyResolveKnownThenUnknown() throws Exception {
		Function<String, String> propertyResolverFromMap = PropertyUtils.propertyResolverFromMap(ImmutableMap.<String, String> of(
				"knownKey", "blah"));
		assertThat(propertyResolverFromMap.apply("abc${knownKey}def${unknownKey}ghi"), is("abcblahdef${unknownKey}ghi"));
	}
	@Test
	public void testPropertyResolveUnknownThenUnknown() throws Exception {
		Function<String, String> propertyResolverFromMap = PropertyUtils.propertyResolverFromMap(ImmutableMap.<String, String> of(
				"knownKey", "blah"));
		assertThat(propertyResolverFromMap.apply("abc${unknownKey}def${knownKey}ghi"), is("abc${unknownKey}defblahghi"));
	}
}
