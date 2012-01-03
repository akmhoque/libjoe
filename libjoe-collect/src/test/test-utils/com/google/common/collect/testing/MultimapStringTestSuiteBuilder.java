package com.google.common.collect.testing;

import junit.framework.TestSuite;

import com.google.common.collect.Multimap;

public class MultimapStringTestSuiteBuilder<M extends Multimap<String, String>> extends
		MultimapTestSuiteBuilder<String, String, M> {
	@Override
	public TestSuite createTestSuite() {
		withSampleGenerators("with distinct keys", TestStringMultimapGenerator.createSamplesWithDistinctKeys());
		withSampleGenerators("with one key", TestStringMultimapGenerator.createSamplesWithSameKeys());
		return super.createTestSuite();
	}
	
	public static <M extends Multimap<String, String>> MultimapTestSuiteBuilder<String, String, M> using(
			TestMultimapGenerator<String, String, M> generator) {
		return new MultimapStringTestSuiteBuilder<M>().usingGenerator(generator);
	}
}
