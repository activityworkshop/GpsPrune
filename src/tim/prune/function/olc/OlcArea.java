package tim.prune.function.olc;

/**
 * Record to represent the result of an OLC decoding
 */
public record OlcArea(double minLat, double minLon, double maxLat, double maxLon, String code)
{
	public double middleLat() {
		return (minLat + maxLat) / 2.0;
	}

	public double middleLon() {
		return (minLon + maxLon) / 2.0;
	}
}
