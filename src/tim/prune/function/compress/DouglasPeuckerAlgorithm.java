package tim.prune.function.compress;

import java.awt.Component;
import java.awt.event.ActionListener;

import tim.prune.data.DataPoint;
import tim.prune.data.Track;

/**
 * Douglas-Peucker algorithm for compresssion
 */
public class DouglasPeuckerAlgorithm extends SingleParameterAlgorithm
{
	/**
	 * Constructor
	 * @param inTrack track object
	 * @param inDetails track details object
	 * @param inListener listener to attach to activation control
	 */
	public DouglasPeuckerAlgorithm(Track inTrack, TrackDetails inDetails, ActionListener inListener)
	{
		super(inTrack, inDetails, inListener);
	}

	/**
	 * Perform the compression and work out which points should be deleted
	 * @param inFlags deletion flags from previous algorithms
	 * @return number of points deleted
	 */
	protected int compress(boolean[] inFlags)
	{
		// Parse parameter
		double param = getParameter();
		// Use 1/x if x greater than 1
		if (param > 1.0) param = 1.0 / param;
		if (param <= 0.0 || param >= 1.0) {
			// Parameter isn't valid, don't delete any
			return 0;
		}
		double threshold = _trackDetails.getTrackSpan() * param;

		int numPoints = _track.getNumPoints();
		int origNumDeleted = countFlags(inFlags);
		// Convert inFlags into keepFlags
		int[] keepFlags = new int[numPoints];
		int segStart = -1, segEnd = -1;
		// Loop over all points in track
		for (int i=0; i<numPoints; i++)
		{
			DataPoint currPoint = _track.getPoint(i);
			if (currPoint.getSegmentStart())
			{
				// new segment found, so process previous one
				if (segStart > -1 && segEnd > segStart)
				{
					keepFlags[segEnd] = 1; // keep
					compressSegment(keepFlags, segStart, segEnd, threshold);
					segStart = segEnd = -1;
				}
			}
			if (inFlags[i]) keepFlags[i] = -1; // already deleted
			else if (currPoint.isWaypoint() || currPoint.hasMedia() || currPoint.getSegmentStart()) {
				keepFlags[i] = 1; // keep
			}
			// Don't consider points which are already marked as deleted, ignore waypoints
			if (!inFlags[i] && !currPoint.isWaypoint())
			{
				// remember starts and ends
				if (segStart < 0) {segStart = i;}
				else {segEnd = i;}
			}
		}
		// Last segment, if any
		if (segStart >= 0 && segEnd > segStart) {
			keepFlags[segEnd] = 1; // keep
			compressSegment(keepFlags, segStart, segEnd, threshold);
		}
		// Convert keepFlags back into inFlags
		for (int i=1; i<numPoints; i++) {
			if (keepFlags[i] < 1) inFlags[i] = true;
		}
		return countFlags(inFlags) - origNumDeleted;
	}


	/**
	 * Count the number of true flags in the given array
	 * @param inFlags array of boolean flags
	 * @return number of flags which are set to true
	 */
	private static int countFlags(boolean[] inFlags)
	{
		int numDeleted = 0;
		for (int i=0; i<inFlags.length; i++) {
			if (inFlags[i]) numDeleted++;
		}
		return numDeleted;
	}

	/**
	 * Compress the given segment (recursively)
	 * @param inFlags int array of deletion flags for entire track
	 * @param inSegStart index of start of segment
	 * @param inSegEnd index of end of segment
	 * @param inThreshold threshold to use
	 */
	private void compressSegment(int[] inFlags, int inSegStart, int inSegEnd,
		double inThreshold)
	{
		// System.out.println("Compress segment " + inSegStart + "-" + inSegEnd);
		final int numPoints = inSegEnd - inSegStart + 1;
		if (numPoints < 3) {return;} // segment too short to compress
		// Calculate parameters of straight line between first and last
		XYpoint startxy = new XYpoint(_track.getX(inSegStart), _track.getY(inSegStart));
		XYpoint endxy = new XYpoint(_track.getX(inSegEnd), _track.getY(inSegEnd));
		XYpoint ab = startxy.vectorTo(endxy);
		final double dist2AB = ab.len2();
		// create unit vector perpendicular to AB
		final double distAB = ab.len();
		XYpoint perpendicular = new XYpoint(ab.y/distAB, -ab.x/distAB);
		// Check whether distAB is 0.0 - if so, find furthest point from startxy and compress from start to here and here to end
		if (distAB <= 0.0)
		{
			final int furthestIndex = getFurthestPointIndex(inSegStart, inSegEnd);
			if (furthestIndex > inSegStart)
			{
				compressSegment(inFlags, inSegStart, furthestIndex, inThreshold);
				compressSegment(inFlags, furthestIndex, inSegEnd, inThreshold);
			}
			return;
		}

		double maxDist = -1.0, dist = -1.0;
		int furthestIndex = -1;
		for (int i=inSegStart+1; i<inSegEnd; i++)
		{
			if (inFlags[i] == 0) // unknown status
			{
				XYpoint currPoint = new XYpoint(_track.getX(i), _track.getY(i));
				XYpoint ac = startxy.vectorTo(currPoint);
				double distAP = ab.dot(ac) / dist2AB;
				// calc distance from point to line depending on distAP
				if (distAP < 0.0) {
					dist = ac.len(); // outside line segment AB on the A side
				}
				else if (distAP > 1.0) {
					dist = endxy.vectorTo(currPoint).len(); // outside on the B side
				}
				else {
					// P lies between A and B so use dot product
					dist = Math.abs(perpendicular.dot(ac));
				}
				if (dist > maxDist)
				{
					maxDist = dist;
					furthestIndex = i;
				}
			}
		}
		// Check furthest point and see if it's further than the threshold
		if (maxDist > inThreshold)
		{
			inFlags[furthestIndex] = 1;
			// Make recursive calls for bit before and bit after kept point
			compressSegment(inFlags, inSegStart, furthestIndex, inThreshold);
			compressSegment(inFlags, furthestIndex, inSegEnd, inThreshold);
		}
	}


	/**
	 * @return specific gui components for dialog
	 */
	protected Component getSpecificGuiComponents()
	{
		return getSpecificGuiComponents("dialog.compress.douglaspeucker.paramdesc", "2000");
	}

	/**
	 * @return title key for box
	 */
	protected String getTitleTextKey()
	{
		return "dialog.compress.douglaspeucker.title";
	}

	/**
	 * Find the index of the point furthest away from the start and end points
	 * @param inStartIndex start index of segment to check
	 * @param inEndIndex end index of segment to check
	 * @return index of furthest point, or -1 if none found
	 */
	private int getFurthestPointIndex(int inStartIndex, int inEndIndex)
	{
		int furthestIndex = -1;
		if (inStartIndex >= 0 && inEndIndex > inStartIndex)
		{
			final DataPoint startPoint = _track.getPoint(inStartIndex);
			double maxDist = 0.0;
			// Loop over points between start and end
			for (int i=inStartIndex+1; i<inEndIndex; i++)
			{
				DataPoint p = _track.getPoint(i);
				if (!p.isWaypoint())
				{
					double distFromStart = DataPoint.calculateRadiansBetween(startPoint, p);
					if (distFromStart > maxDist)
					{
						furthestIndex = i;
						maxDist = distFromStart;
					}
				}
			}
		}
		return furthestIndex;
	}
}
