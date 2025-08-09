package tim.prune.function.compress.methods;

import tim.prune.data.DataPoint;
import tim.prune.data.MarkingData;
import tim.prune.data.NumberUtils;
import tim.prune.data.Track;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;

public class WackyPointsMethod extends CompressionMethod
{
	private final double _factor;

	public WackyPointsMethod(double factor) {
		_factor = factor;
	}

	public WackyPointsMethod(String inString) {
		_factor = NumberUtils.getDoubleOrZero(recogniseString(inString) ? inString.substring(4) : inString);
	}

	public CompressionMethodType getType() {
		return CompressionMethodType.WACKY_POINTS;
	}

	public String getParam() {
		return "" + Math.abs(_factor);
	}

	public int compress(Track inTrack, TrackDetails inDetails, MarkingData inMarkings)
	{
		if (_factor <= 0.0) {
			return 0;
		}
		int numPoints = inTrack.getNumPoints();
		int numDeleted = 0;
		double threshold = _factor * inDetails.getMeanRadians();
		DataPoint currPoint = null, prevPoint = null;
		// Loop over all points looking for points far away from neighbours
		for (int i=0; i<numPoints; i++)
		{
			if (inMarkings.isPointMarkedForDeletion(i)) {
				continue;
			}
			currPoint = inTrack.getPoint(i);
			// Don't delete any waypoints or photo points, or start/end of segments
			if (!currPoint.isWaypoint() && !currPoint.hasMedia()
				&& !inDetails.isSegmentStart(i) && !inDetails.isSegmentEnd(i))
			{
				// Measure distance from previous track point
				if (DataPoint.calculateRadiansBetween(prevPoint, currPoint) > threshold)
				{
					// Now need to find next track point, and measure distances
					DataPoint nextPoint = getNextTrackPoint(inTrack, i+1, inMarkings);
					if (nextPoint != null && DataPoint.calculateRadiansBetween(currPoint, nextPoint) > threshold
						&& DataPoint.calculateRadiansBetween(prevPoint, nextPoint) < threshold)
					{
						// Found a point to delete
						inMarkings.markPointForDeletion(i);
						numDeleted++;
					}
				}
			}
			// Remember last (not-deleted) track point
			if (!currPoint.isWaypoint() && !inMarkings.isPointMarkedForDeletion(i)) {
				prevPoint = currPoint;
			}
		}
		return numDeleted;
	}

	public String getSettingsString() {
		return getType().getKey() + _factor;
	}

	static boolean recogniseString(String inString) {
		return recogniseString(inString, CompressionMethodType.WACKY_POINTS);
	}
}
