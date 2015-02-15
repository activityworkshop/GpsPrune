package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Undo cut and move of a track section
 */
public class UndoCutAndMove implements UndoOperation
{
	/** Start and end indices of section */
	private int _startIndex, _endIndex;
	/** Index of move to point */
	private int _moveToIndex;
	/** First track point in section, next track point after where it was */
	private DataPoint _firstTrackPoint = null, _followingTrackPoint = null;
	/** Next track point after where it's being moved to */
	private DataPoint _moveTrackPoint = null;
	/** Segment flags for these points */
	private boolean _firstSegmentFlag, _followingSegmentFlag, _moveToSegmentFlag;


	/**
	 * Constructor
	 * @param inTrack track object for copying segment flags
	 * @param inStart start index of section
	 * @param inEnd end index of section
	 * @param inMoveTo index of moveTo point
	 */
	public UndoCutAndMove(Track inTrack, int inStart, int inEnd, int inMoveTo)
	{
		_startIndex = inStart;
		_endIndex = inEnd;
		_moveToIndex = inMoveTo;
		// Look for first track point in section to be moved, store flag
		_firstTrackPoint = inTrack.getNextTrackPoint(inStart);
		if (_firstTrackPoint != null) {
			_firstSegmentFlag = _firstTrackPoint.getSegmentStart();
		}
		// Look for following track point, store flag
		_followingTrackPoint = inTrack.getNextTrackPoint(inEnd + 1);
		if (_followingTrackPoint != null) {
			_followingSegmentFlag = _followingTrackPoint.getSegmentStart();
		}
		// Look for next track point after move point, store flag
		_moveTrackPoint = inTrack.getNextTrackPoint(inMoveTo);
		if (_moveTrackPoint != null) {
			_moveToSegmentFlag = _moveTrackPoint.getSegmentStart();
		}
	}


	/**
	 * @return description of operation including number of points moved
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.cutandmove") + " (" + (_endIndex - _startIndex + 1) + ")";
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Cut and move the section back to where it was before
		int numMoved = _endIndex - _startIndex + 1;
		// Calculate new positions depending on whether section was moved forward or backward
		if (_startIndex > _moveToIndex)
		{
			inTrackInfo.getTrack().cutAndMoveSection(_moveToIndex, _moveToIndex + numMoved - 1, _startIndex + numMoved);
		}
		else
		{
			inTrackInfo.getTrack().cutAndMoveSection(_moveToIndex - numMoved, _moveToIndex - 1, _startIndex);
		}
		// Restore segment start flags
		if (_firstTrackPoint != null) {
			_firstTrackPoint.setSegmentStart(_firstSegmentFlag);
		}
		if (_followingTrackPoint != null) {
			_followingTrackPoint.setSegmentStart(_followingSegmentFlag);
		}
		if (_moveTrackPoint != null) {
			_moveTrackPoint.setSegmentStart(_moveToSegmentFlag);
		}
		inTrackInfo.getSelection().clearAll();
		UpdateMessageBroker.informSubscribers();
	}
}
