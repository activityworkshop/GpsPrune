package tim.prune.undo;

import java.util.Stack;

/**
 * Class to hold an undo operation together with a counter
 */
class UndoOpWithState
{
	public UndoOperation _undoOperation = null;
	public int           _undoCounter = 0;
	/** Constructor */
	public UndoOpWithState(UndoOperation inOp, int inCounter)
	{
		_undoOperation = inOp;
		_undoCounter   = inCounter;
	}
}

/**
 * Stack of undo operations
 * which also remembers how many undos have been performed
 */
public class UndoStack extends Stack<UndoOpWithState>
{
	/** Number of undos (and clears) already performed */
	private int _numUndos = 0;

	@Override
	public void clear()
	{
		_numUndos++;
		super.clear();
	}

	/** Add an undo operation to the stack */
	public synchronized boolean add(UndoOperation inOp)
	{
		return super.add(new UndoOpWithState(inOp, _numUndos));
	}

	/** Pop the latest operation from the stack */
	public synchronized UndoOperation popOperation()
	{
		_numUndos++;
		return super.pop()._undoOperation;
	}

	/** Get the operation at the given index */
	public UndoOperation getOperationAt(int inIndex)
	{
		return super.elementAt(inIndex)._undoOperation;
	}

	/** @return number of undos */
	public int getNumUndos()
	{
		if (isEmpty()) {return 0;}
		// Get the number of undos stored by the last operation on the stack
		return peek()._undoCounter;
	}
}
