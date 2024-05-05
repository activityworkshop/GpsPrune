package tim.prune.function.weather;

class LatLongPair
{
	private final double _latitude;
	private final double _longitude;

	public LatLongPair(double inLatitude, double inLongitude)
	{
		_latitude = inLatitude;
		_longitude = inLongitude;
	}

	double getLatitude() {
		return _latitude;
	}

	double getLongitude() {
		return _longitude;
	}
}
