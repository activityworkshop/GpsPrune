package tim.prune.data;

import tim.prune.config.Config;

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
	 * @return distance in currently configured distance units
	 */
	public static double convertRadiansToDistance(double inAngDist)
	{
		return convertRadiansToDistance(inAngDist, Config.getUnitSet().getDistanceUnit());
	}

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
	 * @return angular distance in radians
	 */
	public static double convertDistanceToRadians(double inDist)
	{
		return convertDistanceToRadians(inDist, Config.getUnitSet().getDistanceUnit());
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
}
