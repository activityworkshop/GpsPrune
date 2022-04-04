package tim.prune.function.olc;

/**
 * Class to represent the result of an OLC decoding
 */
public class OlcArea
{
	public final double minLat;
	public final double maxLat;
	public final double minLon;
	public final double maxLon;
	public final String code;

	/** Constructor */
	public OlcArea(double inMinLat, double inMinLon, double inMaxLat, double inMaxLon, String inCode)
	{
		minLat = inMinLat;
		minLon = inMinLon;
		maxLat = inMaxLat;
		maxLon = inMaxLon;
		code = inCode;
	}

	public double middleLat() {
		return (minLat + maxLat) / 2.0;
	}

	public double middleLon() {
		return (minLon + maxLon) / 2.0;
	}
}
