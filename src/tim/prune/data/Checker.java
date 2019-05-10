package tim.prune.data;

/**
 * Class to provide checking functions
 */
public abstract class Checker
{

	/**
	 * Check if a given track is doubled, so that each point is given twice,
	 * once as waypoint and again as track point
	 * @param inTrack track to check
	 * @return true if track is doubled, false otherwise
	 */
	public static boolean isDoubledTrack(Track inTrack)
	{
		// Check for empty track
		if (inTrack == null || inTrack.getNumPoints() < 2) {return false;}
		// Check for non-even number of points
		final int numPoints = inTrack.getNumPoints();
		if (numPoints % 2 != 0) {return false;}
		// Loop through first half of track
		final int halfNum = numPoints / 2;
		for (int i=0; i<halfNum; i++)
		{
			DataPoint firstPoint = inTrack.getPoint(i);
			DataPoint secondPoint = inTrack.getPoint(i + halfNum);
			if (!firstPoint.getLatitude().equals(secondPoint.getLatitude())
				|| !firstPoint.getLongitude().equals(secondPoint.getLongitude())) {
				return false;
			}
		}
		// Passed the test, so contents must all be doubled
		return true;
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
