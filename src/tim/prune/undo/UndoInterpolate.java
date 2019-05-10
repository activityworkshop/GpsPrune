package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo an interpolation
 */
public class UndoInterpolate implements UndoOperation
{
	private int _startIndex = 0;
	private int _totalInserted = 0;
	private DataPoint[] _points = null;


	/**
	 * Constructor
	 * @param inTrackInfo track info object
	 * @param inTotalInserted total number of points inserted
	 */
	public UndoInterpolate(TrackInfo inTrackInfo, int inTotalInserted)
	{
		_startIndex = inTrackInfo.getSelection().getStart();
		_points = inTrackInfo.cloneSelectedRange();
		_totalInserted = inTotalInserted;
	}


	/**
	 * @return description of operation including parameters
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.insert") + " (" + _totalInserted + ")";
	}


	/**
	 * Perform the undo operation on the given TrackInfo
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Work out how many points were in the track before the interpolation
		final int newSize = inTrackInfo.getTrack().getNumPoints() - _totalInserted;
		DataPoint[] oldPoints = inTrackInfo.getTrack().cloneContents();
		DataPoint[] newPoints = new DataPoint[newSize];

		// Restore track to previous values
		System.arraycopy(oldPoints, 0, newPoints, 0, _startIndex);
		System.arraycopy(_points, 0, newPoints, _startIndex, _points.length);
		int endIndex = _startIndex + _points.length;
		System.arraycopy(oldPoints, endIndex + _totalInserted, newPoints, endIndex, newSize - endIndex);

		inTrackInfo.getTrack().replaceContents(newPoints);
		// reset selection
		inTrackInfo.getSelection().clearAll();
	}
}
