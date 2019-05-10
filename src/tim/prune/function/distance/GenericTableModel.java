package tim.prune.function.distance;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;

/**
 * General table model class for the two models in the distance function
 */
public abstract class GenericTableModel extends AbstractTableModel
{
	/** list of points */
	protected ArrayList<DataPoint> _pointList = null;
	/** Column heading */
	private static String _currPointLabel = I18nManager.getText("dialog.distances.currentpoint");

	/**
	 * Initialize the table model with the point list
	 * @param inPointList list of points
	 */
	public void init(ArrayList<DataPoint> inPointList)
	{
		_pointList = inPointList;
	}

	/**
	 * @return row count
	 */
	public int getRowCount()
	{
		if (_pointList == null) {return 0;}
		return _pointList.size();
	}

	/**
	 * Get the name of the specified point from the list
	 * @param inIndex index of point
	 * @return waypoint name if waypoint, otherwise "current point"
	 */
	protected String getPointName(int inIndex)
	{
		if (_pointList == null) {return "null";}
		DataPoint point = _pointList.get(inIndex);
		if (point.isWaypoint()) {return point.getWaypointName();}
		return _currPointLabel;
	}
}
