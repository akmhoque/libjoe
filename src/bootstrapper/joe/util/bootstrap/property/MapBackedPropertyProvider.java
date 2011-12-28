package joe.util.bootstrap.property;

import java.util.Map;

import com.google.common.base.Preconditions;

public class MapBackedPropertyProvider implements PropertyProvider {
	private static NumberFormatException wrapWithExplanation(String key, String stringValue,
			NumberFormatException nfe) {
				NumberFormatException e2 = new NumberFormatException("Found but couldn't parse property [" + key + "] with value [" + stringValue + "]");
				e2.initCause(nfe);
				return nfe;
			}
	
	public static MapBackedPropertyProvider createFromMap(Map<String, String> propertyMap) {
		return new MapBackedPropertyProvider(propertyMap);
	}

	private final Map<String, String> propertyMap;
	MapBackedPropertyProvider(Map<String, String> propertyMap) {
		this.propertyMap = propertyMap;
	}

	@Override
	public String getProperty(String key) {
		return propertyMap.get(key);
	}

	@Override
	public boolean getBoolean(String key) {
		return "true".equalsIgnoreCase(getProperty(key));
	}

	@Override
	public int getInteger(String key) {
		final String value = getProperty(key);
		Preconditions.checkArgument(value != null, "No value found for property [%s]", key);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw wrapWithExplanation(key, value, e);
		}
	}

	@Override
	public int getIntegerOrDefault(String key, int defaultValue) {
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
	public long getLong(String key) {
		final String value = getProperty(key);
		Preconditions.checkArgument(value != null, "No value found for property [%s]", key);
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw wrapWithExplanation(key, value, e);
		}
	}

	@Override
	public long getLongOrDefault(String key, int defaultValue) {
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
	public double getDouble(String key) {
		final String value = getProperty(key);
		Preconditions.checkArgument(value != null, "No value found for property [%s]", key);
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw wrapWithExplanation(key, value, e);
		}
	}

	@Override
	public double getDoubleOrDefault(String key, int defaultValue) {
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

}