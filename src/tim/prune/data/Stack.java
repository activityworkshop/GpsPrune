package tim.prune.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Replacement for java.util.Stack for a LIFO stack */
public class Stack<T>
{
	private final ArrayDeque<T> _queue = new ArrayDeque<>();

	public void clear() {
		_queue.clear();
	}

	public boolean isEmpty() {
		return _queue.isEmpty();
	}

	public int size() {
		return _queue.size();
	}

	public void add(T element) {
		_queue.addLast(element);
	}

	public T peek() {
		return _queue.peekLast();
	}

	public T pop() {
		return _queue.removeLast();
	}

	public List<T> asList()
	{
		ArrayList<T> result = new ArrayList<>();
		Iterator<T> iterator = _queue.descendingIterator();
		while (iterator.hasNext()) {
			result.add(iterator.next());
		}
		return result;
	}
}
