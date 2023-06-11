package tim.prune.function.autoplay;

import tim.prune.data.DataPoint;
import tim.prune.data.Timestamp;

/**
 * Holds the information about a single point required for the sorting
 */
public class PointInfo implements Comparable<PointInfo>
{
	/** Timestamp of the point, if any */
	private final Timestamp _timestamp;
	/** Point index in the track */
	private final int _pointIndex;
	/** Segment flag of point */
	private final boolean _segmentFlag;


	/**
	 * Constructor
	 * @param inPoint point from track
	 * @param inIndex index of point in track
	 */
	public PointInfo(DataPoint inPoint, int inIndex)
	{
		if (inPoint.hasTimestamp()) {
			_timestamp = inPoint.getTimestamp();
		}
		else if (inPoint.getPhoto() != null && inPoint.getPhoto().hasTimestamp()) {
			_timestamp = inPoint.getPhoto().getTimestamp();
		}
		else {
			_timestamp = null;
		}
		_pointIndex = inIndex;
		_segmentFlag = inPoint.getSegmentStart();
	}

	/** @return timestamp */
	public Timestamp getTimestamp() {
		return _timestamp;
	}

	/** @return point index */
	public int getIndex() {
		return _pointIndex;
	}

	/** @return segment flag */
	public boolean getSegmentFlag() {
		return _segmentFlag;
	}

	/**
	 * Sort two objects by timestamp and if times equal then by point index
	 */
	public int compareTo(PointInfo inOther)
	{
		long timeDiff = 0;
		final boolean thisHasTime = (_timestamp != null);
		final boolean otherHasTime = (inOther._timestamp != null);
		if (thisHasTime && otherHasTime)
		{
			timeDiff = _timestamp.getMillisecondsSince(inOther._timestamp);
		}
		else if (thisHasTime)
		{
			timeDiff = -1; // points without time to the end
		}
		else if (otherHasTime)
		{
			timeDiff = 1;
		}
		// If the times are equal (or both missing) then use the point index
		if (timeDiff == 0) {
			return _pointIndex - inOther._pointIndex;
		}
		// Otherwise, compare by time
		return (timeDiff < 0 ? -1 : 1);
	}

	@Override
	public boolean equals(Object inOther)
	{
		if (inOther == null || !(inOther instanceof PointInfo)) {
			return false;
		}

		PointInfo other = (PointInfo) inOther;
		if (_pointIndex != other._pointIndex) return false;
		final boolean thisHasTime = (_timestamp != null);
		final boolean otherHasTime = (other._timestamp != null);
		if (thisHasTime != otherHasTime) {return false;}
		if (!thisHasTime && !otherHasTime) {return true;}
		return _timestamp.isEqual(other._timestamp);
	}
}
