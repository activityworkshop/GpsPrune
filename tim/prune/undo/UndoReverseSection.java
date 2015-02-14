package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Undo reversal of track section
 */
public class UndoReverseSection implements UndoOperation
{
	/** Start and end indices of section */
	private int _startIndex, _endIndex;
	/** First and last track point in section and next track point after */
	private DataPoint _firstTrackPoint, _lastTrackPoint, _nextTrackPoint;
	/** Segment flags for these points */
	private boolean _firstSegmentFlag, _lastSegmentFlag, _nextSegmentFlag;


	/**
	 * Constructor
	 * @param inTrack track object for copying segment flags
	 * @param inStart start index of section
	 * @param inEnd end index of section
	 */
	public UndoReverseSection(Track inTrack, int inStart, int inEnd)
	{
		_startIndex = inStart;
		_endIndex = inEnd;
		// Look for first track point in section to be reversed, store flag
		_firstTrackPoint = inTrack.getNextTrackPoint(inStart);
		if (_firstTrackPoint != null) {
			_firstSegmentFlag = _firstTrackPoint.getSegmentStart();
		}
		// Look for last track point in section to be reversed, store flag
		_lastTrackPoint = inTrack.getPreviousTrackPoint(inEnd);
		if (_lastTrackPoint != null) {
			_lastSegmentFlag = _lastTrackPoint.getSegmentStart();
		}
		// Look for following track point, store flag
		_nextTrackPoint = inTrack.getNextTrackPoint(inEnd + 1);
		if (_nextTrackPoint != null) {
			_nextSegmentFlag = _nextTrackPoint.getSegmentStart();
		}
	}


	/**
	 * @return description of operation
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.reverse");
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		if (!inTrackInfo.getTrack().reverseRange(_startIndex, _endIndex))
		{
			throw new UndoException(getDescription());
		}
		// Restore segment start flags
		if (_firstTrackPoint != null) {
			_firstTrackPoint.setSegmentStart(_firstSegmentFlag);
		}
		if (_lastTrackPoint != null) {
			_lastTrackPoint.setSegmentStart(_lastSegmentFlag);
		}
		if (_nextTrackPoint != null) {
			_nextTrackPoint.setSegmentStart(_nextSegmentFlag);
		}
	}
}
