package joe.collect;

public final class ArrayBackedSet<E> extends AbstractArrayBackedSet<E> {
	@Override
	protected int getIndex(Object element) {
		return findByScan(element, getElementsArray());
	}
	@Override
	protected int getIndexForNewEntry(Object element) {
		return size();
	}
}
