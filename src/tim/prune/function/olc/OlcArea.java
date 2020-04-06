package tim.prune.function.olc;

/**
 * Class to represent the result of an OLC decoding
 */
public class OlcArea
{
	public double minLat = 0.0;
	public double maxLat = 0.0;
	public double minLon = 0.0;
	public double maxLon = 0.0;

	/** Constructor */
	public OlcArea(double inMinLat, double inMinLon, double inMaxLat, double inMaxLon)
	{
		minLat = inMinLat;
		minLon = inMinLon;
		maxLat = inMaxLat;
		maxLon = inMaxLon;
	}
}
