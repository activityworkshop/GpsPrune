package tim.prune.function.comparesegments;

import tim.prune.data.DataPoint;
import tim.prune.data.Timestamp;

/**
 * The result of intersecting a point from one segment
 * with the lines from another segment
 */
public class IntersectionResult implements Comparable<IntersectionResult>
{
	private static class ResultPoint
	{
		private final DataPoint _point;
		private final Timestamp _timestamp;
		private final double _distance;
		private final double _speed;
		private ResultPoint(DataPoint inPoint, Timestamp inTimestamp, double inDistance, double inSpeed) {
			_point = inPoint;
			_timestamp = inTimestamp;
			_distance = inDistance;
			_speed = inSpeed;
		}
	}

	private final ResultPoint _firstPoint;
	private final ResultPoint _secondPoint;


	public IntersectionResult(DataPoint inFirstPoint, DataPoint inSecondPoint,
		Timestamp inFirstTimestamp,	Timestamp inSecondTimestamp,
		double inFirstDistanceRadians, double inSecondDistanceRadians,
		double inFirstSpeed, double inSecondSpeed)
	{
		_firstPoint = new ResultPoint(inFirstPoint, inFirstTimestamp, inFirstDistanceRadians, inFirstSpeed);
		_secondPoint = new ResultPoint(inSecondPoint, inSecondTimestamp, inSecondDistanceRadians, inSecondSpeed);
	}

	private IntersectionResult(ResultPoint inFirstPoint, ResultPoint inSecondPoint)
	{
		_firstPoint = inFirstPoint;
		_secondPoint = inSecondPoint;
	}

	/** Reverse the meaning of the two segments, creating an opposite result */
	IntersectionResult reverse() {
		return new IntersectionResult(_secondPoint, _firstPoint);
	}

	public DataPoint getFirstPoint() {
		return _firstPoint._point;
	}

	public DataPoint getSecondPoint() {
		return _secondPoint._point;
	}

	public long getFirstDurationSeconds(IntersectionResult inOther) {
		return _firstPoint._timestamp.getSecondsSince(inOther._firstPoint._timestamp);
	}

	public long getSecondDurationSeconds(IntersectionResult inOther) {
		return _secondPoint._timestamp.getSecondsSince(inOther._secondPoint._timestamp);
	}

	public double getFirstDistanceRadians(IntersectionResult inOther) {
		return _firstPoint._distance - inOther._firstPoint._distance;
	}

	public double getSecondDistanceRadians(IntersectionResult inOther) {
		return _secondPoint._distance - inOther._secondPoint._distance;
	}

	public double getDeltaSpeedRadiansPerSec() {
		return _secondPoint._speed - _firstPoint._speed;
	}

	public int compareTo(IntersectionResult inOther)
	{
		if (inOther == null || _firstPoint._timestamp.isBefore(inOther._firstPoint._timestamp)) {
			return -1;
		}
		if (inOther._firstPoint._timestamp.isBefore(_firstPoint._timestamp)) {
			return 1;
		}
		int distCompare = Double.compare(_firstPoint._distance, inOther._firstPoint._distance);
		if (distCompare != 0) {
			return distCompare;
		}
		if (_secondPoint._timestamp.isBefore(inOther._secondPoint._timestamp)) {
			return -1;
		}
		if (inOther._secondPoint._timestamp.isBefore(_secondPoint._timestamp)) {
			return 1;
		}
		return Double.compare(_secondPoint._distance, inOther._secondPoint._distance);
	}
}
