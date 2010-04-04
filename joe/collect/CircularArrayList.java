/*
 * @(#)ArrayDeque.java	1.6 06/04/21
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package joe.collect;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.RandomAccess;
import java.util.Stack;

/**
 * Resizable-array implementation of the {@link Deque} and {@link List} interfaces, based on
 * {@link ArrayDeque}. Array deques have no capacity restrictions; they grow as necessary to support
 * usage. They are not thread-safe; in the absence of external synchronization, they do not support
 * concurrent access by multiple threads. Null elements are prohibited. This class is likely to be
 * faster than {@link Stack} when used as a stack, faster than {@link LinkedList} when used as a
 * queue and comparable to {@link ArrayList} when used as a list, with some substantially faster
 * operations, such as {@code remove(0)}.
 * <p>
 * Most operations run in amortized constant time. Exceptions include {@link #remove(Object) remove}, {@link #removeFirstOccurrence removeFirstOccurrence}, {@link #removeLastOccurrence
 * removeLastOccurrence}, {@link #contains contains}, {@link #iterator iterator.remove()}, the
 * indexed insert and remove operations and the bulk operations, all of which run in linear time.
 * <p>
 * The iterators returned by this class's <tt>iterator</tt> method are <i>fail-fast</i>: if the list
 * is modified at any time after the iterator is created, in any way except through the iterator's
 * own <tt>remove</tt> method, the iterator will generally throw a
 * {@link ConcurrentModificationException}. Thus, in the face of concurrent modification, the
 * iterator fails quickly and cleanly, rather than risking arbitrary, non-deterministic behavior at
 * an undetermined time in the future.
 * <p>
 * Note that the fail-fast behavior of an iterator cannot be guaranteed as it is, generally
 * speaking, impossible to make any hard guarantees in the presence of unsynchronized concurrent
 * modification. Fail-fast iterators throw <tt>ConcurrentModificationException</tt> on a best-effort
 * basis. Therefore, it would be wrong to write a program that depended on this exception for its
 * correctness: <i>the fail-fast behavior of iterators should be used only to detect bugs.</i>
 * <p>
 * This class and its iterator implement all of the <em>optional</em> methods of the
 * {@link Collection} and {@link Iterator} interfaces.
 * <p>
 * This class is a member of the <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 * 
 * @author Josh Bloch and Doug Lea
 * @author adapted from ArrayDeque by Joe Kearney
 * @param <E> the type of elements held in this collection
 */
