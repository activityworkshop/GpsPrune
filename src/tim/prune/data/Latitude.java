package tim.prune.data;

/**
 * Class to make it easier to create Coordinate objects for Latitudes
 */
public abstract class Latitude
{
	public static Coordinate make(double inValue)
	{
		if (Math.abs(inValue) >= 90.0) {
			return null;
		}
		return new Coordinate(inValue, inValue >= 0.0 ? Coordinate.Cardinal.NORTH : Coordinate.Cardinal.SOUTH);
	}

	public static Coordinate make(String inString)
	{
		Coordinate coordinate = Coordinate.parse(inString, Coordinate.Cardinal.NORTH, Coordinate.Cardinal.SOUTH);
		if (coordinate == null || Math.abs(coordinate.getDouble()) >= 90.0) {
			return null;
		}
		return coordinate;
	}

	public static Coordinate interpolate(Coordinate inStart, Coordinate inEnd, double inFraction) {
		return Coordinate.interpolate(inStart, inEnd, inFraction, Coordinate.Cardinal.NORTH, Coordinate.Cardinal.SOUTH);
	}

	public static Coordinate interpolate(Coordinate inStart, Coordinate inEnd, int inIndex, int inNumPoints) {
		return interpolate(inStart, inEnd, 1.0 * (inIndex+1) / (inNumPoints + 1));
	}

	public static boolean hasCardinal(String inSource)
	{
		Coordinate.Cardinal cardinal = Coordinate.getCardinal(inSource,
			Coordinate.Cardinal.NORTH, Coordinate.Cardinal.SOUTH);
		return cardinal != Coordinate.Cardinal.NO_CARDINAL;
	}
}
