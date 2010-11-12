package com.google.common.collect.testing;

import java.util.Map;

import com.google.common.collect.Multimap;
import com.google.common.collect.testing.TestContainerGenerator;

public interface TestMultimapGenerator<K, V, M extends Multimap<K, V>> extends TestContainerGenerator<M, Map.Entry<K, V>> {
	K[] createKeysArray(int length);
	V[] createValuesArray(int length);
}

