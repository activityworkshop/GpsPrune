package tim.prune.function.compress;

import tim.prune.App;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.RangeStats;
import tim.prune.data.Track;
import tim.prune.data.UnitSetLibrary;

/**
 * Function to mark all the points going uphill on ski lifts
 */
public class MarkLiftsFunction extends MarkAndDeleteFunction
{
	/**
	 * Constructor
	 * @param inApp App object
	 */
	public MarkLiftsFunction(App inApp)
	{
		super(inApp);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.marklifts";
	}

	/** this function _does_ require split at deleted points */
	protected boolean getShouldSplitSegments() {
		return true;
	}

	/**
	 * Begin the function using the set parameters
	 */
	public void begin()
	{
		// TODO: Might need to do this in a separate thread, it might take a while if the track is big
		// Loop over all points in track
		int numMarked = 0;
		final Track track = _app.getTrackInfo().getTrack();
		final int numPoints = track.getNumPoints();
		boolean[] markFlags = new boolean[numPoints];
		int previousStartIndex = -1;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint currPoint = track.getPoint(i);
			if (currPoint != null && !currPoint.isWaypoint() && currPoint.hasAltitude() && currPoint.hasTimestamp())
			{
				int n = i+1;
				DataPoint endPoint = track.getPoint(n);
				while (endPoint != null && endPoint.hasAltitude() && endPoint.hasTimestamp() &&
					!endPoint.isWaypoint() && endPoint.getTimestamp().getSecondsSince(currPoint.getTimestamp()) < 120)
				{
					n++;
					endPoint = track.getPoint(n);
				}
				if (endPoint != null && endPoint.hasAltitude() && endPoint.hasTimestamp() && !endPoint.isWaypoint()
					&& n > (i+10))
				{
					// Found a 2 minute range to test with at least 12 points
					if (looksLikeLiftRange(track, i, n))
					{
						// Passes tests, so we want to mark all points between i and n
						int startIndex = i;
						// First check if we can merge with the previous marked range
						if (previousStartIndex >= 0
							&& (looksLikeLiftRange(track, previousStartIndex, i)
							 || looksLikeLiftRange(track, previousStartIndex, n)))
						{
							startIndex = previousStartIndex; // merge
						}
						for (int j=startIndex; j<=n; j++)
						{
							markFlags[j] = true;
						}
						// Remember start point for next one
						previousStartIndex = startIndex;
						// skip forward half the range, don't need to test the same points again
						i = (i+n)/2;
					}
				}
			}
		}

		// Copy mark flags to points
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			if (!point.isWaypoint()) point.setMarkedForDeletion(markFlags[i]);
			if (markFlags[i]) numMarked++;
		}
		// Inform subscribers to update display
		UpdateMessageBroker.informSubscribers();
		// Confirm message showing how many marked
		if (numMarked > 0)
		{
			optionallyDeleteMarkedPoints(numMarked);
		}
		else
		{
			// TODO: Show message that no lifts were found
		}
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
		RangeStats stats = new RangeStats(inTrack, inStartIndex, inEndIndex);
		int descent = stats.getTotalAltitudeRange().getDescent(UnitSetLibrary.UNITS_METRES);
		if (descent < 20)
		{
			int ascent = stats.getTotalAltitudeRange().getClimb(UnitSetLibrary.UNITS_METRES);
			if (ascent > (descent * 10))
			{
				// Now check distance and compare to distance between start and end
				final DataPoint startPoint = inTrack.getPoint(inStartIndex);
				final DataPoint endPoint   = inTrack.getPoint(inEndIndex);

				final double trackDist = stats.getTotalDistance();
				final double endToEndDist = Distance.convertRadiansToDistance(
					DataPoint.calculateRadiansBetween(startPoint, endPoint));
				if ((trackDist / endToEndDist) < 1.02)  // Straight(ish) line
				{
					return true;
				}
				//else System.out.println("Not straight enough: " + (trackDist / endToEndDist));
			}
		}
		return false;
	}
}
