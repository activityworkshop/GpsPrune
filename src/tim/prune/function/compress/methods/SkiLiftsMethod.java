package tim.prune.function.compress.methods;

import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.MarkingData;
import tim.prune.data.RangeStats;
import tim.prune.data.Track;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;

/** Remove the parts of the track which look like uphill ski lifts */
public class SkiLiftsMethod extends ParameterlessMethod
{
	private static final long SECONDS_TO_LOOK_FORWARD = 120;
	private static final int ALLOWED_METRES_DOWNHILL = 20;
	private static final int MINIMUM_ASCENT_FACTOR = 10;
	private static final double STRAIGHTNESS_FACTOR = 1.02;


	public int compress(Track inTrack, TrackDetails inDetails, MarkingData inMarkings)
	{
		final int numPoints = inTrack.getNumPoints();
		final int numAlreadyDeleted = inMarkings.getNumDeleted();
		int previousStartIndex = -1;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint currPoint = inTrack.getPoint(i);
			// Look forwards from point i to point n
			int n = i+1;
			DataPoint endPoint = inTrack.getPoint(n);
			while (endPoint != null
					&& endPoint.hasAltitude()
					&& endPoint.hasTimestamp()
					&& !endPoint.isWaypoint()
					&& endPoint.getTimestamp().getSecondsSince(currPoint.getTimestamp()) < SECONDS_TO_LOOK_FORWARD)
			{
				n++;
				endPoint = inTrack.getPoint(n);
			}
			// Found a suitable range to test with altitudes and timestamps
			if (looksLikeLiftRange(inTrack, i, n))
			{
				// Passes tests, so we want to mark all points between i and n
				int startIndex = i;
				// First check if we can merge with the previous marked range
				if (looksLikeLiftRange(inTrack, previousStartIndex, i)
					|| looksLikeLiftRange(inTrack, previousStartIndex, n))
				{
					startIndex = previousStartIndex; // merge
				}
				// Mark all points from startIndex <= x <= n
				for (int j=startIndex; j<=n; j++) {
					inMarkings.markPointForDeletion(j, true, true);
				}
				// Remember start point for next one
				previousStartIndex = startIndex;
				// skip forward half the range, don't need to test the same points again
				i = (i+n)/2;
			}
		}
		return inMarkings.getNumDeleted() - numAlreadyDeleted;
	}

	public CompressionMethodType getType() {
		return CompressionMethodType.SKI_LIFTS;
	}

	static boolean recogniseString(String inString) {
		return recogniseString(inString, CompressionMethodType.SKI_LIFTS);
	}

	/**
	 * Check whether the specified range looks like an uphill lift section or not
	 * Must go at most a little bit downhill, much more uphill than down, and straight
	 * (speed isn't checked yet, but maybe could be?)
	 * @param inTrack track
	 * @param inStartIndex start index of range
	 * @param inEndIndex end index of range
	 * @return true if it looks like a lift
	 */
	private boolean looksLikeLiftRange(Track inTrack, int inStartIndex, int inEndIndex)
	{
		// If the start index is negative, we haven't got a proper range (previous index not set)
		if (inStartIndex < 0) {
			return false;
		}
		// Check whether any points are present which can't be deleted (waypoints, points
		// without altitude or speed)
		for (int i=inStartIndex; i<=inEndIndex; i++)
		{
			DataPoint point = inTrack.getPoint(i);
			if (point == null || point.isWaypoint() || !point.hasAltitude() || !point.hasTimestamp()) {
				return false;
			}
		}
		RangeStats stats = new RangeStats(inTrack, inStartIndex, inEndIndex, 0);
		Unit unitMetres = UnitSetLibrary.UNITS_METRES;
		int descent = stats.getTotalAltitudeRange().getDescent(unitMetres);
		if (descent < ALLOWED_METRES_DOWNHILL)
		{
			int ascent = stats.getTotalAltitudeRange().getClimb(unitMetres);
			if (ascent > (descent * MINIMUM_ASCENT_FACTOR))
			{
				// Now check distance and compare to distance between start and end
				final DataPoint startPoint = inTrack.getPoint(inStartIndex);
				final DataPoint endPoint = inTrack.getPoint(inEndIndex);

				final double trackDist = stats.getTotalDistance(unitMetres);
				final double directRadians = DataPoint.calculateRadiansBetween(startPoint, endPoint);
				final double endToEndDist = Distance.convertRadiansToDistance(directRadians, unitMetres);
				// Check for straight(ish) line
				return (trackDist / endToEndDist) < STRAIGHTNESS_FACTOR;
			}
		}
		return false;
	}
}
