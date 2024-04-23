package tim.prune.data;

/**
 * Class to make it easier to create Coordinate objects for Longitudes
 */
public abstract class Longitude
{
	public static Coordinate make(double inValue)
	{
		if (Math.abs(inValue) > 180.0) {
			return make(wrapTo180(inValue));
		}
		return new Coordinate(inValue, inValue >= 0.0 ? Coordinate.Cardinal.EAST : Coordinate.Cardinal.WEST);
	}

	/** @return given value wrapped to between -180 and +180 */
	static double wrapTo180(double inValue)
	{
		double value = inValue + 180.0;
		while (value < 0.0) {
			value += 360;
		}
		return (value % 360.0) - 180.0;
	}

	public static Coordinate make(String inString)
	{
		Coordinate coordinate = Coordinate.parse(inString, Coordinate.Cardinal.EAST, Coordinate.Cardinal.WEST);
		if (coordinate != null && Math.abs(coordinate.getDouble()) > 180.0)
		{
			// Wrap coordinates to within the correct range, modifying cardinal and all values
			return coordinate.wrapTo180Degrees();
		}
		return coordinate;
	}

	public static Coordinate interpolate(Coordinate inStart, Coordinate inEnd, double inFraction) {
		return Coordinate.interpolate(inStart, inEnd, inFraction, Coordinate.Cardinal.EAST, Coordinate.Cardinal.WEST);
	}

	public static Coordinate interpolate(Coordinate inStart, Coordinate inEnd, int inIndex, int inNumPoints) {
		return interpolate(inStart, inEnd, 1.0 * (inIndex+1) / (inNumPoints + 1));
	}

	public static boolean hasCardinal(String inSource)
	{
		Coordinate.Cardinal cardinal = Coordinate.getCardinal(inSource,
			Coordinate.Cardinal.EAST, Coordinate.Cardinal.WEST);
		return cardinal != Coordinate.Cardinal.NO_CARDINAL;
	}
}
