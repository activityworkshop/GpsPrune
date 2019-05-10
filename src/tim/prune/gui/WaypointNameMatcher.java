package tim.prune.gui;

import java.util.ArrayList;
import javax.swing.AbstractListModel;

import tim.prune.data.DataPoint;
import tim.prune.data.Track;

/**
 * Class to deal with the matching of waypoint names
 * and the representation in a list
 */
public class WaypointNameMatcher extends AbstractListModel<String>
{
	private ArrayList<DataPoint> _waypoints = null;
	private int _numPoints = 0;
	private String[] _waypointNames = null;
	private ArrayList<DataPoint> _matches = null;


	/**
	 * Initialisation giving Track object
	 * @param inTrack Track object
	 */
	public void init(Track inTrack)
	{
		// Get list of waypoints from track
		_waypoints = new ArrayList<DataPoint>();
		inTrack.getWaypoints(_waypoints);
		// Initialise match flags and waypoint names
		_numPoints = _waypoints.size();
		_waypointNames = new String[_numPoints];
		for (int i=0; i<_numPoints; i++) {
			_waypointNames[i] = _waypoints.get(i).getWaypointName().toLowerCase();
		}
		_matches = new ArrayList<DataPoint>();
		findMatches(null);
	}

	/**
	 * Search for the given term and collect the matches
	 * @param inSearch string to search for
	 */
	public void findMatches(String inSearch)
	{
		// Reset array
		_matches.clear();
		// Convert search to lower case to match name array
		String search = null;
		if (inSearch != null && !inSearch.equals("")) {
			search = inSearch.toLowerCase();
		}
		// Loop through waypoint names
		for (int i=0; i<_numPoints; i++)
		{
			if (search == null || _waypointNames[i].indexOf(search) >= 0)
			{
				_matches.add(_waypoints.get(i));
			}
		}
		fireChanged();
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize()
	{
		if (_numPoints == 0) return 0;
		return _matches.size();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public String getElementAt(int inIndex)
	{
		return _matches.get(inIndex).getWaypointName();
	}

	/**
	 * Get the waypoint at the given index
	 * @param inIndex index number, starting at 0
	 * @return DataPoint object
	 */
	public DataPoint getWaypoint(int inIndex)
	{
		return _matches.get(inIndex);
	}

	/**
	 * Fire event to notify that contents have changed
	 */
	public void fireChanged()
	{
		this.fireContentsChanged(this, 0, getSize()-1);
	}
}
