package com.google.common.collect.testfw;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ConcurrentHashMultimap;
import com.google.common.collect.ConcurrentSetMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.testing.MultimapTestSuiteBuilder;
import com.google.common.primitives.Ints;

public class ConcurrentHashMultimapTest {
	ConcurrentSetMultimap<String, Integer> multimap;
	@Before
	public void setUp() throws Throwable {
		multimap = ConcurrentHashMultimap.create();
	}
	
	@Test
	public void testSerializedSimpleGet() throws Exception {
		multimap.put("a", 1);
		multimap = MultimapTestSuiteBuilder.reserialize(multimap);
		assertTrue(multimap.containsKey("a"));
		assertTrue(multimap.containsValue(1));
		assertThat(multimap.get("a"), is(setOf(1)));
		assertThat(multimap.size(), is(1));
	}

	@Test
	public void testSingleAdd() throws Exception {
		multimap.put("a", 1);
		assertThat(multimap.size(), is(1));
		assertThat(multimap.get("a"), is(setOf(1)));
	}
	@Test
	public void testMultiAddSet() throws Exception {
		multimap.putAll("a", setOf(1, 2, 3));
		assertThat(multimap.size(), is(3));
		assertThat(multimap.get("a"), is(setOf(1, 2, 3)));
	}
	@Test
	public void testMultiAddSingle() throws Exception {
		multimap.put("a", 1);
		multimap.put("a", 2);
		assertThat(multimap.size(), is(2));
		assertThat(multimap.get("a"), is(setOf(1, 2)));
	}
	@Test
	public void testSingleAddRemove() throws Exception {
		multimap.put("a", 1);
		multimap.remove("a", 1);
		assertThat(multimap.size(), is(0));
		assertThat(multimap.get("a"), is(setOf()));
	}
	@Test
	public void testSingleAddRemoveAll() throws Exception {
		multimap.put("a", 1);
		multimap.removeAll("a");
		assertThat(multimap.size(), is(0));
		assertThat(multimap.get("a"), is(setOf()));
	}
	@Test
	public void testAddModifiesEmptyView() throws Exception {
		Set<Integer> view = multimap.get("a");
		assertThat(view.size(), is(0));
		
		multimap.put("a", 1);
		assertThat(view.size(), is(1));
		assertThat(view, is(setOf(1)));
		
		multimap.put("a", 2);
		assertThat(view.size(), is(2));
		assertThat(view, is(setOf(1, 2)));
	}
	@Test
	public void testAddModifiesPopulatedView() throws Exception {
		multimap.put("a", 1);
		
		Set<Integer> view = multimap.get("a");
		assertThat(view.size(), is(1));
		assertThat(view, is(setOf(1)));
		
		multimap.put("a", 2);
		assertThat(view.size(), is(2));
		assertThat(view, is(setOf(1, 2)));
	}
	@Test
	public void testReplaceOne() throws Exception {
		multimap.put("a", 1);
		multimap.replaceValue("a", 1, 2);
		assertThat(multimap.size(), is(1));
		assertThat(multimap.get("a"), is(setOf(2)));
	}
	@Test
	public void testReplaceMany() throws Exception {
		multimap.put("a", 1);
		multimap.put("a", 2);
		multimap.replaceValues("a", setOf(3, 4));
		assertThat(multimap.size(), is(2));
		assertThat(multimap.get("a"), is(setOf(3, 4)));
	}
	@Test
	public void testKeySet() throws Exception {
		multimap.put("a", 1);
		multimap.put("a", 2);
		multimap.put("b", 3);
		
		Set<String> keySet = multimap.keySet();
		assertThat(keySet.size(), is(2));
	}
	
	@Test
	public void keySetIteratorTest() throws Exception {
		multimap.put("a", 1);
		multimap.put("a", 2);
		multimap.put("b", 3);
		
		ImmutableSet<String> keySetCopyViaIterator = ImmutableSet.copyOf(multimap.keySet().iterator());
		assertThat(keySetCopyViaIterator, is(setOf("a", "b")));
	}
	@Test
	public void keySetSize() throws Exception {
		multimap.put("a", 1);
		multimap.put("a", 2);
		multimap.put("b", 3);
		
		Set<String> keySet = multimap.keySet();
		assertTrue(keySet.containsAll(setOf("a", "b")));
	}
	@Test
	public void keySetContainsAll() throws Exception {
		multimap.put("a", 1);
		multimap.put("a", 2);
		multimap.put("b", 3);
		
		Set<String> keySet = multimap.keySet();
		assertTrue(keySet.containsAll(setOf("a", "b")));
	}
	
	@Test
	public void testToString() throws Exception {
		multimap.put("a", 1);
		multimap.put("a", 2);
		multimap.put("b", 3);
		
		System.out.println(multimap.toString());
	}

	private static Set<Integer> setOf(int ... ints) {
		return ImmutableSet.copyOf(Ints.asList(ints));
	}
	private static <T> Set<T> setOf(T ... objs) {
		return ImmutableSet.copyOf(objs);
	}
}
