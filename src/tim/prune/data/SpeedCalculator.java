package tim.prune.data;

import tim.prune.config.Config;

/**
 * Abstract class to hold static calculation functions
 * for speed (and vertical speed)
 */
public abstract class SpeedCalculator
{
	/**
	 * Calculate the horizontal speed value of the track at the specified index
	 * @param inTrack track object
	 * @param inIndex index of point to calculate speed for
	 * @param inValue object in which to place result of calculation
	 */
	public static void calculateSpeed(Track inTrack, int inIndex, SpeedValue inValue)
	{
		if (inValue != null)
		{
			inValue.setInvalid();
		}
		if (inTrack == null || inIndex < 0 || inValue == null)
		{
			System.err.println("Cannot calculate speed for index " + inIndex);
			return;
		}

		DataPoint point = inTrack.getPoint(inIndex);
		if (point == null) {return;}
		boolean pointHasSpeed = false;
		double  speedValue = 0.0;

		// First, see if point has a speed value already
		if (point.hasHSpeed()) {
			speedValue = point.getHSpeed().getValue(Config.getUnitSet().getSpeedUnit());
			pointHasSpeed = true;
		}

		// otherwise, see if we can calculate it from the timestamps
		if (!pointHasSpeed && point.hasTimestamp() && !point.isWaypoint())
		{
			double totalRadians = 0.0;
			int index = inIndex-1;
			DataPoint p = null;
			DataPoint q = point;
			Timestamp earlyStamp = point.getTimestamp();
			boolean stop = false;

			// Count backwards until timestamp earlier than now; total distances back to this point
			if (!point.getSegmentStart())
			{
				do
				{
					p = inTrack.getPoint(index);
					boolean timeOk = p != null && p.hasTimestamp() && p.getTimestamp().isBefore(point.getTimestamp());
					boolean pValid = timeOk && !p.isWaypoint();
					if (pValid) {
						totalRadians += DataPoint.calculateRadiansBetween(p, q);
						earlyStamp = p.getTimestamp();
					}

					stop = (p == null) || p.getSegmentStart() || hasSufficientTimeDifference(p, point);
					index--;
					if (p != null && !p.isWaypoint()) {
						q = p;
					}
				}
				while (!stop);
			}
			// Count forwards until timestamp later than now; total distances forward to this point
			Timestamp lateStamp = point.getTimestamp();
			q = point;
			index = inIndex+1;
			do
			{
				p = inTrack.getPoint(index);
				boolean timeOk = p != null && p.hasTimestamp() && !p.getTimestamp().isBefore(point.getTimestamp());
				boolean pValid = timeOk && !p.isWaypoint() && !p.getSegmentStart();
				if (pValid) {
					totalRadians += DataPoint.calculateRadiansBetween(p, q);
					lateStamp = p.getTimestamp();
				}

				stop = (p == null) || p.getSegmentStart() || hasSufficientTimeDifference(point, p);
				index++;
				if (p != null && !p.isWaypoint()) {
					q = p;
				}
			}
			while (!stop);

			// See if we've managed to get a time range of at least a second
			long milliseconds = lateStamp.getMillisecondsSince(earlyStamp);
			if (milliseconds >= 1000L)
			{
				double dist = Distance.convertRadiansToDistance(totalRadians);
				// Store the value and maintain max and min values
				speedValue = dist / milliseconds * 1000.0 * 60.0 * 60.0; // convert from per millisec to per hour
				pointHasSpeed = true;
			}
		}
		// Did we get a value?
		if (pointHasSpeed)
		{
			inValue.setValue(speedValue);
		}
		// otherwise, just leave value as invalid
	}


	/**
	 * Calculate the vertical speed value of the track at the specified index
	 * @param inTrack track object
	 * @param inIndex index of point to calculate speed for
	 * @param inValue object in which to place the result of calculation
	 */
	public static void calculateVerticalSpeed(Track inTrack, int inIndex, SpeedValue inValue)
	{
		if (inTrack == null || inIndex < 0 || inValue == null) {
			System.err.println("Cannot calculate vert speed for index " + inIndex);
			return;
		}
		inValue.setInvalid();

		DataPoint point = inTrack.getPoint(inIndex);
		boolean pointHasSpeed = false;
		double  speedValue = 0.0;

		// First, see if point has a speed value already
		if (point != null && point.hasVSpeed())
		{
			speedValue = point.getVSpeed().getValue(Config.getUnitSet().getVerticalSpeedUnit());
			pointHasSpeed = true;
		}
		// otherwise, see if we can calculate it from the heights and timestamps
		if (!pointHasSpeed
			&& point != null && point.hasTimestamp() && point.hasAltitude() && !point.isWaypoint())
		{
			int index = inIndex-1;
			DataPoint p = null;
			Timestamp earlyStamp = point.getTimestamp();
			Altitude firstAlt = point.getAltitude();
			boolean stop = false;

			// Count backwards until timestamp earlier than now
			if (!point.getSegmentStart())
			{
				do
				{
					p = inTrack.getPoint(index);
					boolean timeOk = p != null && p.hasTimestamp() && p.getTimestamp().isBefore(point.getTimestamp());
					boolean pValid = timeOk && !p.isWaypoint();
					if (pValid) {
						earlyStamp = p.getTimestamp();
						if (p.hasAltitude()) firstAlt = p.getAltitude();
					}

					stop = (p == null) || p.getSegmentStart() || hasSufficientTimeDifference(p, point);
					index--;
				}
				while (!stop);
			}

			// Count forwards until timestamp later than now
			Timestamp lateStamp = point.getTimestamp();
			Altitude lastAlt = point.getAltitude();
			index = inIndex+1;
			do
			{
				p = inTrack.getPoint(index);
				boolean timeOk = p != null && p.hasTimestamp() && !p.getTimestamp().isBefore(point.getTimestamp());
				boolean pValid = timeOk && !p.isWaypoint() && !p.getSegmentStart();
				if (pValid) {
					lateStamp = p.getTimestamp();
					if (p.hasAltitude()) lastAlt = p.getAltitude();
				}

				stop = (p == null) || p.getSegmentStart() || hasSufficientTimeDifference(point, p);
				index++;
			}
			while (!stop);

			// See if we've managed to get a non-zero time range
			long milliseconds = lateStamp.getMillisecondsSince(earlyStamp);
			if (milliseconds >= 1000L)
			{
				double altDiff = (lastAlt.getMetricValue() - firstAlt.getMetricValue())
				 * Config.getUnitSet().getVerticalSpeedUnit().getMultFactorFromStd();
				speedValue = altDiff / milliseconds * 1000.0; // units are feet/sec or metres/sec
				pointHasSpeed = true;
			}
		}
		// Check whether we got a value from either method
		if (pointHasSpeed)
		{
			inValue.setValue(speedValue);
		}
	}

	/**
	 * Check whether the time difference between P1 and P2 is sufficiently large
	 * @param inP1 earlier point
	 * @param inP2 later point
	 * @return true if we can stop looking now, found a point early/late enough
	 */
	private static boolean hasSufficientTimeDifference(DataPoint inP1, DataPoint inP2)
	{
		if (inP1 == null || inP2 == null)
			return true; // we have to give up now
		if (!inP1.hasTimestamp() || !inP2.hasTimestamp())
			return false; // keep looking
		final long MIN_TIME_DIFFERENCE_MS = 1000L;
		return inP2.getTimestamp().getMillisecondsSince(inP1.getTimestamp()) >= MIN_TIME_DIFFERENCE_MS;
	}
}
