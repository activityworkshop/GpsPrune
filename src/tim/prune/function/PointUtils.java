package tim.prune.function;

import tim.prune.data.*;

/** General utility methods for creating new DataPoint objects */
public abstract class PointUtils
{
	/**
	 * Project the given data point and create a new one in the calculated position
	 */
	public static DataPoint projectPoint(DataPoint inPoint, double inBearingRadians, double inDistanceRadians)
	{
		final double origLatRads = Math.toRadians(inPoint.getLatitude().getDouble());
		final double origLonRads = Math.toRadians(inPoint.getLongitude().getDouble());

		double lat2 = Math.asin(Math.sin(origLatRads) * Math.cos(inDistanceRadians)
				+ Math.cos(origLatRads) * Math.sin(inDistanceRadians) * Math.cos(inBearingRadians));
		double lon2 = origLonRads + Math.atan2(Math.sin(inBearingRadians) * Math.sin(inDistanceRadians) * Math.cos(origLatRads),
				Math.cos(inDistanceRadians) - Math.sin(origLatRads) * Math.sin(lat2));

		return new DataPoint(Math.toDegrees(lat2), Math.toDegrees(lon2));
	}

	/**
	 * Interpolate between the two given points
	 * @param inStartPoint start point
	 * @param inEndPoint end point
	 * @param inFrac fractional distance from first point (0.0 to 1.0)
	 * @return new DataPoint object between two given ones
	 */
	public static DataPoint interpolate(DataPoint inStartPoint, DataPoint inEndPoint, double inFrac)
	{
		if (inStartPoint == null || inEndPoint == null) {
			return null;
		}
		DataPoint result = new DataPoint(
				Latitude.interpolate(inStartPoint.getLatitude(), inEndPoint.getLatitude(), inFrac),
				Longitude.interpolate(inStartPoint.getLongitude(), inEndPoint.getLongitude(), inFrac),
				Altitude.interpolate(inStartPoint.getAltitude(), inEndPoint.getAltitude(), inFrac)
		);
		if (inStartPoint.hasTimestamp() && inEndPoint.hasTimestamp())
		{
			String value = TimestampUtc.interpolate(inStartPoint.getTimestamp(), inEndPoint.getTimestamp(), inFrac);
			result.setFieldValue(Field.TIMESTAMP, value, false);
		}
		return result;
	}

	/**
	 * Interpolate a point between two points given an index and number of points
	 * @param inEndPoint end point of interpolation
	 * @param inIndex the index of this interpolation (0 to inNumPoints-1)
	 * @param inNumPoints number of points to generate
	 * @return the interpolated DataPoint
	 */
	public static DataPoint interpolate(DataPoint inStartPoint, DataPoint inEndPoint, int inIndex, int inNumPoints)
	{
		final double frac = 1.0 * (inIndex+1) / (inNumPoints + 1);
		return interpolate(inStartPoint, inEndPoint, frac);
	}
}
