package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.TrackInfo;
import tim.prune.function.edit.FieldEditList;

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
		String newName = null;
		if (_undoFieldList.getEdit(0).getField() == Field.WAYPT_NAME)
			newName = _undoFieldList.getEdit(0).getValue();
		String pointName = _originalPoint.getWaypointName();
		if (newName != null && !newName.equals(""))
			desc = desc + " " + newName;
		else if (pointName != null && !pointName.equals(""))
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
		if (!inTrackInfo.getTrack().editPoint(_originalPoint, _undoFieldList, true))
		{
			// throw exception if failed
			throw new UndoException(getDescription());
		}
	}
}