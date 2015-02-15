package tim.prune.data;

/**
 * Class to manage the scaling of points, used by the ThreeDModel
 */
public class PointScaler
{
	// Original data
	private Track _track = null;
	// Scaled values
	private double[] _xValues = null;
	private double[] _yValues = null;
	private double[] _altValues = null;
	// Altitude range
	private double _altitudeRange = 0.0;


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
		// Work out extents
		TrackExtents extents = new TrackExtents(_track);
		extents.applySquareBorder();
		final double horizDistance = Math.max(extents.getHorizontalDistanceMetres(), 1.0);
		final int numPoints = _track.getNumPoints();

		// Find altitude range
		_altitudeRange = extents.getAltitudeRange().getRange() / horizDistance;
		final double minAltitude = extents.getAltitudeRange().getMinimum();

		// create new arrays for scaled values
		if (_xValues == null || _xValues.length != numPoints)
		{
			_xValues = new double[numPoints];
			_yValues = new double[numPoints];
			_altValues = new double[numPoints];
		}

		final double midXvalue = extents.getXRange().getMidValue();
		final double midYvalue = extents.getYRange().getMidValue();
		final double xyRange   = extents.getXRange().getRange();

		// Calculate scaled values
		for (int p=0; p<numPoints; p++)
		{
			DataPoint point = _track.getPoint(p);
			if (point != null)
			{
				_xValues[p] = (_track.getX(p) - midXvalue) / xyRange;
				_yValues[p] = (midYvalue - _track.getY(p)) / xyRange; // y values have to be inverted
				_altValues[p] = (point.getAltitude().getMetricValue() - minAltitude) / horizDistance;
			}
		}
	}



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
	 * @return altitude range, in metres
	 */
	public double getAltitudeRange()
	{
		return _altitudeRange;
	}
}
