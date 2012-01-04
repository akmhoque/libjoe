package com.google.common.collect.testing;

import static com.google.common.collect.Maps.newHashMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestSuite;

import com.google.common.collect.ForwardingObject;
import com.google.common.collect.Multimap;
import com.google.common.collect.testing.features.ObjectFeature;
import com.google.common.collect.testing.testers.MultimapClearTester;
import com.google.common.collect.testing.testers.MultimapContainsKeyTester;
import com.google.common.collect.testing.testers.MultimapContainsValueTester;
import com.google.common.collect.testing.testers.MultimapCreationTester;
import com.google.common.collect.testing.testers.MultimapEqualsTester;
import com.google.common.collect.testing.testers.MultimapGetTester;
import com.google.common.collect.testing.testers.MultimapHashCodeTester;
import com.google.common.collect.testing.testers.MultimapIsEmptyTester;
import com.google.common.collect.testing.testers.MultimapPutAllTester;
import com.google.common.collect.testing.testers.MultimapPutTester;
import com.google.common.collect.testing.testers.MultimapRemoveTester;
import com.google.common.collect.testing.testers.MultimapSizeTester;

public class MultimapTestSuiteBuilder<K, V, M extends Multimap<K, V>> extends
		FeatureSpecificTestSuiteBuilder<MultimapTestSuiteBuilder<K, V, M>, TestMultimapGenerator<K, V, M>> {
	private Map<String, SampleElements<Entry<K, V>>> sampleSuppliersByName = newHashMap();

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	protected List<Class<? extends AbstractTester>> getTesters() {
		return Arrays.<Class<? extends AbstractTester>> asList(MultimapClearTester.class,
				MultimapContainsKeyTester.class, MultimapContainsValueTester.class, MultimapCreationTester.class,
				MultimapEqualsTester.class, MultimapGetTester.class, MultimapHashCodeTester.class,
				MultimapIsEmptyTester.class, MultimapPutAllTester.class, MultimapPutTester.class,
				MultimapRemoveTester.class, MultimapSizeTester.class);
	}
	public static <K, V, M extends Multimap<K, V>> MultimapTestSuiteBuilder<K, V, M> using(
			TestMultimapGenerator<K, V, M> generator) {
		return new MultimapTestSuiteBuilder<K, V, M>().usingGenerator(generator);
	}

	public MultimapTestSuiteBuilder<K, V, M> withSampleGenerators(String name,
			SampleElements<Map.Entry<K, V>> sampleElements) {
		this.sampleSuppliersByName.put(name, sampleElements);
		return this;
	}

	@Override
	protected TestMultimapGenerator<K, V, M> getSubjectGenerator() {
		return super.getSubjectGenerator();
	}

	@Override
	public TestSuite createTestSuite() {
		if (getFeatures().contains(ObjectFeature.SERIALIZABLE)) {
			String baseName = getName();
			TestSuite bigTestSuite = new TestSuite(baseName);
			bigTestSuite.addTest(createPerCollectionSizeTests(baseName + " directly", getSubjectGenerator()));
			bigTestSuite.addTest(createPerCollectionSizeTests(baseName + " reserialized",
					new ForwardingTestContainerGenerator<M, Map.Entry<K, V>>() {
						@Override
						protected TestContainerGenerator<M, Entry<K, V>> delegate() {
							return getSubjectGenerator();
						}
						@Override
						public M create(Object ... elements) {
							return reserialize(super.create(elements));
						}
					}));
			return bigTestSuite;
		} else {
			return createPerCollectionSizeTests(getName(), getSubjectGenerator());
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> T reserialize(T object) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bytes);
			out.writeObject(object);
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
			return (T) in.readObject();
		} catch (IOException e) {
			Helpers.fail(e, e.getMessage());
		} catch (ClassNotFoundException e) {
			Helpers.fail(e, e.getMessage());
		}
		throw new AssertionError("not reachable");
	}

	private TestSuite createPerCollectionSizeTests(String baseName,
			final TestContainerGenerator<M, Entry<K, V>> subjectGenerator) {
		TestSuite bigTestSuite = new TestSuite(baseName);
		for (Entry<String, SampleElements<Entry<K, V>>> entry : sampleSuppliersByName.entrySet()) {
			String name = entry.getKey();
			final SampleElements<Entry<K, V>> samples = entry.getValue();
			ForwardingTestContainerGenerator<M, Entry<K, V>> subjGeneratorWithNewSamples = new ForwardingTestContainerGenerator<M, Entry<K, V>>() {
				@Override
				protected TestContainerGenerator<M, Entry<K, V>> delegate() {
					return subjectGenerator;
				}
				@Override
				public SampleElements<Entry<K, V>> samples() {
					return samples;
				}
			};
			TestSuite testSuite = new PerSampleSetTestSuiteBuilder().named(baseName + " " + name).suppressing(
					getSuppressedTests()).withFeatures(getFeatures()).usingGenerator(subjGeneratorWithNewSamples).createTestSuite();
			bigTestSuite.addTest(testSuite);
		}
		return bigTestSuite;
	}

	class PerSampleSetTestSuiteBuilder
			extends
			PerCollectionSizeTestSuiteBuilder<PerSampleSetTestSuiteBuilder, TestContainerGenerator<M, Entry<K, V>>, M, Map.Entry<K, V>> {
		@Override
		protected List<Class<? extends AbstractTester>> getTesters() {
			return MultimapTestSuiteBuilder.this.getTesters();
		}
		@Override
		public PerSampleSetTestSuiteBuilder usingGenerator(TestContainerGenerator<M, Entry<K, V>> subjectGenerator) {
			return super.usingGenerator(subjectGenerator);
		}

//		@Override
//		List<TestSuite> createDerivedSuites(
//				FeatureSpecificTestSuiteBuilder<?, ? extends OneSizeTestContainerGenerator<M, Map.Entry<K, V>>> parentBuilder) {
//			// TODO(George van den Driessche): Once invariant support is added, supply
//			// invariants to each of the derived suites, to check that mutations to
//			// the derived collections are reflected in the underlying map.
//
//			List<TestSuite> derivedSuites = super.createDerivedSuites(parentBuilder);
//
//			derivedSuites.add(CollectionTestSuiteBuilder.using(
//					new MultimapTestSuiteBuilder<K, V, M>(parentBuilder.getSubjectGenerator())).withFeatures(
//					computeEntrySetFeatures(parentBuilder.getFeatures())).named(parentBuilder.getName() + " entrySet").suppressing(
//					parentBuilder.getSuppressedTests()).createTestSuite());
//
//			derivedSuites.add(SetTestSuiteBuilder.using(
//					new MapKeySetGenerator<K, V>(parentBuilder.getSubjectGenerator())).withFeatures(
//					computeKeySetFeatures(parentBuilder.getFeatures())).named(parentBuilder.getName() + " keys").suppressing(
//					parentBuilder.getSuppressedTests()).createTestSuite());
//
//			derivedSuites.add(CollectionTestSuiteBuilder.using(
//					new MapValueCollectionGenerator<K, V>(parentBuilder.getSubjectGenerator())).named(
//					parentBuilder.getName() + " values").withFeatures(
//					computeValuesCollectionFeatures(parentBuilder.getFeatures())).suppressing(
//					parentBuilder.getSuppressedTests()).createTestSuite());
//
//			return derivedSuites;
//		}
	}
	static abstract class ForwardingTestContainerGenerator<T, E> extends ForwardingObject implements
			TestContainerGenerator<T, E> {
		@Override
		protected abstract TestContainerGenerator<T, E> delegate();
		@Override
		public SampleElements<E> samples() {
			return delegate().samples();
		}
		@Override
		public T create(Object ... elements) {
			return delegate().create(elements);
		}
		@Override
		public E[] createArray(int length) {
			return delegate().createArray(length);
		}
		@Override
		public Iterable<E> order(List<E> insertionOrder) {
			return delegate().order(insertionOrder);
		}
	}
	static abstract class ForwardingTestMultimapGenerator<K, V, M extends Multimap<K, V>> extends
			ForwardingTestContainerGenerator<M, Entry<K, V>> implements TestMultimapGenerator<K, V, M> {
		@Override
		protected abstract TestMultimapGenerator<K, V, M> delegate();
		@Override
		public K[] createKeysArray(int length) {
			return delegate().createKeysArray(length);
		}
		@Override
		public V[] createValuesArray(int length) {
			return delegate().createValuesArray(length);
		}
	}

