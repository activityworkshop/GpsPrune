package tim.prune.function.compress.methods;

import tim.prune.data.DataPoint;
import tim.prune.data.MarkingData;
import tim.prune.data.NumberUtils;
import tim.prune.data.Track;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;

public class TooSoonMethod extends CompressionMethod
{
	private final int _secondsLimit;

	public TooSoonMethod(int inSeconds) {
		_secondsLimit = inSeconds;
	}

	public TooSoonMethod(String inString) {
		_secondsLimit = recogniseString(inString) ? NumberUtils.getIntOrZero(inString.substring(4)) : 0;
	}

	public CompressionMethodType getType() {
		return CompressionMethodType.TIME_DIFFERENCE;
	}

	public String getParam() {
		return "" + Math.abs(_secondsLimit);
	}

	public int compress(Track inTrack, TrackDetails inDetails, MarkingData inMarkings)
	{
		// Loop over all points checking time since previous point
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
			// Don't consider waypoints or points without timestamps
			if (currPoint.isWaypoint() || !currPoint.hasTimestamp()) {
				continue;
			}
			// Don't delete any photo points or start/end of segments
			if (!currPoint.hasMedia()
				&& !inDetails.isSegmentStart(i) && !inDetails.isSegmentEnd(i)
				&& prevPoint != null)
			{
				// Check current point against prevPoint
				long seconds = currPoint.getTimestamp().getSecondsSince(prevPoint.getTimestamp());
				if (seconds >= 0 && seconds < _secondsLimit)
				{
					inMarkings.markPointForDeletion(i);
					numDeleted++;
				}
			}
			if (!inMarkings.isPointMarkedForDeletion(i)) {
				prevPoint = currPoint;
			}
		}
		return numDeleted;
	}

	public String getSettingsString() {
		return getType().getKey() + _secondsLimit;
	}

	static boolean recogniseString(String inString) {
		return recogniseString(inString, CompressionMethodType.TIME_DIFFERENCE);
	}
}
