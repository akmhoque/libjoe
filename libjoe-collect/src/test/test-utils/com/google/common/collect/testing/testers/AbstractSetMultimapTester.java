package com.google.common.collect.testing.testers;

import java.util.Set;

import com.google.common.collect.SetMultimap;

public class AbstractSetMultimapTester<K, V> extends AbstractMultimapTester<K, V, SetMultimap<K, V>> {
	@Override
	protected Set<V> get(K key) {
		return (Set<V>) super.get(key);
	}
}

