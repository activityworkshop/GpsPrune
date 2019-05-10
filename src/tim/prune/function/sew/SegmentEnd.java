package tim.prune.function.sew;

import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;

/**
 * Class to represent one end of a segment, including the
 * coordinates and the other end of the segment
 */
public class SegmentEnd implements Comparable<SegmentEnd>
{
	private SegmentEnd _otherEnd = null;
	private Coordinate _longitude = null;
	private Coordinate _latitude  = null;
	private int        _pointIndex = 0;
	private boolean    _active = true;


	/**
	 * Constructor
	 * @param inPoint data point
	 * @param inIndex point index within track
	 */
	public SegmentEnd(DataPoint inPoint, int inIndex)
	{
		_longitude = inPoint.getLongitude();
		_latitude  = inPoint.getLatitude();
		_pointIndex = inIndex;
		_active    = true;
	}

	/**
	 * @param inOther other end of the segment
	 */
	public void setOtherEnd(SegmentEnd inOther)
	{
		_otherEnd = inOther;
	}

	/**
	 * @return other end
	 */
	public SegmentEnd getOtherEnd()
	{
		return _otherEnd;
	}

	/**
	 * @return true if this is the start of the segment
	 */
	public boolean isStart()
	{
		return _otherEnd == null || _otherEnd._pointIndex > _pointIndex;
	}

	/** @return point index */
	public int getPointIndex() {
		return _pointIndex;
	}

	/** @return point index of other end */
	public int getOtherPointIndex()
	{
		return _otherEnd == null ? _pointIndex : _otherEnd._pointIndex;
	}

	/** @return get the earlier of the two point indices */
	public int getEarlierIndex() {
		return isStart() ? _pointIndex : _otherEnd._pointIndex;
	}

	/** @return get the later of the two point indices */
	public int getLaterIndex() {
		return isStart() ? _otherEnd._pointIndex : _pointIndex;
	}

	/**
	 * @return earlier end of this segment
	 */
	public SegmentEnd getEarlierEnd() {
		return isStart() ? this : _otherEnd;
	}

	/**
	 * @return later end of this segment
	 */
	public SegmentEnd getLaterEnd() {
		return isStart() ? _otherEnd : this;
	}

	/**
	 * Reverse this segment, by swapping the point indices of the start and end
	 * isStart() will thereby also be reversed for both ends
	 */
	public void reverseSegment()
	{
		if (_otherEnd != null)
		{
			int pointIndex = _pointIndex;
			_pointIndex = _otherEnd._pointIndex;
			_otherEnd._pointIndex = pointIndex;
		}
	}

	/**
	 * @return true if this node is still active
	 */
	public boolean isActive() {
		return _active;
	}

	/**
	 * Deactive this node, don't use it any more (it's already been merged)
	 */
	public void deactivate() {
		_active = false;
	}

	/**
	 * @param inOther other segment end
	 * @return true if the coordinates are identical
	 */
	public boolean atSamePointAs(SegmentEnd inOther)
	{
		return inOther != null && _latitude.equals(inOther._latitude) && _longitude.equals(inOther._longitude);
	}

	/**
	 * Compare two objects for sorting
	 */
	public int compareTo(SegmentEnd o)
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

	/**
	 * Adjust the point index as a result of a cut/move operation on the track
	 * @param inSegmentStart index of start of segment to be moved
	 * @param inSegmentEnd index of end of segment to be moved
	 * @param inMoveTo index of point before which the segment should be moved
	 */
	public void adjustPointIndex(int inSegmentStart, int inSegmentEnd, int inMoveTo)
	{
		final int segmentSize = inSegmentEnd - inSegmentStart + 1; // number of points moved
		final boolean forwardsMove = inMoveTo > inSegmentEnd;
		// Min and max indices of affected points (apart from segment to be moved)
		final int minIndex = forwardsMove ? inSegmentEnd + 1 : inMoveTo;
		final int maxIndex = forwardsMove ? inMoveTo - 1 : inSegmentStart - 1;
		if (_pointIndex >= minIndex && _pointIndex <= maxIndex)
		{
			// final int origIndex = _pointIndex;
			if (forwardsMove) {
				_pointIndex -= segmentSize; // segment moved forwards, point indices reduced
			}
			else {
				_pointIndex += segmentSize; // segment moved backwards, point indices shifted forwards
			}
			// System.out.println("    Need to adjust index: " + origIndex + " -> " + _pointIndex);
		}
		else if (_pointIndex == inSegmentStart)
		{
			// final int origIndex = _pointIndex;
			if (forwardsMove) {
				_pointIndex = inMoveTo - segmentSize;
			}
			else
			{
				// Point index moves to moveTo
				_pointIndex = inMoveTo;
			}
			// System.out.println("    Need to adjust movedseg: " + origIndex + " -> " + _pointIndex);
		}
		else if (_pointIndex == inSegmentEnd)
		{
			// final int origEndIndex = _otherEnd._pointIndex;
			if (forwardsMove) {
				_pointIndex = inMoveTo - 1;
			}
			else
			{
				// Point index moves to moveTo
				_pointIndex = inMoveTo + inSegmentEnd - inSegmentStart;
			}
			// System.out.println("    Need to adjust movedseg: " + origEndIndex + " -> " + _pointIndex);
		}
	}
}
