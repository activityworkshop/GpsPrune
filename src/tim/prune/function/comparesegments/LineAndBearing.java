package tim.prune.function.comparesegments;

import tim.prune.data.Bearing;
import tim.prune.data.DataPoint;

/** Holds the information about a single line between two (not necessarily consecutive) track points and its bearing */
public class LineAndBearing
{
	final PointData _fromPoint;
	final PointData _toPoint;
	final double _bearing;
	final double _distToFromPointRadians;
	final double _distAlongRadians;

	LineAndBearing(PointData fromPoint, PointData toPoint)
	{
		_fromPoint = fromPoint;
		_toPoint = toPoint;
		_bearing = Bearing.calculateDegrees(_fromPoint._point, toPoint._point);
		_distToFromPointRadians = fromPoint._distanceToHereRadians;
		_distAlongRadians = toPoint._distanceToHereRadians - fromPoint._distanceToHereRadians;
	}

	DataPoint getFromPoint() {
		return _fromPoint._point;
	}

	DataPoint getToPoint() {
		return _toPoint._point;
	}

	long getMilliseconds() {
		return getToPoint().getTimestamp().getMillisecondsSince(getFromPoint().getTimestamp());
	}

	double getInterpolatedSpeed(double inFraction)
	{
		final double fromSpeed = _fromPoint._speedRadiansPerSec;
		final double toSpeed = _toPoint._speedRadiansPerSec;
		return fromSpeed + inFraction * (toSpeed - fromSpeed);
	}
}
