package tim.prune.function.compress.methods;

import tim.prune.data.DataPoint;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;

public class DuplicatesMethod extends ParameterlessMethod
{
	/** Number of points before this one to consider as duplicates */
	private static final int NUM_POINTS_TO_BACKTRACK = 20;

	public int compress(Track inTrack, TrackDetails inDetails, MarkingData inMarkings)
	{
		int numPoints = inTrack.getNumPoints();
		int numDeleted = 0;
		// Loop over all points looking for duplicates
		for (int i=1; i<numPoints; i++)
		{
			// Don't delete points which are already marked as deleted, or the first point in a segment
			if (inMarkings.isPointMarkedForDeletion(i) || inDetails.isSegmentStart(i)) {
				continue;
			}
			DataPoint currPoint = inTrack.getPoint(i);
			// Don't delete any photo points or audio points
			if (currPoint.hasMedia()) {
				continue;
			}
			// loop over last few points before this one
			final int startIdx = Math.max(0, i - NUM_POINTS_TO_BACKTRACK);
			for (int j = startIdx; j<i; j++)
			{
				if (!inMarkings.isPointMarkedForDeletion(j) && currPoint.isDuplicate(inTrack.getPoint(j)))
				{
					inMarkings.markPointForDeletion(i);
					numDeleted++;
					break;
				}
			}
		}
		return numDeleted;
	}

	public CompressionMethodType getType() {
		return CompressionMethodType.DUPLICATES;
	}

	static boolean recogniseString(String inString) {
		return recogniseString(inString, CompressionMethodType.DUPLICATES);
	}
}
