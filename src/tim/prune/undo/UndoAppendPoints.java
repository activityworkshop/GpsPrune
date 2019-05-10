package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo an operation (such as create marker waypoints)
 * in which a number of points were appended to the track
 */
public class UndoAppendPoints implements UndoOperation
{
	private int _previousTrackLength = -1;
	private int _numAppended = 0;


	/**
	 * Constructor
	 */
	public UndoAppendPoints(int inTrackLength)
	{
		_previousTrackLength = inTrackLength;
	}

	/**
	 * @param inNumPoints number of points appended to track
	 */
	public void setNumPointsAppended(int inNumPoints)
	{
		_numAppended = inNumPoints;
	}

	/**
	 * @return description of operation including number of points loaded
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.insert") + " (" + _numAppended + ")";
	}

	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// crop track to previous size
		inTrackInfo.getTrack().cropTo(_previousTrackLength);
		// clear selection
		inTrackInfo.getSelection().clearAll();
	}
}
