package tim.prune.undo;

import java.util.Stack;

/**
 * Stack of undo operations
 * which also remembers how many times it's been cleared
 */
public class UndoStack extends Stack<UndoOperation>
{
	private int _numTimesDeleted = 0;

	/** @return number of times this stack has been deleted */
	public int getNumTimesDeleted() {
		return _numTimesDeleted;
	}

	@Override
	public void clear()
	{
		_numTimesDeleted++;
		super.clear();
	}
}
