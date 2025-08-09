package tim.prune.function.compress.methods;

import tim.prune.data.DataPoint;
import tim.prune.data.MarkingData;
import tim.prune.data.NumberUtils;
import tim.prune.data.Track;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;
import tim.prune.function.compress.XYpoint;

public class DouglasPeuckerMethod extends CompressionMethod
{
	private final int _factor;

	public DouglasPeuckerMethod(int factor) {
		_factor = factor;
	}

	public DouglasPeuckerMethod(String inString) {
		_factor = recogniseString(inString) ? NumberUtils.getIntOrZero(inString.substring(4)) : (int) NumberUtils.getDoubleOrZero(inString);
	}

	public CompressionMethodType getType() {
		return CompressionMethodType.DOUGLAS_PEUCKER;
	}

	public String getParam() {
		return "" + Math.abs(_factor);
	}

	public int compress(Track inTrack, TrackDetails inDetails, MarkingData inMarkings)
	{
		// Parse parameter
		double param = _factor <= 0 ? 1.0 : (1.0 / _factor);
		double threshold = inDetails.getTrackSpan() * param;

		int numPoints = inTrack.getNumPoints();
		int origNumDeleted = inMarkings.getNumDeleted();
		// Convert inFlags into keepFlags
		int[] keepFlags = new int[numPoints];
		int segStart = -1, segEnd = -1;
		// Loop over all points in track
		for (int i=0; i<numPoints; i++)
		{
			DataPoint currPoint = inTrack.getPoint(i);
			if (currPoint.getSegmentStart())
			{
				// new segment found, so process previous one
				if (segStart > -1 && segEnd > segStart)
				{
					keepFlags[segEnd] = 1; // keep
					compressSegment(inTrack, keepFlags, segStart, segEnd, threshold);
					segStart = segEnd = -1;
				}
			}
			if (inMarkings.isPointMarkedForDeletion(i)) {
				keepFlags[i] = -1; // already deleted
			}
			else if (currPoint.isWaypoint() || currPoint.hasMedia() || currPoint.getSegmentStart()) {
				keepFlags[i] = 1; // keep
			}
			// Don't consider points which are already marked as deleted, ignore waypoints
			if (!inMarkings.isPointMarkedForDeletion(i) && !currPoint.isWaypoint())
			{
				// remember starts and ends
				if (segStart < 0) {segStart = i;}
				else {segEnd = i;}
			}
		}
		// Last segment, if any
		if (segStart >= 0 && segEnd > segStart) {
			keepFlags[segEnd] = 1; // keep
			compressSegment(inTrack, keepFlags, segStart, segEnd, threshold);
		}
		// Convert keepFlags back into inFlags
		for (int i=1; i<numPoints; i++)
		{
			if (keepFlags[i] < 1) {
				inMarkings.markPointForDeletion(i);
			}
		}
		return inMarkings.getNumDeleted() - origNumDeleted;
	}

	/**
	 * Compress the given segment (recursively)
	 * @param inTrack the track to compress
	 * @param inFlags int array of deletion flags for entire track
	 * @param inSegStart index of start of segment
	 * @param inSegEnd index of end of segment
	 * @param inThreshold threshold to use
	 */
	private void compressSegment(Track inTrack, int[] inFlags, int inSegStart, int inSegEnd,
		double inThreshold)
	{
		// System.out.println("Compress segment " + inSegStart + "-" + inSegEnd);
		final int numPoints = inSegEnd - inSegStart + 1;
		if (numPoints < 3) {
			 // segment too short to compress
			return;
		}
		// Calculate parameters of straight line between first and last
		XYpoint startxy = new XYpoint(inTrack.getX(inSegStart), inTrack.getY(inSegStart));
		XYpoint endxy = new XYpoint(inTrack.getX(inSegEnd), inTrack.getY(inSegEnd));
		XYpoint ab = startxy.vectorTo(endxy);
		final double dist2AB = ab.len2();
		// create unit vector perpendicular to AB
		final double distAB = ab.len();
		XYpoint perpendicular = new XYpoint(ab.y/distAB, -ab.x/distAB);
		// Check whether distAB is 0.0 - if so, find furthest point from startxy and compress from start to here and here to end
		if (distAB <= 0.0)
		{
			final int furthestIndex = getFurthestPointIndex(inTrack, inSegStart, inSegEnd);
			if (furthestIndex > inSegStart)
			{
				compressSegment(inTrack, inFlags, inSegStart, furthestIndex, inThreshold);
				compressSegment(inTrack, inFlags, furthestIndex, inSegEnd, inThreshold);
			}
			return;
		}

		double maxDist = -1.0;
		int furthestIndex = -1;
		for (int i=inSegStart+1; i<inSegEnd; i++)
		{
			if (inFlags[i] == 0) // unknown status
			{
				XYpoint currPoint = new XYpoint(inTrack.getX(i), inTrack.getY(i));
				XYpoint ac = startxy.vectorTo(currPoint);
				double distAP = ab.dot(ac) / dist2AB;
				// calc distance from point to line depending on distAP
				final double dist;
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
			compressSegment(inTrack, inFlags, inSegStart, furthestIndex, inThreshold);
			compressSegment(inTrack, inFlags, furthestIndex, inSegEnd, inThreshold);
		}
	}

	/**
	 * Find the index of the point furthest away from the start and end points
	 * @param inTrack the track to compress
	 * @param inStartIndex start index of segment to check
	 * @param inEndIndex end index of segment to check
	 * @return index of furthest point, or -1 if none found
	 */
	private int getFurthestPointIndex(Track inTrack, int inStartIndex, int inEndIndex)
	{
		int furthestIndex = -1;
		if (inStartIndex >= 0 && inEndIndex > inStartIndex)
		{
			final DataPoint startPoint = inTrack.getPoint(inStartIndex);
			double maxDist = 0.0;
			// Loop over points between start and end
			for (int i=inStartIndex+1; i<inEndIndex; i++)
			{
				DataPoint p = inTrack.getPoint(i);
				if (p.isWaypoint()) {
					continue;
				}
				double distFromStart = DataPoint.calculateRadiansBetween(startPoint, p);
				if (distFromStart > maxDist)
				{
					furthestIndex = i;
					maxDist = distFromStart;
				}
			}
		}
		return furthestIndex;
	}

	public String getSettingsString() {
		return getType().getKey() + _factor;
	}

	static boolean recogniseString(String inString) {
		return recogniseString(inString, CompressionMethodType.DOUGLAS_PEUCKER);
	}
}
