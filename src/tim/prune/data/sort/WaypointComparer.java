package tim.prune.data.sort;

import java.util.Comparator;

import tim.prune.data.DataPoint;


/**
 * Class for comparing waypoints to sort them by name or timestamp
 */
public class WaypointComparer implements Comparator<DataPoint>
{
	/** Sort mode */
	private SortMode _sortMode;


	/**
	 * Constructor
	 * @param inMode sort mode
	 */
	public WaypointComparer(SortMode inMode)
	{
		_sortMode = inMode;
	}

	/**
	 * Main compare method
	 */
	public int compare(DataPoint inP1, DataPoint inP2)
	{
		if (inP2 == null || !inP2.isWaypoint()) return -1; // all nulls at end
		if (inP1 == null || !inP1.isWaypoint()) return 1;

		// Sort by time, if requested
		int result = 0;
		if (_sortMode == SortMode.SORTBY_TIME) {
			result = compareTimes(inP1, inP2);
		}
		// check names if names requested or if times didn't work
		if (result == 0) {
			result = inP1.getWaypointName().compareTo(inP2.getWaypointName());
		}
		// names and times equal, try longitude
		if (result == 0) {
			result = inP1.getLongitude().getDouble() > inP2.getLongitude().getDouble() ? 1 : -1;
		}
		// and latitude
		if (result == 0) {
			result = inP1.getLatitude().getDouble() > inP2.getLatitude().getDouble() ? 1 : -1;
		}
		return result;
	}

	/**
	 * Compare the timestamps of the two waypoints
	 * @param inP1 first point
	 * @param inP2 second point
	 * @return compare value (-1,0,1)
	 */
	private int compareTimes(DataPoint inP1, DataPoint inP2)
	{
		// Points might not have timestamps
		if (inP1.hasTimestamp() && !inP2.hasTimestamp()) return 1;
		if (!inP1.hasTimestamp() && inP2.hasTimestamp()) return -1;
		if (inP1.hasTimestamp() && inP2.hasTimestamp())
		{
			// Compare the timestamps
			long secDiff = inP1.getTimestamp().getMillisecondsSince(inP2.getTimestamp());
			return (secDiff<0?-1:(secDiff==0?0:1));
		}
		// neither has a timestamp
		return 0;
	}
}
