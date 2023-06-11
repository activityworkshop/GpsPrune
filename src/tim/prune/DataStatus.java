package tim.prune;

/**
 * Class to remember the current status of the data,
 * and make it possible to see whether the data has
 * changed in any way since the DataStatus was requested
 */
public class DataStatus
{
	private final int _undoSize;
	private final int _numUndos;

	/**
	 * Constructor
	 * @param inUndoSize current size of undo stack
	 * @param inNumUndos number of operations undone
	 */
	public DataStatus(int inUndoSize, int inNumUndos)
	{
		_undoSize = inUndoSize;
		_numUndos = inNumUndos;
	}

	/**
	 * Has the data changed compared to the previous status?
	 * @param inPreviousStatus previous status obtained from App
	 * @return true if the status is now different
	 */
	public boolean hasDataChanged(DataStatus inPreviousStatus)
	{
		return _undoSize != inPreviousStatus._undoSize
			|| _numUndos != inPreviousStatus._numUndos;
	}
}