//	private class MultimapEntrySetGenerator<K, V> implements TestCollectionGenerator<Map.Entry<K, V>> {
//		private final OneSizeTestContainerGenerator<M, Map.Entry<K, V>> multimapGenerator;
//
//		public MultimapEntrySetGenerator(OneSizeTestContainerGenerator<M, Map.Entry<K, V>> multimapGenerator) {
//			this.multimapGenerator = multimapGenerator;
//		}
//
//		@Override
//		public SampleElements<Map.Entry<K, V>> samples() {
//			return multimapGenerator.samples();
//		}
//
//		@Override
//		public Collection<Entry<K,V>> create(Object ... elements) {
//			return multimapGenerator.create(elements).entries();
//		}
//
//		@Override
//		public Map.Entry<K, V>[] createArray(int length) {
//			return multimapGenerator.createArray(length);
//		}
//
//		@Override
//		public Iterable<Map.Entry<K, V>> order(List<Map.Entry<K, V>> insertionOrder) {
//			return multimapGenerator.order(insertionOrder);
//		}
//	}
//
//	// TODO(George van den Driessche): investigate some API changes to
//	// SampleElements that would tidy up parts of the following classes.
//
//	private static class MapKeySetGenerator<K, V> implements TestSetGenerator<K> {
//		private final OneSizeTestContainerGenerator<Map<K, V>, Map.Entry<K, V>> mapGenerator;
//		private final SampleElements<K> samples;
//
//		public MapKeySetGenerator(OneSizeTestContainerGenerator<Map<K, V>, Map.Entry<K, V>> mapGenerator) {
//			this.mapGenerator = mapGenerator;
//			final SampleElements<Map.Entry<K, V>> mapSamples = this.mapGenerator.samples();
//			this.samples = new SampleElements<K>(mapSamples.e0.getKey(), mapSamples.e1.getKey(),
//					mapSamples.e2.getKey(), mapSamples.e3.getKey(), mapSamples.e4.getKey());
//		}
//
//		@Override
//		public SampleElements<K> samples() {
//			return samples;
//		}
//
//		@Override
//		public Set<K> create(Object ... elements) {
//			@SuppressWarnings("unchecked")
//			K[] keysArray = (K[]) elements;
//
//			// Start with a suitably shaped collection of entries
//			Collection<Map.Entry<K, V>> originalEntries = mapGenerator.getSampleElements(elements.length);
//
//			// Create a copy of that, with the desired value for each key
//			Collection<Map.Entry<K, V>> entries = new ArrayList<Entry<K, V>>(elements.length);
//			int i = 0;
//			for (Map.Entry<K, V> entry : originalEntries) {
//				entries.add(Helpers.mapEntry(keysArray[i++], entry.getValue()));
//			}
//
//			return mapGenerator.create(entries.toArray()).keySet();
//		}
//
//		@Override
//		public K[] createArray(int length) {
//			// TODO(George): with appropriate refactoring of OneSizeGenerator, we
//			// can perhaps tidy this up and get rid of the casts here and in
//			// MapValueCollectionGenerator.
//
//			// noinspection UnnecessaryLocalVariable
//			@SuppressWarnings("unchecked")
//			final K[] ks = ((TestMapGenerator<K, V>) mapGenerator.getInnerGenerator()).createKeyArray(length);
//			return ks;
//		}
//
//		@Override
//		public Iterable<K> order(List<K> insertionOrder) {
//			return insertionOrder;
//		}
//	}
}
