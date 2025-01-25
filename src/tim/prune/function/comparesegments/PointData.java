package tim.prune.function.comparesegments;

import tim.prune.data.DataPoint;

/**
 * Hold the information about a single point in the segment,
 * including the bearing of the track at this point, and the
 * cumulative distance from the start of the segment to here
 */
class PointData
{
	final DataPoint _point;
	final double _bearing;
	final double _distanceToHereRadians;
	final double _distanceToPrevPointRadians;
	final double _speedRadiansPerSec;


	PointData(DataPoint inPoint, double inBearing, double inDistanceToPrevPointRadians,
		double inDistanceToHereRadians, double inSpeedRadsPerSec)
	{
		_point = inPoint;
		_bearing = inBearing;
		_distanceToPrevPointRadians = inDistanceToPrevPointRadians;
		_distanceToHereRadians = inDistanceToHereRadians;
		_speedRadiansPerSec = inSpeedRadsPerSec;
	}
}
