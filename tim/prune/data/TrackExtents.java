package tim.prune.data;

/**
 * Class to hold the extents of a track, in 2d and in 3d,
 * and to calculate a square area with a default border
 */
public class TrackExtents
{
	/** Track object */
	private Track _track = null;
	/** X and Y ranges */
	private DoubleRange _xRange   = null, _yRange   = null;

	/** Border multiplier */
	private static final double BORDER_MULTIPLIER = 1.1; // 10% border

	/**
	 * Constructor
	 * @param inTrack track object to take extents from
	 */
	public TrackExtents(Track inTrack)
	{
		_track  = inTrack;
		_xRange = inTrack.getXRange().copy();
		_yRange = inTrack.getYRange().copy();
	}


	/**
	 * Make the x and y ranges square with a default border around
	 */
	public void applySquareBorder()
	{
		// Find the middle of the x and y
		final double midXvalue = _xRange.getMidValue();
		final double midYvalue = _yRange.getMidValue();
		// Find x and y range, take maximum
		double xyRange = Math.max(_xRange.getRange(), _yRange.getRange()) * BORDER_MULTIPLIER;
		if (getHorizontalDistanceMetres() < 10.0)
		{
			// all the points are near enough on the same spot, expand scale to avoid dividing by zero
			xyRange = 0.1;
		}

		// Apply these new min and max to the ranges
		_xRange.addValue(midXvalue - xyRange / 2.0);
		_xRange.addValue(midXvalue + xyRange / 2.0);
		_yRange.addValue(midYvalue - xyRange / 2.0);
		_yRange.addValue(midYvalue + xyRange / 2.0);
	}

	/** @return x range */
	public DoubleRange getXRange() {
		return _xRange;
	}

	/** @return y range */
	public DoubleRange getYRange() {
		return _yRange;
	}

	/** @return altitude range */
	public DoubleRange getAltitudeRange()
	{
		final int numPoints = _track.getNumPoints();
		DoubleRange altRange = new DoubleRange();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint p = _track.getPoint(i);
			if (p != null && p.hasAltitude()) {
				altRange.addValue(p.getAltitude().getMetricValue());
			}
		}
		return altRange;
	}

	/**
	 * @return the greater of the N/S and E/W extent of the track, in metres (including border)
	 */
	public double getHorizontalDistanceMetres()
	{
		DoubleRange lonRange = _track.getLonRange();
		DoubleRange latRange = _track.getLatRange();

		// Find horizontal and vertical extents of enclosing rectangle
		DataPoint southPoint = new DataPoint(new Latitude(latRange.getMinimum(), Coordinate.FORMAT_DEG),
			new Longitude(lonRange.getMidValue(), Coordinate.FORMAT_DEG), null);
		DataPoint northPoint = new DataPoint(new Latitude(latRange.getMaximum(), Coordinate.FORMAT_DEG),
			new Longitude(lonRange.getMidValue(), Coordinate.FORMAT_DEG), null);
		double nsDist = Distance.convertRadiansToDistance(
			DataPoint.calculateRadiansBetween(northPoint, southPoint), UnitSetLibrary.UNITS_METRES); // both in m
		// Same again for bottom and top, take maximum
		DataPoint westPoint = new DataPoint(new Latitude(latRange.getMidValue(), Coordinate.FORMAT_DEG),
			new Longitude(lonRange.getMinimum(), Coordinate.FORMAT_DEG), null);
		DataPoint eastPoint = new DataPoint(new Latitude(latRange.getMidValue(), Coordinate.FORMAT_DEG),
			new Longitude(lonRange.getMinimum(), Coordinate.FORMAT_DEG), null);
		double ewDist = Distance.convertRadiansToDistance(
			DataPoint.calculateRadiansBetween(westPoint, eastPoint), UnitSetLibrary.UNITS_METRES); // both in m
		final double horizDistance = Math.max(nsDist, ewDist) * BORDER_MULTIPLIER;

		return horizDistance;
	}
}
