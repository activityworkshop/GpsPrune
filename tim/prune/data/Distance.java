package tim.prune.data;

/**
 * Class to provide distance constants and functions
 */
public abstract class Distance
{
	// distance formats
	public static final int UNITS_KILOMETRES = 1;
	public static final int UNITS_MILES      = 2;
	public static final int UNITS_METRES     = 3;

	// Geographical constants
	private static final double EARTH_RADIUS_KM = 6372.795;
	// Conversion constants
	private static final double CONVERT_KM_TO_MILES = 0.621371192;


	/**
	 * Convert the given angle in radians into a distance
	 * @param inAngDist angular distance in radians
	 * @param inUnits desired units, eg miles or km
	 * @return distance in specified format
	 */
	public static double convertRadiansToDistance(double inAngDist, int inUnits)
	{
		// Multiply by appropriate factor
		if (inUnits == UNITS_MILES)
			return inAngDist * EARTH_RADIUS_KM * CONVERT_KM_TO_MILES;
		else if (inUnits == UNITS_METRES)
			return inAngDist * EARTH_RADIUS_KM * 1000;
		// default kilometres
		return inAngDist * EARTH_RADIUS_KM;
	}

	/**
	 * Convert the given distance into an angle in radians
	 * @param inDist distance to convert
	 * @param inUnits units, eg miles or km
	 * @return angular distance in radians
	 */
	public static double convertDistanceToRadians(double inDist, int inUnits)
	{
		// Divide by appropriate factor
		if (inUnits == UNITS_MILES)
			return inDist / EARTH_RADIUS_KM / CONVERT_KM_TO_MILES;
		else if (inUnits == UNITS_METRES)
			return inDist / EARTH_RADIUS_KM / 1000;
		// default kilometres
		return inDist / EARTH_RADIUS_KM;
	}
}
