package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a delete of a single point
 */
public class UndoDeletePoint implements UndoOperation
{
	private int _pointIndex = -1;
	private DataPoint _point = null;


	/**
	 * Constructor
	 * @param inIndex index number of point within track
	 * @param inPoint data point
	 */
	public UndoDeletePoint(int inIndex, DataPoint inPoint)
	{
		_pointIndex = inIndex;
		_point = inPoint;
	}


	/**
	 * @return description of operation including point name if any
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.deletepoint");
		String pointName = _point.getWaypointName();
		if (pointName != null && !pointName.equals(""))
			desc = desc + " " + pointName;
		return desc;
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrack Track object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// restore point into track
		if (!inTrackInfo.getTrack().insertPoint(_point, _pointIndex))
		{
			throw new UndoException(getDescription());
		}
		// TODO: Reinsert photo into list if necessary
	}
}