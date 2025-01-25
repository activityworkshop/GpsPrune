package tim.prune.function.comparesegments;

import tim.prune.data.*;
import tim.prune.function.PointUtils;

import java.util.List;

/**
 * Responsible for looping over a list of lines provided by SegmentData,
 * and efficiently only looking forwards through the list rather than skipping back
 * to previous lines.
 */
public class SegmentLooper
{
	private final List<LineAndBearing> _lines;
	private int _currentLine = 0;

	private static final double BEARING_TOLERANCE_DEGREES = 20.0;
	private static final double DISTANCE_TOLERANCE_RADIANS = Distance.convertDistanceToRadians(25.0, UnitSetLibrary.UNITS_METRES);
	private static final double NO_MATCH_FOUND = -1.0;


	public SegmentLooper(List<LineAndBearing> inLines) {
		_lines = inLines;
	}

	/**
	 * Try to match the given point with any line beyond the current one
	 * @return a result object if an intersection could be found, otherwise null
	 */
	public IntersectionResult match(PointData inPoint)
	{
		for (int i=_currentLine; i<_lines.size(); i++)
		{
			final IntersectionResult result = matchFound(inPoint, _lines.get(i));
			if (result != null)
			{
				_currentLine = i;
				return result;
			}
		}
		return null;
	}

	/**
	 * Try to match the given point with a single line
	 * @return a result object if an intersection could be found, otherwise null
	 */
	private IntersectionResult matchFound(PointData inPoint, LineAndBearing inLine)
	{
		// Check that two bearings are within a tolerance of BEARING_TOLERANCE_DEGREES
		final double bearingDifference = Bearing.angleDifferenceDegrees(inPoint._bearing, inLine._bearing);
		if (bearingDifference >= BEARING_TOLERANCE_DEGREES) {
			return null;
		}
		// Project point at right angles to bearing
		DataPoint pointRight = PointUtils.projectPoint(inPoint._point, Math.toRadians(inPoint._bearing + 90.0), DISTANCE_TOLERANCE_RADIANS);
		DataPoint pointLeft = PointUtils.projectPoint(inPoint._point, Math.toRadians(inPoint._bearing - 90.0), DISTANCE_TOLERANCE_RADIANS);
		DataPoint lineFromPoint = inLine.getFromPoint();
		DataPoint lineToPoint = inLine.getToPoint();
		if (!overlap(pointLeft.getLongitude(), pointRight.getLongitude(), lineFromPoint.getLongitude(), lineToPoint.getLongitude())
			|| !overlap(pointLeft.getLatitude(), pointRight.getLatitude(), lineFromPoint.getLatitude(), lineToPoint.getLatitude()))
		{
			// There is no overlap in latitude or longitude, so there's no overlap
			return null;
		}
		// Now we need to do the actual intersection
		final double lineFraction = calculateLineFraction(pointLeft, pointRight, lineFromPoint, lineToPoint);
		if (lineFraction < 0.0 || lineFraction > 1.0) {
			return null;
		}
		long totalMillis = inLine.getMilliseconds();
		Timestamp timestamp = lineFromPoint.getTimestamp().addOffsetMilliseconds((long) (totalMillis * lineFraction));
		double lineDistRadians = inLine._distToFromPointRadians + lineFraction * inLine._distAlongRadians;
		final double speed1 = inPoint._speedRadiansPerSec;
		final double speed2 = inLine.getInterpolatedSpeed(lineFraction);
		// we'll interpolate the cut point too to be able to show it if we need to
		DataPoint calculatedPoint = PointUtils.interpolate(lineFromPoint, lineToPoint, lineFraction);
		return new IntersectionResult(inPoint._point, calculatedPoint,
			inPoint._point.getTimestamp(), timestamp,
			inPoint._distanceToHereRadians, lineDistRadians,
			speed1, speed2);
	}

