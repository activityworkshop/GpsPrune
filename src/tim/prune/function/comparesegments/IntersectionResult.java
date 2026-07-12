package tim.prune.function.comparesegments;

import tim.prune.data.DataPoint;
import tim.prune.data.Timestamp;

/**
 * The result of intersecting a point from one segment
 * with the lines from another segment
 */
public class IntersectionResult implements Comparable<IntersectionResult>
{
	private record ResultPoint(DataPoint point, Timestamp timestamp, double distance, double speed) {
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
		return _firstPoint.point();
	}

	public DataPoint getSecondPoint() {
		return _secondPoint.point();
	}

	public long getFirstDurationSeconds(IntersectionResult inOther) {
		return _firstPoint.timestamp().getSecondsSince(inOther._firstPoint.timestamp());
	}

	public long getSecondDurationSeconds(IntersectionResult inOther) {
		return _secondPoint.timestamp().getSecondsSince(inOther._secondPoint.timestamp());
	}

	public double getFirstDistanceRadians(IntersectionResult inOther) {
		return _firstPoint.distance() - inOther._firstPoint.distance();
	}

	public double getSecondDistanceRadians(IntersectionResult inOther) {
		return _secondPoint.distance() - inOther._secondPoint.distance();
	}

	public double getDeltaSpeedRadiansPerSec() {
		return _secondPoint.speed() - _firstPoint.speed();
	}

	public int compareTo(IntersectionResult inOther)
	{
		if (inOther == null || _firstPoint.timestamp().isBefore(inOther._firstPoint.timestamp())) {
			return -1;
		}
		if (inOther._firstPoint.timestamp().isBefore(_firstPoint.timestamp())) {
			return 1;
		}
		int distCompare = Double.compare(_firstPoint.distance(), inOther._firstPoint.distance());
		if (distCompare != 0) {
			return distCompare;
		}
		if (_secondPoint.timestamp().isBefore(inOther._secondPoint.timestamp())) {
			return -1;
		}
		if (inOther._secondPoint.timestamp().isBefore(_secondPoint.timestamp())) {
			return 1;
		}
		return Double.compare(_secondPoint.distance(), inOther._secondPoint.distance());
	}
}
