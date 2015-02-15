package tim.prune.function.sew;

import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;

/**
 * Class to represent a possible split point, including
 * the distances to the previous and next points
 */
public class SplitPoint implements Comparable<SplitPoint>
{
	private SplitPoint _nextPoint = null;
	private Coordinate _longitude = null;
	private Coordinate _latitude  = null;
	private int        _pointIndex = 0;
	private double     _distToPrevPoint = 0.0;
	private double     _distToNextPoint = -1.0;


	/**
	 * Constructor
	 * @param inPoint data point
	 * @param inIndex point index within track
	 */
	public SplitPoint(DataPoint inPoint, int inIndex)
	{
		_longitude = inPoint.getLongitude();
		_latitude  = inPoint.getLatitude();
		_pointIndex = inIndex;
	}

	/**
	 * @param inDist distance to previous track point
	 */
	public void setDistanceToPrevPoint(double inDist) {
		_distToPrevPoint = inDist;
	}
	/** @return distance to previous track point */
	public double getDistanceToPrevPoint() {
		return _distToPrevPoint;
	}

	/**
	 * @param inDist distance to next track point, or -1.0
	 */
	public void setDistanceToNextPoint(double inDist) {
		_distToNextPoint = inDist;
	}
	/** @return distance to next track point */
	public double getDistanceToNextPoint() {
		return _distToNextPoint;
	}
	/** @return true if this is closer to the next point than to the previous one */
	public boolean closerToNext() {
		return _distToNextPoint > 0.0 && _distToNextPoint < _distToPrevPoint;
	}

	/** @return point index */
	public int getPointIndex() {
		return _pointIndex;
	}

	/**
	 * @param inOther the next point
	 */
	public void setNextPoint(SplitPoint inOther) {
		_nextPoint = inOther;
	}

	/** @return the next point, or null */
	public SplitPoint getNextPoint() {
		return _nextPoint;
	}

	/**
	 * @param inOther other segment end
	 * @return true if the coordinates are identical
	 */
	public boolean atSamePointAs(SplitPoint inOther)
	{
		return inOther != null && _latitude.equals(inOther._latitude) && _longitude.equals(inOther._longitude);
	}

	/**
	 * Compare two objects for sorting
	 */
	public int compareTo(SplitPoint o)
	{
		if (o == null) return -1;
		// First, sort by latitude
		if (!_latitude.equals(o._latitude)) {
			return (_latitude.getDouble() < o._latitude.getDouble() ? -1 : 1);
		}
		// Latitudes same, so sort by longitude
		if (!_longitude.equals(o._longitude)) {
			return (_longitude.getDouble() < o._longitude.getDouble() ? -1 : 1);
		}
		// Points are identical so just sort by index
		return _pointIndex - o._pointIndex;
	}
}
