package joe.util.bootstrap;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingPropertyProvider extends ForwardingObject implements PropertyProvider {
	@Override
	protected abstract PropertyProvider delegate();
	
	@Override
	public String getProperty(String key) {
		return delegate().getProperty(key);
	}
	@Override
	public boolean getBoolean(String key) {
		return delegate().getBoolean(key);
	}
	@Override
	public int getInteger(String key) {
		return delegate().getInteger(key);
	}
	@Override
	public int getIntegerOrDefault(String key, int defaultValue) {
		return delegate().getIntegerOrDefault(key, defaultValue);
	}
	@Override
	public long getLong(String key) {
		return delegate().getLong(key);
	}
	@Override
	public long getLongOrDefault(String key, int defaultValue) {
		return delegate().getLongOrDefault(key, defaultValue);
	}
	@Override
	public double getDouble(String key) {
		return delegate().getDouble(key);
	}
	@Override
	public double getDoubleOrDefault(String key, int defaultValue) {
		return delegate().getDoubleOrDefault(key, defaultValue);
	}
}
