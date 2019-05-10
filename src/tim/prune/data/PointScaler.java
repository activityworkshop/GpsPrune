package tim.prune.data;

/**
 * Class to manage the scaling of points, used by the ThreeDModel
 */
public class PointScaler
{
	/** Original data */
	private Track _track = null;
	/** Secondary data for terrain grid */
	private Track _terrainTrack = null;
	// Scaled values for data track
	private double[] _xValues = null;
	private double[] _yValues = null;
	private double[] _altValues = null;
	// Scaled values for terrain track, if any
	private double[] _terrainxValues = null;
	private double[] _terrainyValues = null;
	private double[] _terrainAltValues = null;
	// Altitude range
	private double _altitudeRange = 0.0;
	private double _minAltitudeMetres = 0.0;
	// Horizontal distance
	private double _horizDistanceMetres = 0.0;


	/**
	 * Constructor
	 * @param inTrack Track object
	 */
	public PointScaler(Track inTrack)
	{
		_track = inTrack;
	}

	/**
	 * @param inTrack terrain track to add
	 */
	public void addTerrain(Track inTrack)
	{
		_terrainTrack = inTrack;
	}

	/**
	 * Scale the points
	 */
	public void scale()
	{
		// Work out extents
		TrackExtents extents = new TrackExtents(_track);
		extents.applySquareBorder();
		_horizDistanceMetres = Math.max(extents.getHorizontalDistanceMetres(), 1.0);
		final int numPoints = _track.getNumPoints();

		// Find altitude range (including terrain)
		DoubleRange altRangeMetres = extents.getAltitudeRange();
		if (_terrainTrack != null) {
			altRangeMetres.combine(new TrackExtents(_terrainTrack).getAltitudeRange());
		}
		_altitudeRange = altRangeMetres.getRange() / _horizDistanceMetres;
		_minAltitudeMetres = altRangeMetres.getMinimum();

		// create new arrays for scaled values
		if (_xValues == null || _xValues.length != numPoints)
		{
			_xValues = new double[numPoints];
			_yValues = new double[numPoints];
			_altValues = new double[numPoints];
			if (_terrainTrack != null)
			{
				_terrainxValues = new double[_terrainTrack.getNumPoints()];
				_terrainyValues = new double[_terrainTrack.getNumPoints()];
				_terrainAltValues = new double[_terrainTrack.getNumPoints()];
			}
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
				_altValues[p] = (point.getAltitude().getMetricValue() - _minAltitudeMetres) / _horizDistanceMetres;
			}
		}
		if (_terrainTrack != null)
		{
			for (int p=0; p<_terrainTrack.getNumPoints(); p++)
			{
				_terrainxValues[p] = (_terrainTrack.getX(p) - midXvalue) / xyRange;
				_terrainyValues[p] = (midYvalue - _terrainTrack.getY(p)) / xyRange; // y values have to be inverted
				_terrainAltValues[p] = (_terrainTrack.getPoint(p).getAltitude().getMetricValue() - _minAltitudeMetres) / _horizDistanceMetres;
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
	 * @return altitude range as fraction of horizontal range
	 */
	public double getAltitudeRange()
	{
		return _altitudeRange;
	}

	/**
	 * Get the horizontal value for the specified point
	 * @param inIndex index of point, starting at 0
	 * @return scaled horizontal value
	 */
	public double getTerrainHorizValue(int inIndex)
	{
		return _terrainxValues[inIndex];
	}

	/**
	 * Get the vertical value for the specified point
	 * @param inIndex index of point, starting at 0
	 * @return scaled vertical value
	 */
	public double getTerrainVertValue(int inIndex)
	{
		return _terrainyValues[inIndex];
	}

	/**
	 * @param inIndex index of point in terrain track
	 * @return scaled altitude value for the specified terrain point
	 */
	public double getTerrainAltValue(int inIndex)
	{
		if (_terrainAltValues != null) {
			return _terrainAltValues[inIndex];
		}
		return 0.0;
	}
}
