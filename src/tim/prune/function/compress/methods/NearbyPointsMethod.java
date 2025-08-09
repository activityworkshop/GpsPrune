package tim.prune.function.compress.methods;

import tim.prune.data.DataPoint;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;
import tim.prune.function.compress.TrackDetails;

public abstract class NearbyPointsMethod extends CompressionMethod
{
	public int compress(Track inTrack, TrackDetails inDetails, MarkingData inMarkings)
	{
		// Parse parameter
		final double radianThreshold = getRadianThreshold(inDetails);
		if (radianThreshold <= 0.0) {
			return 0;
		}

		// Loop over all points checking distances to previous point
		// TODO: Maybe this should also check distance to _next_ point as well!
		int numPoints = inTrack.getNumPoints();
		DataPoint prevPoint = null;
		int numDeleted = 0;
		for (int i=0; i<numPoints; i++)
		{
			// don't delete points already deleted
			if (inMarkings.isPointMarkedForDeletion(i)) {
				continue;
			}
			DataPoint currPoint = inTrack.getPoint(i);
			// Don't consider waypoints
			if (currPoint.isWaypoint()) {
				continue;
			}
			boolean deleted = false;
			// Don't delete any photo points or start/end of segments
			if (!currPoint.hasMedia()
				&& !isPointAtSegmentBoundary(i, inDetails, inMarkings)
				&& prevPoint != null)
			{
				// Check current point against prevPoint
				double radians = DataPoint.calculateRadiansBetween(prevPoint, currPoint);
				if (radians < radianThreshold)
				{
					inMarkings.markPointForDeletion(i);
					deleted = true;
					numDeleted++;
				}
			}
			if (!deleted) {
				prevPoint = currPoint;
			}
		}
		return numDeleted;
	}

	/** @return the radian threshold specified by the subclass */
	protected abstract double getRadianThreshold(TrackDetails inDetails);
}
