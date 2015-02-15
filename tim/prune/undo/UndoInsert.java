package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo an insertion (eg average)
 */
public class UndoInsert implements UndoOperation
{
	private int _startPosition = 0;
	private int _numInserted = 0;
	private boolean _hasSegmentFlag = false;
	private boolean _segmentFlag = false;


	/**
	 * Constructor without segment flag
	 * @param inStart start of insert
	 * @param inNumInserted number of points inserted
	 */
	public UndoInsert(int inStart, int inNumInserted)
	{
		this(inStart, inNumInserted, false, false);
	}


	/**
	 * Constructor with segment flag
	 * @param inStart start of insert
	 * @param inNumInserted number of points inserted
	 * @param inSegmentFlag segment flag of following point
	 */
	public UndoInsert(int inStart, int inNumInserted, boolean inSegmentFlag)
	{
		this(inStart, inNumInserted, true, inSegmentFlag);
	}


	/**
	 * Constructor
	 * @param inStart start of insert
	 * @param inNumInserted number of points inserted
	 * @param inHasFlag is there a segment flag present
	 * @param inFlag segment flag, if any
	 */
	public UndoInsert(int inStart, int inNumInserted, boolean inHasFlag, boolean inFlag)
	{
		_startPosition = inStart;
		_numInserted = inNumInserted;
		_hasSegmentFlag = inHasFlag;
		_segmentFlag = inFlag;
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
		if (_hasSegmentFlag) {
			DataPoint nextPoint = inTrackInfo.getTrack().getNextTrackPoint(_startPosition);
			if (nextPoint != null) {nextPoint.setSegmentStart(_segmentFlag);}
		}
		// reset selection
		inTrackInfo.getSelection().clearAll();
	}
}