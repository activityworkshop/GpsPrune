package tim.prune.data;

public abstract class Bearing
{
	/**
	 * Calculate the initial bearing from one point to another
	 * @param inFromPoint starting point
	 * @param inToPoint finishing point
	 * @return initial angle in degrees clockwise from North
	 */
	public static double calculateDegrees(DataPoint inFromPoint, DataPoint inToPoint)
	{
		double deltaLong = Math.toRadians(inToPoint.getLongitude().getDouble()
				- inFromPoint.getLongitude().getDouble());
		double sinDeltaLong = Math.sin(deltaLong);
		double cosDeltaLong = Math.cos(deltaLong);
		double lat1 = Math.toRadians(inFromPoint.getLatitude().getDouble());
		double lat2 = Math.toRadians(inToPoint.getLatitude().getDouble());
		double angleRadians = Math.atan2(sinDeltaLong * Math.cos(lat2),
			Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * cosDeltaLong);
		return Math.toDegrees(angleRadians);
	}

	public static double calculateDegreeChange(DataPoint inFromPoint, DataPoint inMidPoint, DataPoint inToPoint)
	{
		if (inFromPoint == null || inMidPoint == null || inToPoint == null) {
			return 180.0; // cannot calculate
		}
		final double firstAngle = calculateDegrees(inFromPoint, inMidPoint);
		final double secondAngle = calculateDegrees(inMidPoint, inToPoint);
		return angleDifferenceDegrees(firstAngle, secondAngle);
	}

	public static double angleDifferenceDegrees(double inFirstDeg, double inSecondDeg)
	{
		double angleDiff = Math.abs(inFirstDeg - inSecondDeg) % 360.0;
		return angleDiff > 180.0 ? (360.0 - angleDiff) : angleDiff;
	}
}
