package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.TrackInfo;

/**
 * Undo addition/subtraction of a time offset
 */
public class UndoAddTimeOffset implements UndoOperation
{
	/** Start and end indices of section */
	private int _startIndex, _endIndex;
	/** time offset in seconds */
	private long _timeOffset;


	/**
	 * Constructor
	 * @param inStart start index of section
	 * @param inEnd end index of section
	 * @param inOffset time offset in seconds
	 */
	public UndoAddTimeOffset(int inStart, int inEnd, long inOffset)
	{
		_startIndex = inStart;
		_endIndex = inEnd;
		_timeOffset = inOffset;
	}


	/**
	 * @return description of operation including number of points adjusted
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.addtimeoffset") + " (" + (_endIndex - _startIndex + 1) + ")";
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Perform the inverse operation
		inTrackInfo.getTrack().addTimeOffsetSeconds(_startIndex, _endIndex, -_timeOffset, true);
		UpdateMessageBroker.informSubscribers();
	}
}
