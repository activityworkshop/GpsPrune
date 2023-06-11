package tim.prune.data;

import java.util.List;

/**
 * Class to provide checking functions
 */
public abstract class Checker
{
	/**
	 * Possible return values of double status
	 */
	public enum DoubleStatus {
		NOTHING, DOUBLED, DOUBLED_WAYPOINTS_TRACKPOINTS
	}

	/**
	 * Check if a given track is doubled, so that each point is given twice,
	 * once as waypoint and again as track point
	 * @param inPoints list of points
	 * @return enum value describing status
	 */
	public static DoubleStatus isDoubledTrack(List<DataPoint> inPoints)
	{
		// Check for empty track
		final int numPoints = (inPoints == null ? 0 : inPoints.size());
		if (numPoints < 2 || numPoints % 2 != 0) {
			return DoubleStatus.NOTHING;
		}
		// Loop through first half of track
		final int halfNum = numPoints / 2;
		boolean trackPointsAndWaypoints = true;
		for (int i=0; i<halfNum; i++)
		{
			DataPoint firstPoint = inPoints.get(i);
			DataPoint secondPoint = inPoints.get(i + halfNum);
			if (!firstPoint.getLatitude().equals(secondPoint.getLatitude())
				|| !firstPoint.getLongitude().equals(secondPoint.getLongitude()))
			{
				return DoubleStatus.NOTHING;
			}
			if (firstPoint.isWaypoint() == secondPoint.isWaypoint()) {
				trackPointsAndWaypoints = false;
			}
		}
		// Passed the test, so contents must all be doubled
		return trackPointsAndWaypoints ? DoubleStatus.DOUBLED_WAYPOINTS_TRACKPOINTS : DoubleStatus.DOUBLED;
	}

	/**
	 * Find the index of the next segment start after the given index
	 * @param inTrack track object
	 * @param inIndex current index
	 * @return index of next segment start
	 */
	public static int getNextSegmentStart(Track inTrack, int inIndex)
	{
		int i = inIndex + 1;
		DataPoint point = null;
		while ((point=inTrack.getPoint(i)) != null && (point.isWaypoint() || !point.getSegmentStart())) {
			i++;
		}
		return Math.min(i, inTrack.getNumPoints()-1);
	}

	/**
	 * Find the index of the previous segment start before the given index
	 * @param inTrack track object
	 * @param inIndex current index
	 * @return index of previous segment start
	 */
	public static int getPreviousSegmentStart(Track inTrack, int inIndex)
	{
		int i = inIndex - 1;
		DataPoint point = null;
		while ((point=inTrack.getPoint(i)) != null && (point.isWaypoint() || !point.getSegmentStart())) {
			i--;
		}
		// Have we gone past the beginning of the track?
		i = Math.max(i, 0);
		// count forwards past the waypoints if necessary
		while ((point=inTrack.getPoint(i)) != null && point.isWaypoint()) {
			i++;
		}
		return i;
	}

	/**
	 * Find the index of the last track point in the current segment
	 * @param inTrack track object
	 * @param inIndex current index
	 * @return index of next segment end
	 */
	public static int getNextSegmentEnd(Track inTrack, int inIndex)
	{
		// First, go to start of following segment, or the end of the track
		int i = getNextSegmentStart(inTrack, inIndex);
		// If it's the next segment, subtract one
		DataPoint point = inTrack.getPoint(i);
		if (point == null || point.getSegmentStart())
		{
			i--;
		}
		// Now we may be on a waypoint, so count back to get the last track point
		while ((point=inTrack.getPoint(i)) != null && point.isWaypoint()) {
			i--;
		}
		return Math.min(i, inTrack.getNumPoints()-1);
	}


	/**
	 * @param inTrack track object
	 * @return true if there is at least one waypoint with a timestamp
	 */
	public static boolean haveWaypointsGotTimestamps(Track inTrack)
	{
		if (inTrack != null)
		{
			for (int i=0; i<inTrack.getNumPoints(); i++)
			{
				DataPoint p = inTrack.getPoint(i);
				if (p != null && p.isWaypoint() && p.hasTimestamp())
				{
					return true;
				}
			}
		}
		return false;
	}
}
