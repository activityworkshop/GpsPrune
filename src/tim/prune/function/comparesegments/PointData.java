package tim.prune.function.comparesegments;

import tim.prune.data.DataPoint;

/**
 * Hold the information about a single point in the segment,
 * including the bearing of the track at this point, and the
 * cumulative distance from the start of the segment to here
 */
record PointData(DataPoint point, double bearing, double distanceToPrevPointRadians, double distanceToHereRadians,
				 double speedRadiansPerSec) {
}
