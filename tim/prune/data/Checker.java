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
		if (numPoints % 2 == 1) {return false;}
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
		while ((point=inTrack.getPoint(i)) != null && !point.getSegmentStart()) {
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
		while ((point=inTrack.getPoint(i)) != null && !point.getSegmentStart()) {
			i--;
		}
		return Math.max(i, 0);
	}
}
