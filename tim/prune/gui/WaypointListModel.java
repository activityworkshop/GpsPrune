package tim.prune.gui;

import java.util.ArrayList;
import javax.swing.AbstractListModel;

import tim.prune.data.DataPoint;
import tim.prune.data.Track;

/**
 * Class to act as list model for the waypoint list
 */
public class WaypointListModel extends AbstractListModel<String>
{
	Track _track = null;
	ArrayList<DataPoint> _waypoints = null;

	/**
	 * Constructor giving Track object
	 * @param inTrack Track object
	 */
	public WaypointListModel(Track inTrack)
	{
		_track = inTrack;
		_waypoints = new ArrayList<DataPoint>();
		_track.getWaypoints(_waypoints);
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize()
	{
		return _waypoints.size();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public String getElementAt(int inIndex)
	{
		DataPoint p = null;
		if (inIndex < 0 || inIndex >= getSize()
			|| _waypoints == null || (p = _waypoints.get(inIndex)) == null)
			return "";
		return p.getWaypointName();
	}

	/**
	 * Get the waypoint at the given index
	 * @param inIndex index number, starting at 0
	 * @return DataPoint object
	 */
	public DataPoint getWaypoint(int inIndex)
	{
		return _waypoints.get(inIndex);
	}

	/**
	 * Fire event to notify that contents have changed
	 */
	public void fireChanged()
	{
		_track.getWaypoints(_waypoints);
		this.fireContentsChanged(this, 0, getSize()-1);
	}
}
