package tim.prune.data;

/**
 * Class to hold a rectangle of latitude/longitude
 * with minimum and maximum values for each
 */
public class LatLonRectangle
{
	private DoubleRange _latRange = null;
	private DoubleRange _lonRange = null;


	/**
	 * Constructor
	 * @param inLatRange latitude range
	 * @param inLonRange longitude range
	 */
	public LatLonRectangle(DoubleRange inLatRange, DoubleRange inLonRange)
	{
		_latRange = inLatRange;
		_lonRange = inLonRange;
		// MAYBE: Expand range by certain percentage
	}

	/**
	 * @return true if the range is empty
	 */
	public boolean isEmpty()
	{
		return _latRange == null || _lonRange == null
			|| !_latRange.hasData() || !_lonRange.hasData();
	}

	/**
	 * Check if a point is within the rectangle
	 * @param inPoint point to check
	 * @return true if point within rectangle
	 */
	public boolean containsPoint(DataPoint inPoint)
	{
		if (inPoint != null && !isEmpty())
		{
			double pointLat = inPoint.getLatitude().getDouble();
			double pointLon = inPoint.getLongitude().getDouble();
			return (pointLat >= _latRange.getMinimum() && pointLat <= _latRange.getMaximum()
				&& pointLon >= _lonRange.getMinimum() && pointLon <= _lonRange.getMaximum());
		}
		return false;
	}
}
