package tim.prune.data;

/**
 * Class to manage the scaling of points
 */
public class PointScaler
{
	// Original data
	private Track _track = null;
	// Range information
	private double _latMedian = 0.0;
	private double _lonMedian = 0.0;
	private int _minAltitude = 0;
	// Scaling information
	private double _longFactor = 0.0;
	private double _altFactor = 0.0;
	// Scaled points
	private double[] _xValues = null;
	private double[] _yValues = null;
	private double[] _altValues = null;
	// max values
	private double _maxX = 0.0;
	private double _maxY = 0.0;
	private double _maxScaledAlt = 0.0;
	// lat/long lines
	private double[] _latLinesDegs = null;
	private double[] _lonLinesDegs = null;
	private double[] _latLinesScaled = null;
	private double[] _lonLinesScaled = null;

	// Constants
	private static final double[] COORD_SEPARATIONS = {
		1.0,                      // 1deg
		30.0/60.0, 20.0/60.0,     // 30min, 20min
		10.0/60.0, 5.0/60.0,      // 10min, 5min
		3.0/60.0, 2.0/60.0, 1.0/60.0   // 3min, 2min, 1min
	};
	private static final int MAX_COORD_SEPARATION_INDEX = COORD_SEPARATIONS.length - 1;

	/**
	 * Constructor
	 * @param inTrack Track object
	 */
	public PointScaler(Track inTrack)
	{
		_track = inTrack;
	}


	/**
	 * Scale the points
	 */
	public void scale()
	{
		// Clear data
		DoubleRange latRange = new DoubleRange();
		DoubleRange lonRange = new DoubleRange();
		DoubleRange altRange = new DoubleRange();
		int numPoints = 0;
		int p = 0;
		DataPoint point = null;
		// Find limits of data
		if (_track != null && (numPoints = _track.getNumPoints()) > 0)
		{
			for (p=0; p<numPoints; p++)
			{
				point = _track.getPoint(p);
				if (point != null)
				{
					latRange.addValue(point.getLatitude().getDouble());
					lonRange.addValue(point.getLongitude().getDouble());
					altRange.addValue(point.getAltitude().getValue(Altitude.Format.METRES));
				}
			}

			// Find median latitude and calculate factor
			_latMedian = (latRange.getMinimum() + latRange.getMaximum()) / 2;
			_lonMedian = (lonRange.getMinimum() + lonRange.getMaximum()) / 2;
			_minAltitude = (int) altRange.getMinimum();
			_longFactor = Math.cos(_latMedian / 180.0 * Math.PI); // quite rough
			// Find altitude scale factor using distance
			DataPoint p1 = new DataPoint(new Latitude(latRange.getMinimum(), Coordinate.FORMAT_DEG),
				new Longitude(_lonMedian, Coordinate.FORMAT_DEG), null);
			DataPoint p2 = new DataPoint(new Latitude(latRange.getMaximum(), Coordinate.FORMAT_DEG),
				new Longitude(_lonMedian, Coordinate.FORMAT_DEG), null);
			double horizDist = Distance.convertRadiansToDistance(
				DataPoint.calculateRadiansBetween(p1, p2), Distance.Units.METRES);
			_altFactor = 1.0 / horizDist;

			// create new arrays for scaled values
			if (_xValues == null || _xValues.length != numPoints)
			{
				_xValues = new double[numPoints];
				_yValues = new double[numPoints];
				_altValues = new double[numPoints];
			}
			// Calculate scaled values
			for (p=0; p<numPoints; p++)
			{
				point = _track.getPoint(p);
				if (point != null)
				{
					_xValues[p] = getScaledLongitude(point.getLongitude().getDouble());
					_yValues[p] = getScaledLatitude(point.getLatitude().getDouble());
					_altValues[p] = getScaledAltitude(point.getAltitude());
					if (_altValues[p] > _maxScaledAlt) {_maxScaledAlt = _altValues[p];}
				}
			}
			// Calculate x and y range
			_maxX = getScaledLongitude(lonRange.getMaximum());
			_maxY = getScaledLatitude(latRange.getMaximum());
		}
	}


	/**
	 * @return maximum horiz value
	 */
	public double getMaximumHoriz() { return _maxX; }
	/**
	 * @return maximum vert value
	 */
	public double getMaximumVert() { return _maxY; }

	/** @return maximum scaled altitude value */
	public double getMaxScaledAlt() { return _maxScaledAlt; }

	/**
	 * Get the horizontal value for the specified point
	 * @param inIndex index of point, starting at 0
	 * @return scaled horizontal value
	 */
	public double getHorizValue(int inIndex)
	{
		return _xValues[inIndex];
	}
	/**
	 * Get the vertical value for the specified point
	 * @param inIndex index of point, starting at 0
	 * @return scaled vertical value
	 */
	public double getVertValue(int inIndex)
	{
		return _yValues[inIndex];
	}
	/**
	 * Get the altitude value for the specified point
	 * @param inIndex index of point, starting at 0
	 * @return scaled altitude value
	 */
	public double getAltValue(int inIndex)
	{
		return _altValues[inIndex];
	}

