package tim.prune.data;

/**
 * Class to provide distance constants and functions
 */
public abstract class Distance
{
	// distance formats
	public static final int UNITS_KILOMETRES = 1;
	public static final int UNITS_MILES      = 2;

	// Geographical constants
	private static final double EARTH_RADIUS_KM = 6372.795;
	private static final double EARTH_RADIUS_MILES = 3959.8712255;
	// Conversion constants
	//private static final double CONVERT_KM_TO_MILES = 1.609344;
	//private static final double CONVERT_MILES_TO_KM = 0.621371192;


	/**
	 * Convert the given angle in radians into a distance
	 * @param inAngDist angular distance in radians
	 * @param inUnits desired units, miles or km
	 * @return distance in specified format
	 */
	public static double convertRadians(double inAngDist, int inUnits)
	{
		// Multiply by appropriate factor
		if (inUnits == UNITS_MILES)
			return inAngDist * EARTH_RADIUS_MILES;
		return inAngDist * EARTH_RADIUS_KM;
	}

}
