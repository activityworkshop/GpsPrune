package tim.prune.data;

/**
 * Abstract class to hold static calculation functions
 * for gradient (like glide slope)
 */
public abstract class GradientCalculator
{
	/**
	 * Calculate the gradient value of the track at the specified index
	 * @param inTrack track object
	 * @param inIndex index of point to calculate gradient for
	 * @param inValue object in which to place result of calculation
	 */
	public static void calculateGradient(Track inTrack, int inIndex, SpeedValue inValue)
	{
		if (inValue != null)
		{
			inValue.setInvalid();
		}
		if (inTrack == null || inIndex < 0 || inValue == null)
		{
			System.err.println("Cannot calculate gradient for index " + inIndex);
			return;
		}

		// If no altitude or it's a waypoint then no gradient either
		DataPoint point = inTrack.getPoint(inIndex);
		if (point == null || !point.hasAltitude() || point.isWaypoint()) {
			return;
		}

		// If the point has horizontal and vertical speeds already then just use those
		if (point.hasHSpeed() && point.hasVSpeed()) {
			inValue.setValue(point.getVSpeed().getValueInMetresPerSec() / point.getHSpeed().getValueInMetresPerSec());
		}
		else if (!point.getSegmentStart())
		{
			// Use the previous track point and the next track point
			DataPoint p = inTrack.getPreviousTrackPoint(inIndex-1);
			DataPoint q = inTrack.getNextTrackPoint(inIndex+1);
			if (p != null && q != null && !q.getSegmentStart()
				&& p.hasAltitude() && q.hasAltitude())
			{
				final double horizRads = DataPoint.calculateRadiansBetween(p, point) +
					DataPoint.calculateRadiansBetween(point, q);
				final double horizDist = Distance.convertRadiansToDistance(horizRads, UnitSetLibrary.UNITS_METRES);
				final double heightDiff = q.getAltitude().getMetricValue() - p.getAltitude().getMetricValue();
				// Get gradient in radians
				final double gradient = Math.atan2(heightDiff, horizDist);
				inValue.setValue(gradient);
			}
		}
		// otherwise, just leave value as invalid
	}
}