public class CircularArrayList<E> extends AbstractList<E> implements Deque<E>, List<E>,
		RandomAccess, Cloneable, Serializable {
	/*
	 * subList is the only thing we get from AbstractList that isn't easy to implement.
	 * TODO remove this superclass?
	 */

	/*
	 * TERMINOLOGY
	 * 
	 * array index: index into the elements array
	 * list index: logical index into the structure, counting from head mod circularity
	 */

	/**
	 * The array in which the elements of the deque are stored.
	 * The capacity of the deque is the length of this array, which is
	 * always a power of two. The array is never allowed to become
	 * full, except transiently within an addX method where it is
	 * resized (see doubleCapacity) immediately upon becoming full,
	 * thus avoiding head and tail wrapping around to equal each
	 * other. We also guarantee that all array cells not holding
	 * deque elements are always null.
	 */
	transient E[] elements;
	
	/**
	 * The index of the element at the head of the deque (which is the
	 * element that would be removed by remove() or pop()); or an
	 * arbitrary number equal to tail if the deque is empty.
	 */
	transient int head;
	
	/**
	 * The index at which the next element would be added to the tail
	 * of the deque (via addLast(E), add(E), or push(E)).
	 */
	transient int tail;
	
	/**
	 * The minimum capacity that we'll use for a newly created deque.
	 * Must be a power of 2.
	 */
	private static final int MIN_INITIAL_CAPACITY = 8;
	
	// ****** Array allocation and resizing utilities ******
	
	/**
	 * Allocate empty array to hold the given number of elements.
	 * 
	 * @param numElements the number of elements to hold
	 */
	@SuppressWarnings("unchecked")
	// cast of Object[] to E[] is safe as we only ever put Es in it
	private void allocateElements(int numElements) {
		int initialCapacity = MIN_INITIAL_CAPACITY;
		// Find the best power of two to hold elements.
		// Tests "<=" because arrays aren't kept full.
		if (numElements >= initialCapacity) {
			initialCapacity = numElements;
			initialCapacity |= (initialCapacity >>> 1);
			initialCapacity |= (initialCapacity >>> 2);
			initialCapacity |= (initialCapacity >>> 4);
			initialCapacity |= (initialCapacity >>> 8);
			initialCapacity |= (initialCapacity >>> 16);
			initialCapacity++;
			
			if (initialCapacity < 0) // Too many elements, must back off
				initialCapacity >>>= 1;// Good luck allocating 2 ^ 30 elements
		}
		elements = (E[]) new Object[initialCapacity];
	}
	
	/**
	 * Double the capacity of this deque. Call only when full, i.e.,
	 * when head and tail have wrapped around to become equal.
	 */
	@SuppressWarnings("unchecked")
	// cast of Object[] to E[] is safe as we only ever put Es in it
	private void doubleCapacity() {
		assert head == tail;
		int p = head;
		int n = elements.length;
		int r = n - p; // number of elements to the right of p
		int newCapacity = n << 1;
		if (newCapacity < 0)
			throw new IllegalStateException("Sorry, deque too big");
		Object[] a = new Object[newCapacity];
		System.arraycopy(elements, p, a, 0, r);
		System.arraycopy(elements, 0, a, r, p);
		elements = (E[]) a;
		head = 0;
		tail = n;
	}
	
	/**
	 * Copies the elements from our element array into the specified array,
	 * in order (from first to last element in the deque). It is assumed
	 * that the array is large enough to hold all elements in the deque.
	 * 
	 * @param a target array
	 * @param <T> type of elements in the target array
	 * @return its argument
	 */
	private <T> T[] copyElements(T[] a) {
		if (head < tail) {
			System.arraycopy(elements, head, a, 0, size());
		} else if (head > tail) {
			int headPortionLen = elements.length - head;
			System.arraycopy(elements, head, a, 0, headPortionLen);
			System.arraycopy(elements, 0, a, headPortionLen, tail);
		}
		return a;
	}
	
	/**
	 * Constructs an empty array deque with an initial capacity
	 * sufficient to hold 16 elements.
	 */
	@SuppressWarnings("unchecked")
	// cast of Object[] to E[] is safe as we only ever put Es in it
	public CircularArrayList() {
		elements = (E[]) new Object[MIN_INITIAL_CAPACITY];
	}
	
	/**
	 * Constructs an empty array deque with an initial capacity
	 * sufficient to hold the specified number of elements.
	 * 
	 * @param numElements lower bound on initial capacity of the deque
	 */
	public CircularArrayList(int numElements) {
		allocateElements(numElements);
	}
	
	/**
	 * Constructs a deque containing the elements of the specified
	 * collection, in the order they are returned by the collection's
	 * iterator. (The first element returned by the collection's
	 * iterator becomes the first element, or <i>front</i> of the
	 * deque.)
	 * 
	 * @param c the collection whose elements are to be placed into the deque
	 * @throws NullPointerException if the specified collection is null
	 */
	public CircularArrayList(Collection<? extends E> c) {
		allocateElements(c.size());
		addAll(c);
	}
	
	// The main insertion and extraction methods are addFirst,
	// addLast, pollFirst, pollLast. The other methods are defined in
	// terms of these.
	
	/**
	 * Inserts the specified element at the front of this deque.
	 * 
	 * @param e the element to add
	 * @throws NullPointerException if the specified element is null
	 */
	public void addFirst(E e) {
		if (e == null)
			throw new NullPointerException();
		elements[head = (head - 1) & (elements.length - 1)] = e;
		if (head == tail)
			doubleCapacity();
	}
	
	/**
	 * Inserts the specified element at the end of this deque.
	 * <p>
	 * This method is equivalent to {@link #add}.
	 * 
	 * @param e the element to add
	 * @throws NullPointerException if the specified element is null
	 */
	public void addLast(E e) {
		if (e == null)
			throw new NullPointerException();
		elements[tail] = e;
		if ((tail = (tail + 1) & (elements.length - 1)) == head)
			doubleCapacity();
	}
	
	/**
	 * Inserts the specified element at the front of this deque.
	 * 
	 * @param e the element to add
	 * @return <tt>true</tt> (as specified by {@link Deque#offerFirst})
	 * @throws NullPointerException if the specified element is null
	 */
	public boolean offerFirst(E e) {
		addFirst(e);
		return true;
	}
	
	/**
	 * Inserts the specified element at the end of this deque.
	 * 
	 * @param e the element to add
	 * @return <tt>true</tt> (as specified by {@link Deque#offerLast})
	 * @throws NullPointerException if the specified element is null
	 */
	public boolean offerLast(E e) {
		addLast(e);
		return true;
	}
	
	/**
	 * @throws NoSuchElementException {@inheritDoc}
	 */
	public E removeFirst() {
		E x = pollFirst();
		if (x == null)
			throw new NoSuchElementException();
		return x;
	}
	
	/**
	 * @throws NoSuchElementException {@inheritDoc}
	 */
	public E removeLast() {
		E x = pollLast();
		if (x == null)
			throw new NoSuchElementException();
		return x;
	}
	
	public E pollFirst() {
		int h = head;
		E result = elements[h]; // Element is null if deque empty
		if (result == null)
			return null;
		elements[h] = null; // Must null out slot
		head = (h + 1) & (elements.length - 1);
		return result;
	}
	
	public E pollLast() {
		int t = (tail - 1) & (elements.length - 1);
		E result = elements[t];
		if (result == null)
			return null;
		elements[t] = null;
		tail = t;
		return result;
	}
	
	/**
	 * @throws NoSuchElementException {@inheritDoc}
	 */
	public E getFirst() {
		E x = elements[head];
		if (x == null)
			throw new NoSuchElementException();
		return x;
	}
	
	/**
	 * @throws NoSuchElementException {@inheritDoc}
	 */
	public E getLast() {
		E x = elements[(tail - 1) & (elements.length - 1)];
		if (x == null)
			throw new NoSuchElementException();
		return x;
	}
	
	public E peekFirst() {
		return elements[head]; // elements[head] is null if deque empty
	}
	
	public E peekLast() {
		return elements[(tail - 1) & (elements.length - 1)];
	}
	
	/**
	 * Removes the first occurrence of the specified element in this
	 * deque (when traversing the deque from head to tail).
	 * If the deque does not contain the element, it is unchanged.
	 * More formally, removes the first element <tt>e</tt> such that <tt>o.equals(e)</tt> (if such
	 * an element exists).
	 * Returns <tt>true</tt> if this deque contained the specified element
	 * (or equivalently, if this deque changed as a result of the call).
	 * 
	 * @param o element to be removed from this deque, if present
	 * @return <tt>true</tt> if the deque contained the specified element
	 */
	public boolean removeFirstOccurrence(Object o) {
		if (o == null)
			return false;
		int mask = elements.length - 1;
		int i = head;
		E x;
		while ((x = elements[i]) != null) {
			if (o.equals(x)) {
				delete(i);
				return true;
			}
			i = (i + 1) & mask;
		}
		return false;
	}
	
	/**
	 * Removes the last occurrence of the specified element in this
	 * deque (when traversing the deque from head to tail).
	 * If the deque does not contain the element, it is unchanged.
	 * More formally, removes the last element <tt>e</tt> such that <tt>o.equals(e)</tt> (if such an
	 * element exists).
	 * Returns <tt>true</tt> if this deque contained the specified element
	 * (or equivalently, if this deque changed as a result of the call).
	 * 
	 * @param o element to be removed from this deque, if present
	 * @return <tt>true</tt> if the deque contained the specified element
	 */
	public boolean removeLastOccurrence(Object o) {
		if (o == null)
			return false;
		int mask = elements.length - 1;
		int i = (tail - 1) & mask;
		E x;
		while ((x = elements[i]) != null) {
			if (o.equals(x)) {
				delete(i);
				return true;
			}
			i = (i - 1) & mask;
		}
		return false;
	}
	
	// *** List methods ***
	/**
	 * Converts an array index into the equivalent list index.
	 * 
	 * @param i array index to convert
	 * @param mask mask for the current array size
	 * @return list index for the element at array index {@code i}
	 */
	int getListIndexFromArrayIndex(int i, int mask) {
		return (i - head) & mask;
	}
	/**
	 * Converts a list index into the equivalent array index.
	 * 
	 * @param i list index to convert
	 * @param mask mask for the current array size
	 * @return array index for the element at list index {@code i}
	 */
	int getArrayIndexFromListIndex(int i, int mask) {
		return (i + head) & mask;
	}
	/**
	 * Gets the mask for the current array length.
	 * 
	 * @return the mask for modding array indices
	 */
	int getMask() {
		return elements.length - 1;
	}
	/**
	 * Inserts a gap into the elements array, resizing if necessary. head and tail pointers are
	 * updated. The shift distance induced in the {@code head} pointer is returned so that indices
	 * into the array may be maintained across this method call.
	 * 
	 * @param index index of the first element of the gap
	 * @param gapLength number of nulls to be inserted
	 * @return shift distance of {@code head} induced by this change
	 */
	int insertGap(int index, int gapLength) {
		checkInvariants();
		
		if (gapLength < 0) {
			throw new IllegalArgumentException("Attempted to insertGap() with negative length");
		}
		
		if (gapLength == 0) { // no change
			return 0;
		}
		
		/*
		 * TODO add cleverness to add gap on left if index < size()/2, moving the minimum number of
		 * elements on each copy.
		 */

		int arrayLength = elements.length;
		final int oldHead = head;
		
		if (isEmpty()) {
			if (gapLength >= arrayLength) {
				allocateElements(gapLength);
			}
			head = 0;
			tail = gapLength;
			return -oldHead;
		}
		
		int size = size();
		
		if (arrayLength > size + gapLength) { // array is big enough
			/*
			 * Four cases:
			 * h head, i index, t tail, g gap
			 * 
			 * A) [ h====i====t t+g ]
			 * B) [ t+g h====i====t ]
			 * C) [ ==i====t t+g h== ]
			 * D) [ ==t t+g h====i== ]
			 */

			int newTail = (tail + gapLength) & getMask();
			if (index <= tail) { // Case A, C: won't need to wrap elements
				System.arraycopy(elements, index, elements, index + gapLength, tail - index);
			} else {
				if (head < tail && newTail < arrayLength) { // Case B: not yet wrapped
					/*
					 * Allow equality both ends. addLast has index==tail
					 */
					assert head <= index && index <= tail;
					int remainder = arrayLength - tail;
					// copy [tail-remainder, arrayLength) to [0, remainder)
					// copy [index, tail-remainder) to [index + gapLength, arrayLength)
					System.arraycopy(elements, tail - remainder, elements, 0, tail - remainder);
					System.arraycopy(elements, index, elements, 0, remainder);
				} else { // Case D: already wrapped
					assert tail <= head && head <= index;
					// copy [0, tail) to [gapLength, tail+gapLength) length tail
					// copy [arrayLength-gapLength, arrayLength) to [0, gapLength) length gapLength
					// copy [index, arrayLength-gapLength) to [index+gapLength, arrayLength) length
					// ...arrayLength-(index+gapLength)
					System.arraycopy(elements, 0, elements, gapLength, tail);
					System.arraycopy(elements, arrayLength - gapLength, elements, 0, gapLength);
					System.arraycopy(elements, index, elements, index + gapLength, arrayLength
							- (index + gapLength));
				}
			}
			
			// clear out the arrayCopy source range
			nullifyRange(index, gapLength);
			
			if (gapLength < 0) {
				// clear out rest of the gapLength not overwritten by the arraycopy
				// nullifyRange(size, arrayLength);
			}
			tail = newTail;
		} else { // array is not big enough
			E[] oldElements = elements;
			allocateElements(size + gapLength);
			
			if (head < tail) {
				System.arraycopy(oldElements, head, elements, 0, tail - head);
			} else {
				System.arraycopy(oldElements, head, elements, 0, index - head);
				System.arraycopy(oldElements, 0, elements, index - head, tail - index);
			}
			
			// need to set head, tail correctly
			head = 0;
			tail = size + gapLength;
		}
		
		return oldHead - head;
	}
	/**
	 * Deletes a range from the elements array. head and tail pointers are updated. The shift
	 * distance induced in the {@code head} pointer is returned so that indices into the array may
	 * be maintained across this method call.
	 * <p>
	 * This method is useful in those {@link List} methods where {@link #delete(int)} doesn't return
	 * enough information, for example when we need to delete an element in an iterator having just
	 * called {@link ListIterator#previous()}.
	 * 
	 * @param index index of first element to remove
	 * @param rangeLength number of elements to remove
	 * @return shift distance of {@code head} induced by this change
	 */
	@SuppressWarnings("unchecked")
	// cast of Object[] to E[] is safe as we only ever put Es in it
	int deleteRange(int index, int rangeLength) {
		checkInvariants();
		
		// if (rangeLength == 0) { // no change
		// return 0;
		// }
		//		
		// if (rangeLength < 0) {
		// throw new IllegalArgumentException(
		// "Attempted to deleteRange() with negative range length");
		// }
		
		// if ((head < tail) && (index >= tail || index < head)) {
		// throw new IllegalArgumentException("Range to delete has invalid index: " + index);
		// } else if (index >= tail && index < head) {
		// throw new IllegalArgumentException("Range to delete has invalid index");
		// }
		//		
		int rangeEnd = (index + rangeLength) & getMask();
		// if ((head < tail) && (rangeEnd > tail || rangeEnd < head)) { // unwrapped case
		// throw new IllegalArgumentException("Range to delete is too long");
		// } else if (rangeEnd > tail && rangeEnd < head) { // wrapped case
		// throw new IllegalArgumentException("Range to delete is too long");
		// }
		
		if (isEmpty()) {
			return 0;
		}
		
		int oldHead = head;
		int size = size();
		
		if (rangeLength == size) {
			// remove everything, might as well resize
			elements = (E[]) new Object[MIN_INITIAL_CAPACITY];
			head = tail = 0;
			return -oldHead;
		}
		
		/*
		 * Four cases of where we find the interval to delete relative to the
		 * head and tail pointers:
		 * 
		 * h head, t tail, {==} range to delete
		 * 
		 * A) [ h===={==}==t ]
		 * B) [ =={==}=t h== ]
		 * C) [ ==t h=={==}= ]
		 * D) [ =}==t h=={== ]
		 */

		if (head < tail) { // case A
			int leftLength = index - head;
			int rightLength = tail - rangeEnd;
			if (leftLength > rightLength) { // shift left
				shiftSuffixLeft(index, rangeLength);
				checkInvariants();
				return 0; // head unmoved
			} else { // shift right
				shiftPrefixRight(index, rangeLength);
				checkInvariants();
				return rangeLength;
			}
		}
		
		// else we know tail <= head
		if (index < tail) { // case B
			shiftSuffixLeft(index, rangeLength);
			checkInvariants();
			return 0; // head unmoved
		}
		
		if (rangeEnd > head || rangeEnd == 0) { // case C
			shiftPrefixRight(index, rangeLength);
			checkInvariants();
			return rangeLength;
		} else { // case D
			int arrayLength = elements.length;
			int unwrappedSuffixLength = arrayLength - index;
			System.arraycopy(elements, rangeEnd, elements, index, unwrappedSuffixLength);
			
			int toMoveToZero = rangeEnd + unwrappedSuffixLength;
			System.arraycopy(elements, toMoveToZero, elements, 0, tail - toMoveToZero);
			tail -= rangeLength;
			nullifyRange(tail, rangeLength);
			checkInvariants();
			return 0;
		}
	}
	/**
	 * Moves the interval {@code [head, index)} right by distance {@code rangeLength}.
	 * 
	 * @param index destination index of first element to be overwritten by the moved range
	 * @param rangeLength number of elements to move
	 */
	private void shiftPrefixRight(int index, int rangeLength) {
		int oldHead = head;
		int newHead = oldHead + rangeLength; // don't need to mask
		System.arraycopy(elements, oldHead, elements, newHead, index - oldHead);
		nullifyRange(oldHead, rangeLength);
		head = newHead;
	}
	/**
	 * Moves the interval {@code [index+rangeLength, tail)} left by distance {@code rangeLength}.
	 * 
	 * @param index destination index of first element to be overwritten by the moved range
	 * @param rangeLength number of elements to move
	 */
	private void shiftSuffixLeft(int index, int rangeLength) {
		int newTail = tail - rangeLength; // don't need to mask
		int suffixLength = (tail - index) - rangeLength;
		System.arraycopy(elements, index + rangeLength, elements, index, suffixLength);
		nullifyRange(newTail, rangeLength);
		tail = newTail;
	}
	/**
	 * Sets a range of elements to {@code null}, with no range check.
	 * 
	 * @param start index of first {@code null}
	 * @param length index of first element not to be {@code null}ed
	 */
	private void nullifyRange(int start, int length) {
		int end = start + length;
		for (int i = start; i < end; i++) {
			elements[i] = null;
		}
	}

	/**
	 * Tests that the specified index is a valid list index, that is, between zero (inclusive) and
	 * {@link #size()} (exclusive).
	 * 
	 * @param index index to test
	 * @throws IndexOutOfBoundsException if the index is invalid
	 */
	void checkListRange(int index) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException("No such element index: " + index);
		}
	}
	/**
	 * Tests that the specified index is a valid list index for element insertion, that is, between
	 * zero (inclusive) and {@link #size()} (inclusive).
	 * 
	 * @param index index to test
	 * @throws IndexOutOfBoundsException if the index is invalid
	 */
	private void checkInsertListRange(int index) {
		if (index < 0 || index > size()) { // not >=
			throw new IndexOutOfBoundsException("Invalid element index: " + index);
		}
	}
	/**
	 * Tests that the specified object is non-null.
	 * 
	 * @param element object to check
	 * @throws NullPointerException if the parameter is {@code null}
	 */
	private static void checkNotNull(Object element) {
		if (element == null) {
			throw new NullPointerException("Null elements not supported");
		}
	}
	/**
	 * Copies the collection to an array, checking that no member elements are {@code null}.
	 * 
	 * @param <E> type of the element
	 * @param coll collection from which to copy elements
	 * @return array containing all elements in the collection in order as returned by the
	 *         collection's iterator
	 */
	private static <E> E[] nullSafeToArray(Collection<? extends E> coll) {
		@SuppressWarnings("unchecked")
		// cast of Object[] to E[] is safe as we only ever put Es in it
		E[] array = (E[]) new Object[coll.size()];
		int i = 0;
		for (E e : coll) {
			checkNotNull(e);
			array[i++] = e;
		}
		return array;
	}
	
	@Override
	public void add(int index, E element) {
		checkInsertListRange(index);
		checkNotNull(element);
		int arrayIndex = getArrayIndexFromListIndex(index, getMask());
		insertGap(arrayIndex, 1);
		elements[arrayIndex] = element;
	}
	@Override
	public boolean addAll(Collection<? extends E> c) {
		return addAll(size(), c);
	}
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		checkInsertListRange(index);
		if (c.isEmpty()) {
			return false;
		}

		E[] tmp = nullSafeToArray(c);
		int sizeToAdd = tmp.length;
		assert sizeToAdd == c.size();

		int mask = getMask();
		int arrayIndex = getArrayIndexFromListIndex(index, mask);
		int gapDiff = insertGap(arrayIndex, sizeToAdd);
		int insertIndex = (arrayIndex + gapDiff) & mask;
		int rangeEnd = (insertIndex + sizeToAdd) & mask;
		
		if (insertIndex < rangeEnd) { // do we need to wrap?
			System.arraycopy(tmp, 0, elements, insertIndex, sizeToAdd);
		} else { // yes
			int suffixLength = elements.length - insertIndex;
			System.arraycopy(tmp, 0, elements, insertIndex, suffixLength);
			System.arraycopy(tmp, suffixLength, elements, insertIndex + suffixLength, sizeToAdd
					- suffixLength);
		}
		return true;
	}
	@Override
	public E get(int index) {
		checkListRange(index);
		return elements[getArrayIndexFromListIndex(index, getMask())];
	}
	@Override
	public int indexOf(Object o) {
		if (o == null) {
			return -1;
		}
		int mask = getMask();
		int i = head;
		E x;
		while ((x = elements[i]) != null) {
			if (o.equals(x))
				return getListIndexFromArrayIndex(i, mask);
			i = (i + 1) & mask;
		}
		return -1;
	}
	@Override
	public int lastIndexOf(Object o) {
		if (o == null) {
			return -1;
		}
		int mask = getMask();
		int i = (tail - 1) & mask;
		E x;
		while ((x = elements[i]) != null) {
			if (o.equals(x))
				return getListIndexFromArrayIndex(i, mask);
			i = (i - 1) & mask;
		}
		return -1;
	}
	@Override
	public E remove(int index) {
		checkListRange(index);
		int arrayIndex = getArrayIndexFromListIndex(index, getMask());
		E old = elements[arrayIndex];
		delete(arrayIndex);
		return old;
	}
	@Override
	public E set(int index, E element) {
		checkNotNull(element);
		checkListRange(index);
		
		int arrayIndex = getArrayIndexFromListIndex(index, getMask());
		E old = elements[arrayIndex];
		elements[arrayIndex] = element;
		return old;
	}
	
	@Override
	public ListIterator<E> listIterator() {
		return new ListItr(head);
	}
	@Override
	public ListIterator<E> listIterator(int index) {
		checkInsertListRange(index); // allow zero on empty, for example
		return new ListItr(getArrayIndexFromListIndex(index, getMask()));
	}
	
	/**
	 * List iterator for the circular deque.
	 */
	private class ListItr implements ListIterator<E> {
		/**
		 * Index into the array. On calls to next(), the cursor is updated after the value to be
		 * returned is found. On calls to previous(), the opposite holds and the cursor is updated
		 * prior to looking up the value. Thus, repeated alternating calls to {@link #next()} and
		 * {@link #previous()} return the same value and repeatedly compute the
		 * next cursor.
		 */
		private int cursor;
		/**
		 * expected head pointer, updated when the iterator makes a structural change to the list
		 */
		private int expectedHead = head;
		/**
		 * expected tail pointer, updated when the iterator makes a structural change to the list
		 */
		private int expectedTail = tail;
		/**
		 * Index of element returned by most recent call to next or previous. Set to -2 on a call to
		 * {@link #add(Object)} or {@link #remove()}. Set to -1 on exhausting the iterator in either
		 * direction. Begins with value {@code -1}.
		 */
		private int lastRet;
		
		/**
		 * Creates a new list iterator with the specified start index.
		 * 
		 * @param startArrayIndex start index into the array, an array index not a list index
		 */
		ListItr(int startArrayIndex) {
			cursor = startArrayIndex;
			lastRet = -1;
		}
		
		@Override
		public boolean hasNext() {
			checkCoMod();
			
			// if (cursor == ((tail + 1) & getMask()) && cursor != head) {
			// return false;
			// }
			
			return cursor != -1 && cursor != tail;
		}
		@Override
		public E next() {
			checkCoMod();
			int mask = getMask();
			int index = cursor;
			
			if (index == tail || index == -1) {
				throw new NoSuchElementException();
			} else {
				lastRet = cursor;
				cursor = (cursor + 1) & mask;
				return elements[lastRet];
			}
		}
		@Override
		public int nextIndex() {
			if (cursor == tail) {
				return size();
			}
			return getListIndexFromArrayIndex(cursor, getMask());
		}
		
		@Override
		public boolean hasPrevious() {
			checkCoMod();
			return cursor != head && cursor != -1;
		}
		@Override
		public E previous() {
			checkCoMod();
			// check next value to be computed, not current position
			if (cursor == head || cursor == -1) {
				throw new NoSuchElementException();
			}
			lastRet = cursor = (cursor - 1) & getMask();
			return elements[lastRet];
		}
		@Override
		public int previousIndex() {
			if (cursor == head) {
				return -1;
			}
			
			int mask = getMask();
			return getListIndexFromArrayIndex((cursor - 1) & mask, mask);
		}
		
		@Override
		public void add(E e) {
			checkCoMod();
			cursor += insertGap(cursor, 1);
			expectedHead = head;
			expectedTail = tail;
			elements[cursor] = e;
			cursor = (cursor + 1) & getMask();
			lastRet = -2; // prevent immediate remove, in line with spec
		}
		@Override
		public void remove() {
			checkCoMod();
			if (lastRet < 0) {
				throw new IllegalStateException();
			}
			
			cursor = (lastRet + deleteRange(lastRet, 1)) & getMask();
			expectedHead = head;
			expectedTail = tail;
			
			lastRet = -2; // prevent immediate remove, in line with spec
		}
		@Override
		public void set(E e) {
			checkCoMod();
			if (lastRet < 0) {
				throw new IllegalStateException(
						"Neither next() nor previous() have been called yet");
			}
			CircularArrayList.this.elements[lastRet] = e;
		}
		/**
		 * Checks that there have been no structural modifications to the list outside this
		 * iterator.
		 * 
		 * @throws ConcurrentModificationException if the expected head and tail do not match the
		 *             actual values
		 */
		private void checkCoMod() {
			if (head != expectedHead && tail != expectedTail) {
				throw new ConcurrentModificationException();
			}
		}
	}
	
	// *** Queue methods ***
	
	/**
	 * Inserts the specified element at the end of this deque.
	 * <p>
	 * This method is equivalent to {@link #addLast}.
	 * 
	 * @param e the element to add
	 * @return <tt>true</tt> (as specified by {@link Collection#add})
	 * @throws NullPointerException if the specified element is null
	 */
	@Override
	public boolean add(E e) {
		addLast(e);
		return true;
	}
	
	/**
	 * Inserts the specified element at the end of this deque.
	 * <p>
	 * This method is equivalent to {@link #offerLast}.
	 * 
	 * @param e the element to add
	 * @return <tt>true</tt> (as specified by {@link Queue#offer})
	 * @throws NullPointerException if the specified element is null
	 */
	public boolean offer(E e) {
		return offerLast(e);
	}
	
	/**
	 * Retrieves and removes the head of the queue represented by this deque.
	 * This method differs from {@link #poll poll} only in that it throws an
	 * exception if this deque is empty.
	 * <p>
	 * This method is equivalent to {@link #removeFirst}.
	 * 
	 * @return the head of the queue represented by this deque
	 * @throws NoSuchElementException {@inheritDoc}
	 */
	public E remove() {
		return removeFirst();
	}
	
	/**
	 * Retrieves and removes the head of the queue represented by this deque
	 * (in other words, the first element of this deque), or returns <tt>null</tt> if this deque is
	 * empty.
	 * <p>
	 * This method is equivalent to {@link #pollFirst}.
	 * 
	 * @return the head of the queue represented by this deque, or <tt>null</tt> if this deque is
	 *         empty
	 */
	public E poll() {
		return pollFirst();
	}
	
	/**
	 * Retrieves, but does not remove, the head of the queue represented by
	 * this deque. This method differs from {@link #peek peek} only in
	 * that it throws an exception if this deque is empty.
	 * <p>
	 * This method is equivalent to {@link #getFirst}.
	 * 
	 * @return the head of the queue represented by this deque
	 * @throws NoSuchElementException {@inheritDoc}
	 */
	public E element() {
		return getFirst();
	}
	
	/**
	 * Retrieves, but does not remove, the head of the queue represented by
	 * this deque, or returns <tt>null</tt> if this deque is empty.
	 * <p>
	 * This method is equivalent to {@link #peekFirst}.
	 * 
	 * @return the head of the queue represented by this deque, or <tt>null</tt> if this deque is
	 *         empty
	 */
	public E peek() {
		return peekFirst();
	}
	
	// *** Stack methods ***
	
	/**
	 * Pushes an element onto the stack represented by this deque. In other
	 * words, inserts the element at the front of this deque.
	 * <p>
	 * This method is equivalent to {@link #addFirst}.
	 * 
	 * @param e the element to push
	 * @throws NullPointerException if the specified element is null
	 */
	public void push(E e) {
		addFirst(e);
	}
	
	/**
	 * Pops an element from the stack represented by this deque. In other
	 * words, removes and returns the first element of this deque.
	 * <p>
	 * This method is equivalent to {@link #removeFirst()}.
	 * 
	 * @return the element at the front of this deque (which is the top
	 *         of the stack represented by this deque)
	 * @throws NoSuchElementException {@inheritDoc}
	 */
	public E pop() {
		return removeFirst();
	}
	
	private void checkInvariants() {
		assert elements[tail] == null;
		assert head == tail ? elements[head] == null : (elements[head] != null && elements[(tail - 1)
				& (elements.length - 1)] != null);
		assert elements[(head - 1) & (elements.length - 1)] == null;
	}
	
	/**
	 * Removes the element at the specified position in the elements array,
	 * adjusting head and tail as necessary. This can result in motion of
	 * elements backwards or forwards in the array.
	 * <p>
	 * This method is called delete rather than remove to emphasize that its semantics differ from
	 * those of {@link List#remove(int)}.
	 * 
	 * @return true if elements moved backwards
	 */
	private boolean delete(int i) {
		checkInvariants();
		final E[] elements = this.elements;
		final int mask = elements.length - 1;
		final int h = head;
		final int t = tail;
		final int front = (i - h) & mask;
		final int back = (t - i) & mask;
		
		// Invariant: head <= i < tail mod circularity
		if (front >= ((t - h) & mask))
			throw new ConcurrentModificationException();
		
		// Optimize for least element motion
		if (front < back) {
			if (h <= i) {
				System.arraycopy(elements, h, elements, h + 1, front);
			} else { // Wrap around
				System.arraycopy(elements, 0, elements, 1, i);
				elements[0] = elements[mask];
				System.arraycopy(elements, h, elements, h + 1, mask - h);
			}
			elements[h] = null;
			head = (h + 1) & mask;
			return false;
		} else {
			if (i < t) { // Copy the null tail as well
				System.arraycopy(elements, i + 1, elements, i, back);
				tail = t - 1;
			} else { // Wrap around
				System.arraycopy(elements, i + 1, elements, i, mask - i);
				elements[mask] = elements[0];
				System.arraycopy(elements, 1, elements, 0, t);
				tail = (t - 1) & mask;
			}
			return true;
		}
	}
	
	// *** Collection Methods ***
	
	/**
	 * Returns the number of elements in this deque.
	 * 
	 * @return the number of elements in this deque
	 */
	@Override
	public int size() {
		int size = (tail - head) & (elements.length - 1);
		return size;
	}
	
	/**
	 * Returns <tt>true</tt> if this deque contains no elements.
	 * 
	 * @return <tt>true</tt> if this deque contains no elements
	 */
	@Override
	public boolean isEmpty() {
		return head == tail;
	}
	
	/**
	 * Returns an iterator over the elements in this deque. The elements
	 * will be ordered from first (head) to last (tail). This is the same
	 * order that elements would be dequeued (via successive calls to {@link #remove} or popped (via
	 * successive calls to {@link #pop}).
	 * 
	 * @return an iterator over the elements in this deque
	 */
	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}
	
	public Iterator<E> descendingIterator() {
		return new Iterator<E>() {
			private final ListIterator<E> delegate = listIterator((size() - 1) & getMask());
			@Override
			public E next() {
				return delegate.previous();
			}
			@Override
			public boolean hasNext() {
				return delegate.hasPrevious();
			}
			@Override
			public void remove() {
				delegate.remove();
			}
		};
	}
	
	/**
	 * Returns <tt>true</tt> if this deque contains the specified element.
	 * More formally, returns <tt>true</tt> if and only if this deque contains
	 * at least one element <tt>e</tt> such that <tt>o.equals(e)</tt>.
	 * 
	 * @param o object to be checked for containment in this deque
	 * @return <tt>true</tt> if this deque contains the specified element
	 */
	@Override
	public boolean contains(Object o) {
		if (o == null)
			return false;
		int mask = elements.length - 1;
		int i = head;
		E x;
		while ((x = elements[i]) != null) {
			if (o.equals(x))
				return true;
			i = (i + 1) & mask;
		}
		return false;
	}
	
	/**
	 * Removes a single instance of the specified element from this deque.
	 * If the deque does not contain the element, it is unchanged.
	 * More formally, removes the first element <tt>e</tt> such that <tt>o.equals(e)</tt> (if such
	 * an element exists).
	 * Returns <tt>true</tt> if this deque contained the specified element
	 * (or equivalently, if this deque changed as a result of the call).
	 * <p>
	 * This method is equivalent to {@link #removeFirstOccurrence}.
	 * 
	 * @param o element to be removed from this deque, if present
	 * @return <tt>true</tt> if this deque contained the specified element
	 */
	@Override
	public boolean remove(Object o) {
		return removeFirstOccurrence(o);
	}
	
	/**
	 * Removes all of the elements from this deque.
	 * The deque will be empty after this call returns.
	 */
	@Override
	public void clear() {
		int h = head;
		int t = tail;
		if (h != t) { // clear all cells
			head = tail = 0;
			int i = h;
			int mask = elements.length - 1;
			do {
				elements[i] = null;
				i = (i + 1) & mask;
			} while (i != t);
		}
	}
	
	/**
	 * Returns an array containing all of the elements in this deque
	 * in proper sequence (from first to last element).
	 * <p>
	 * The returned array will be "safe" in that no references to it are maintained by this deque.
	 * (In other words, this method must allocate a new array). The caller is thus free to modify
	 * the returned array.
	 * <p>
	 * This method acts as bridge between array-based and collection-based APIs.
	 * 
	 * @return an array containing all of the elements in this deque
	 */
	@Override
	public Object[] toArray() {
		return copyElements(new Object[size()]);
	}
	
	/**
	 * Returns an array containing all of the elements in this deque in
	 * proper sequence (from first to last element); the runtime type of the
	 * returned array is that of the specified array. If the deque fits in
	 * the specified array, it is returned therein. Otherwise, a new array
	 * is allocated with the runtime type of the specified array and the
	 * size of this deque.
	 * <p>
	 * If this deque fits in the specified array with room to spare (i.e., the array has more
	 * elements than this deque), the element in the array immediately following the end of the
	 * deque is set to <tt>null</tt>.
	 * <p>
	 * Like the {@link #toArray()} method, this method acts as bridge between array-based and
	 * collection-based APIs. Further, this method allows precise control over the runtime type of
	 * the output array, and may, under certain circumstances, be used to save allocation costs.
	 * <p>
	 * Suppose <tt>x</tt> is a deque known to contain only strings. The following code can be used
	 * to dump the deque into a newly allocated array of <tt>String</tt>:
	 * 
	 * <pre>
	 * String[] y = x.toArray(new String[0]);
	 * </pre>
	 * 
	 * Note that <tt>toArray(new Object[0])</tt> is identical in function to <tt>toArray()</tt>.
	 * 
	 * @param a the array into which the elements of the deque are to
	 *            be stored, if it is big enough; otherwise, a new array of the
	 *            same runtime type is allocated for this purpose
	 * @return an array containing all of the elements in this deque
	 * @throws ArrayStoreException if the runtime type of the specified array
	 *             is not a supertype of the runtime type of every element in
	 *             this deque
	 * @throws NullPointerException if the specified array is null
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		int size = size();
		if (a.length < size)
			a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
		copyElements(a);
		if (a.length > size)
			a[size] = null;
		return a;
	}
	
	// *** Object methods ***
	
	/**
	 * Returns a copy of this deque.
	 * 
	 * @return a copy of this deque
	 */
	@Override
	public CircularArrayList<E> clone() {
		try {
			CircularArrayList<E> result = (CircularArrayList<E>) super.clone();
			result.elements = Arrays.copyOf(elements, elements.length);
			return result;
			
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}
	
	/**
	 * Appease the serialization gods.
	 */
	private static final long serialVersionUID = 2340985798034038923L;
	
	/**
	 * Serialize this deque.
	 * 
	 * @serialData The current size (<tt>int</tt>) of the deque,
	 *             followed by all of its elements (each an object reference) in
	 *             first-to-last order.
	 */
	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
		
		// Write out size
		s.writeInt(size());
		
		// Write out elements in order.
		int mask = elements.length - 1;
		for (int i = head; i != tail; i = (i + 1) & mask)
			s.writeObject(elements[i]);
	}
	
	/**
	 * Deserialize this deque.
	 */
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		
		// Read in size and allocate array
		int size = s.readInt();
		allocateElements(size);
		head = 0;
		tail = size;
		
		// Read in all elements in the proper order.
		for (int i = 0; i < size; i++)
			elements[i] = (E) s.readObject();
	}
}
