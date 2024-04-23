package tim.prune.data;


/**
 * Class to provide distance constants and functions
 */
public abstract class Distance
{
	// Geographical constants
	/** Earth radius in metres */
	private static final double EARTH_RADIUS_M = 6372795.0;


	/**
	 * Convert the given angle in radians into a distance
	 * @param inAngDist angular distance in radians
	 * @param inUnit distance units
	 * @return distance in specified distance units
	 */
	public static double convertRadiansToDistance(double inAngDist, Unit inUnit)
	{
		// Multiply by appropriate factor
		return inAngDist * EARTH_RADIUS_M * inUnit.getMultFactorFromStd();
	}

	/**
	 * Convert the given distance into an angle in radians
	 * @param inDist distance to convert in the current distance units
	 * @param inUnit distance unit
	 * @return angular distance in radians
	 */
	public static double convertDistanceToRadians(double inDist, Unit inUnit)
	{
		// Divide by appropriate factor
		return inDist / EARTH_RADIUS_M / inUnit.getMultFactorFromStd();
	}

	/**
	 * Convert distance value from one unit to another
	 * @param inDist distance value
	 * @param inValueUnit unit in which the given value is represented
	 * @param inTargetUnit desired unit
	 * @return value in target units
	 */
	public static double convertBetweenUnits(double inDist, Unit inValueUnit, Unit inTargetUnit)
	{
		return inDist / inValueUnit.getMultFactorFromStd() * inTargetUnit.getMultFactorFromStd();
	}

	/**
	 * Calculate the number of radians between two points (for distance calculation)
	 * @param inLatitude1 latitude of first point (in degrees)
	 * @param inLongitude1 longitude of first point
	 * @param inLatitude2 latitude of second point
	 * @param inLongitude2 longitude of second point
	 * @return angular distance between points in radians
	 */
	public static double calculateRadiansBetween(double inLatitude1, double inLongitude1,
		double inLatitude2, double inLongitude2)
	{
		final double TO_RADIANS = Math.PI / 180.0;
		final double lat1rad = inLatitude1 * TO_RADIANS;
		final double lat2rad = inLatitude2 * TO_RADIANS;
		final double lon1rad = inLongitude1 * TO_RADIANS;
		final double lon2rad = inLongitude2 * TO_RADIANS;
		// Formula given by Wikipedia:Great-circle_distance as follows:
		// angle = 2 arcsin( sqrt( (sin ((lat2-lat1)/2))^^2 + cos(lat1)cos(lat2)(sin((lon2-lon1)/2))^^2))
		double firstSine = Math.sin((lat2rad - lat1rad) / 2.0);
		double secondSine = Math.sin((lon2rad - lon1rad) / 2.0);
		double term2 = Math.cos(lat1rad) * Math.cos(lat2rad) * secondSine * secondSine;
		double answer = 2 * Math.asin(Math.sqrt(firstSine * firstSine + term2));
		// phew
		return answer;
	}
}
