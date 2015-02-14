package tim.prune.data;

/**
 * Class to provide distance constants and functions
 */
public abstract class Distance
{
	/** distance units */
	public enum Units
	{
		/** Kilometres */
		KILOMETRES,
		/** Miles */
		MILES,
		/** Metres */
		METRES,
		/** Feet */
		FEET
	}

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
	public static double convertRadiansToDistance(double inAngDist, Units inUnits)
	{
		// Multiply by appropriate factor
		if (inUnits == Units.MILES)
			return inAngDist * EARTH_RADIUS_KM * CONVERT_KM_TO_MILES;
		else if (inUnits == Units.METRES)
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
	public static double convertDistanceToRadians(double inDist, Units inUnits)
	{
		// Divide by appropriate factor
		if (inUnits == Units.MILES)
			return inDist / EARTH_RADIUS_KM / CONVERT_KM_TO_MILES;
		else if (inUnits == Units.METRES)
			return inDist / EARTH_RADIUS_KM / 1000;
		// default kilometres
		return inDist / EARTH_RADIUS_KM;
	}

	/**
	 * Convert the given distance from metres to miles
	 * @param inMetres number of metres
	 * @return number of miles
	 */
	public static double convertMetresToMiles(double inMetres)
	{
		return inMetres / 1000.0 * CONVERT_KM_TO_MILES;
	}
}