	/**
	 * Scale the given latitude value
	 * @param inLatitude latitude in degrees
	 * @return scaled latitude
	 */
	private double getScaledLatitude(double inLatitude)
	{
		return inLatitude - _latMedian;
	}
	/**
	 * Scale the given longitude value
	 * @param inLongitude longitude in degrees
	 * @return scaled longitude
	 */
	private double getScaledLongitude(double inLongitude)
	{
		return (inLongitude - _lonMedian) * _longFactor;
	}
	/**
	 * Scale the given altitude value
	 * @param inAltitude Altitude object
	 * @return scaled altitude
	 */
	private double getScaledAltitude(Altitude inAltitude)
	{
		if (inAltitude == null) return -1;
		return (inAltitude.getValue(Altitude.Format.METRES) - _minAltitude) * _altFactor;
	}

	/**
	 * Unscale the given latitude value
	 * @param inScaledLatitude scaled latitude
	 * @return latitude in degrees
	 */
	private double getUnscaledLatitude(double inScaledLatitude)
	{
		return inScaledLatitude + _latMedian;
	}
	/**
	 * Unscale the given longitude value
	 * @param inScaledLongitude scaled longitude
	 * @return longitude in degrees
	 */
	private double getUnscaledLongitude(double inScaledLongitude)
	{
		return inScaledLongitude / _longFactor + _lonMedian;
	}

	/**
	 * Calculate the latitude and longitude lines
	 */
	public void calculateLatLongLines()
	{
		double maxValue = getMaximumHoriz() > getMaximumVert() ?
			getMaximumHoriz():getMaximumVert();
		// calculate boundaries in degrees
		double minLong = getUnscaledLongitude(-maxValue);
		double maxLong = getUnscaledLongitude(maxValue);
		double minLat = getUnscaledLatitude(-maxValue);
		double maxLat = getUnscaledLatitude(maxValue);
		// work out what line separation to use to give at least two lines
		int sepIndex = -1;
		double separation;
		int numLatLines = 0, numLonLines = 0;
		do
		{
			sepIndex++;
			separation = COORD_SEPARATIONS[sepIndex];
			numLatLines = getNumLinesBetween(minLat, maxLat, separation);
			numLonLines = getNumLinesBetween(minLong, maxLong, separation);
		}
		while ((numLonLines <= 1 || numLatLines <= 1) && sepIndex < MAX_COORD_SEPARATION_INDEX);
		// create lines based on this separation
		_latLinesDegs = getLines(minLat, maxLat, separation, numLatLines);
		_lonLinesDegs = getLines(minLong, maxLong, separation, numLonLines);
		// scale lines also
		_latLinesScaled = new double[numLatLines];
		for (int i=0; i<numLatLines; i++) _latLinesScaled[i] = getScaledLatitude(_latLinesDegs[i]);
		_lonLinesScaled = new double[numLonLines];
		for (int i=0; i<numLonLines; i++) _lonLinesScaled[i] = getScaledLongitude(_lonLinesDegs[i]);
	}


	/**
	 * Calculate the number of lines in the given range using the specified separation
	 * @param inMin minimum value
	 * @param inMax maximum value
	 * @param inSeparation line separation
	 * @return number of lines
	 */
	private static int getNumLinesBetween(double inMin, double inMax, double inSeparation)
	{
		// Start looking from round number of degrees below minimum
		double value = (int) inMin;
		if (inMin < 0.0) value = value - 1.0;
		// Loop until bigger than maximum
		int numLines = 0;
		while (value < inMax)
		{
			if (value >= inMin) numLines++;
			value += inSeparation;
		}
		return numLines;
	}


	/**
	 * Get the line values in the given range using the specified separation
	 * @param inMin minimum value
	 * @param inMax maximum value
	 * @param inSeparation line separation
	 * @param inCount number of lines already counted
	 * @return array of line values
	 */
	private static double[] getLines(double inMin, double inMax, double inSeparation, int inCount)
	{
		double[] values = new double[inCount];
		// Start looking from round number of degrees below minimum
		double value = (int) inMin;
		if (inMin < 0.0) value = value - 1.0;
		// Loop until bigger than maximum
		int numLines = 0;
		while (value < inMax)
		{
			if (value >= inMin)
			{
				values[numLines] = value;
				numLines++;
			}
			value += inSeparation;
		}
		return values;
	}

	/**
	 * @return array of latitude lines in degrees
	 */
	public double[] getLatitudeLines()
	{
		return _latLinesDegs;
	}
	/**
	 * @return array of longitude lines in degrees
	 */
	public double[] getLongitudeLines()
	{
		return _lonLinesDegs;
	}
	/**
	 * @return array of latitude lines in scaled units
	 */
	public double[] getScaledLatitudeLines()
	{
		return _latLinesScaled;
	}
	/**
	 * @return array of longitude lines in scaled units
	 */
	public double[] getScaledLongitudeLines()
	{
		return _lonLinesScaled;
	}
}
