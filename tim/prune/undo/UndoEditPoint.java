package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;
import tim.prune.edit.FieldEditList;

/**
 * Operation to undo the edit of a single point
 */
public class UndoEditPoint implements UndoOperation
{
	private DataPoint _originalPoint = null;
	private FieldEditList _undoFieldList = null;


	/**
	 * Constructor
	 * @param inPoint data point
	 * @param inUndoFieldList FieldEditList for undo operation
	 */
	public UndoEditPoint(DataPoint inPoint, FieldEditList inUndoFieldList)
	{
		_originalPoint = inPoint;
		_undoFieldList = inUndoFieldList;
	}


	/**
	 * @return description of operation including point name if any
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.editpoint");
		String pointName = _originalPoint.getWaypointName();
		if (pointName != null && !pointName.equals(""))
			desc = desc + " " + pointName;
		return desc;
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Restore contents of point into track
		if (!inTrackInfo.getTrack().editPoint(_originalPoint, _undoFieldList))
		{
			// throw exception if failed
			throw new UndoException(getDescription());
		}
	}
}