	/**
	 * Calculate the intersection between two straight lines on a plane
	 * @param pointLeft start point of perpendicular
	 * @param pointRight end point of perpendicular
	 * @param fromPoint start point of line segment to be cut
	 * @param toPoint end point of line segment to be cut
	 * @return fractional distance along line segment at which the intersection occurs (from 0 to 1) or -1 if not possible
	 */
	public static double calculateLineFraction(DataPoint pointLeft, DataPoint pointRight, DataPoint fromPoint, DataPoint toPoint)
	{
		// Coordinates of perpendicular cutting line
		final double x1 = pointLeft.getLongitude().getDouble();
		final double x2 = pointRight.getLongitude().getDouble();
		final double y1 = pointLeft.getLatitude().getDouble();
		final double y2 = pointRight.getLatitude().getDouble();
		// Coordinates of segment line
		final double u1 = fromPoint.getLongitude().getDouble();
		final double u2 = toPoint.getLongitude().getDouble();
		final double v1 = fromPoint.getLatitude().getDouble();
		final double v2 = toPoint.getLatitude().getDouble();

		// Cutting line is defined by:
		//	  x = x1 + s (x2 - x1)	 where 0 <= s <= 1
		// and  y = y1 + s (y2 - y1)
		// Segment line is similarly defined by:
		//	  u = u1 + t (u2 - u1)	 where 0 <= t <= 1
		// and  v = v1 + t (v2 - v1)
		// We want to find the intersection point x=u, y=v
		// where both s and t are between 0 and 1.
		// Then we will return the fraction t if it's valid, or -1 if not

		// Special case: x2 == x1, which means original line is horizontal (E/W) and perpendicular is exactly vertical (N/S)
		if (isNearlyZero(x2 - x1))
		{
			// Then: x = x1 = u1 + t (u2 - u1)
			//	   t = (x1 - u1) / (u2 - u1)
			if (isNearlyZero(u2 - u1))
			{
				// u2 == u1 means that segment line is also vertical, but this should have been excluded earlier
				// due to the angle tolerance check
				return NO_MATCH_FOUND;
			}
			return checkWithinRange((x1 - u1) / (u2 - u1));
		}

		// Special case: y2 == y1, which means original line is vertical (N/S) and perpendicular is exactly horizontal (E/W)
		if (isNearlyZero(y2 - y1))
		{
			// Then: y = y1 = v1 + t (v2 - v1)
			//	   t = (y1 - v1) / (v2 - v1)
			if (isNearlyZero(v2 - v1))
			{
				// v2 == v1 means that segment line is also horizontal, but this should have been excluded earlier
				// due to the angle tolerance check
				return NO_MATCH_FOUND;
			}
			return checkWithinRange((y1 - v1) / (v2 - v1));
		}

		// More general case: cutting line is neither horizontal nor vertical, but at some other angle
		//	  x1 + s (x2 - x1) = u1 + t (u2 - u1)
		// and  y1 + s (y2 - y1) = v1 + t (v2 - v1)
		// so   s = (u1 - x1 + t (u2 - u1)) / (x2 - x1)
		// it's known that (x2 - x1) != 0
		final double alpha = (u1 - x1) / (x2 - x1);
		final double beta = (u2 - u1) / (x2 - x1);
		// therefore s = alpha + t * beta
		//	  y1 + (alpha + t * beta)(y2 - y1) = v1 + t (v2 - v1)
		//	  t * (beta*(y2 - y1) - (v2 - v1)) = v1 - y1 - alpha * (y2 - y1)
		final double gamma = beta * (y2 - y1) + v1 - v2;
		if (isNearlyZero(gamma)) {
			return NO_MATCH_FOUND; // lines are parallel, this shouldn't happen because of angle tolerance check
		}
		// now we have a value for t
		final double t = (v1 - y1 - alpha * (y2 - y1)) / gamma;
		final double s = alpha + t * beta;
		if (checkWithinRange(s) >= 0.0) {
			return checkWithinRange(t);
		}
		return NO_MATCH_FOUND;
	}

	/** @return true if the given number is close to zero */
	private static boolean isNearlyZero(double gamma) {
		return Math.abs(gamma) < 1e-10;
	}

	/** @return validated value, or NO_MATCH_FOUND if out of range */
	private static double checkWithinRange(double inValue)
	{
		if (inValue < 0.0 || inValue > 1.0) {
			return NO_MATCH_FOUND;
		}
		return inValue;
	}

	/**
	 * @return true if there is any overlap between the lat/long ranges (1 to 2) and (3 to 4)
	 */
	private static boolean overlap(Coordinate inCoord1, Coordinate inCoord2, Coordinate inCoord3, Coordinate inCoord4)
	{
		if (Math.abs(inCoord1.getDouble() - inCoord2.getDouble()) > 1.0
				|| Math.abs(inCoord3.getDouble() - inCoord4.getDouble()) > 1.0)
		{
			// TODO: Problems here by the date line if projected points are on either side or if line segment crosses over
			return false;
		}
		final double min1 = Math.min(inCoord1.getDouble(), inCoord2.getDouble());
		final double max1 = Math.max(inCoord1.getDouble(), inCoord2.getDouble());
		final double min2 = Math.min(inCoord3.getDouble(), inCoord4.getDouble());
		final double max2 = Math.max(inCoord3.getDouble(), inCoord4.getDouble());
		return max2 >= min1 && max1 >= min2;
	}
}
