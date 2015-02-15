package tim.prune.gui.map;

/**
 * Class to manage coordinate conversions for maps
 */
public abstract class MapUtils
{
	/**
	 * Transform a longitude into an x coordinate
	 * @param inLon longitude in degrees
	 * @return scaled X value from 0 to 1
	 */
	public static double getXFromLongitude(double inLon)
	{
		return (inLon + 180.0) / 360.0;
	}

	/**
	 * Transform a latitude into a y coordinate
	 * @param inLat latitude in degrees
	 * @return scaled Y value from 0 to 1
	 */
	public static double getYFromLatitude(double inLat)
	{
		return (1 - Math.log(Math.tan(inLat * Math.PI / 180) + 1 / Math.cos(inLat * Math.PI / 180)) / Math.PI) / 2;
	}

	/**
	 * Transform an x coordinate into a longitude
	 * @param inX scaled X value from 0(-180deg) to 1(+180deg)
	 * @return longitude in degrees
	 */
	public static double getLongitudeFromX(double inX)
	{
		// Ensure x is really between 0 and 1 (to wrap longitudes)
		double x = ((inX % 1.0) + 1.0) % 1.0;
		// Note: First %1.0 restricts range to (-1,1), then +1.0 shifts to (0,2)
		// Finally, %1.0 to give (0,1)
		return x * 360.0 - 180.0;
	}

	/**
	 * Transform a y coordinate into a latitude
	 * @param inY scaled Y value from 0 to 1
	 * @return latitude in degrees
	 */
	public static double getLatitudeFromY(double inY)
	{
		double n = Math.PI * (1 - 2 * inY);
		return 180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
	}
}
