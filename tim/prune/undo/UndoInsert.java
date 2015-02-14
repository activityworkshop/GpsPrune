package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo an insertion (eg interpolate)
 */
public class UndoInsert implements UndoOperation
{
	private int _startPosition = 0;
	private int _numInserted = 0;


	/**
	 * Constructor
	 * @param inStart start of insert
	 * @param inNumInserted number of points inserted
	 */
	public UndoInsert(int inStart, int inNumInserted)
	{
		_startPosition = inStart;
		_numInserted = inNumInserted;
	}


	/**
	 * @return description of operation including parameters
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.insert") + " (" + _numInserted + ")";
	}


	/**
	 * Perform the undo operation on the given TrackInfo
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// restore track to previous values
		inTrackInfo.getTrack().deleteRange(_startPosition, _startPosition + _numInserted - 1);
		// reset selection
		inTrackInfo.getSelection().select(_startPosition-1, _startPosition-1, _startPosition);
	}
}