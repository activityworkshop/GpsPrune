package tim.prune.data;

/**
 * Class to hold information about the mid-points between
 * adjacent track points.  Used by the MapCanvas for creating
 * points by dragging.
 */
public class MidpointData
{
	// track object
	private Track _track = null;
	// Scaled x, y values
	private double[] _xValues = null;
	private double[] _yValues = null;
	// Validity flags
	private boolean[] _valids = null;
	// Flag to set data stale
	private boolean _needRefresh = true;


	/**
	 * Flag the data as needing to be updated
	 * @param inTrack track object from which to get the data
	 */
	public void updateData(Track inTrack)
	{
		_track = inTrack;
		_needRefresh = true;
	}

	/**
	 * Update the arrays of data from the track
	 */
	private synchronized void updateData()
	{
		_needRefresh = false;
		if (_track == null) return;
		// Make arrays the right size
		final int numPoints = _track.getNumPoints();
		if (_xValues == null || _xValues.length != numPoints)
		{
			_xValues = new double[numPoints];
			_yValues = new double[numPoints];
			_valids  = new boolean[numPoints];
		}
		if (numPoints <= 0) return;
		_valids[0] = false;

		// Loop over the points in the track
		for (int i=1; i<numPoints; i++)
		{
			boolean pointValid = false;
			DataPoint point = _track.getPoint(i);
			if (point != null && !point.getSegmentStart() && !point.isWaypoint())
			{
				_xValues[i] = (_track.getX(i) + _track.getX(i-1)) / 2.0;
				_yValues[i] = (_track.getY(i) + _track.getY(i-1)) / 2.0;
				pointValid = true;
			}
			_valids[i] = pointValid;
		}
	}

	/**
	 * Find the nearest point to the specified x and y coordinates
	 * or -1 if no point is within the specified max distance
	 * @param inX x coordinate
	 * @param inY y coordinate
	 * @param inMaxDist maximum distance from selected coordinates
	 * @return index of nearest point or -1 if not found
	 */
	public int getNearestPointIndex(double inX, double inY, double inMaxDist)
	{
		if (_track == null) return -1;
		if (_needRefresh) updateData();
		final int numPoints = _track.getNumPoints();
		int nearestPoint = 0;
		double nearestDist = -1.0;
		double currDist;
		for (int i=1; i < numPoints; i++)
		{
			if (_valids[i])
			{
				currDist = Math.abs(_xValues[i] - inX) + Math.abs(_yValues[i] - inY);
				if (currDist < nearestDist || nearestDist < 0.0)
				{
					nearestPoint = i;
					nearestDist = currDist;
				}
			}
		}
		// Check whether it's within required distance
		if (nearestDist > inMaxDist && inMaxDist > 0.0) {
			return -1;
		}
		return nearestPoint;
	}
}
