package tim.prune.function.comparesegments;

import tim.prune.data.Bearing;
import tim.prune.data.DataPoint;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the points and lines of a single segment ready for comparison
 */
public class SegmentData
{
	private final PointSequence _points = new PointSequence();
	private final ArrayList<LineAndBearing> _lines = new ArrayList<>();

	/** Maximum angle change at a point for it to be considered */
	private static final double MAX_ANGLE_CHANGE_DEGREES = 45.0;

	public SegmentData(Track inTrack, int inStartIndex)
	{
		fillPointData(inTrack, _points, inStartIndex);
		fillLineData(_points, _lines);
	}

	/** Fill the given PointSequence object with PointData objects for the specified segment */
	private static void fillPointData(Track inTrack, PointSequence inPoints, int inStartIndex)
	{
		ArrayList<DataPoint> points = createPoints(inTrack, inStartIndex);
		DataPoint prevPoint = null;
		DataPoint point = null;
		double radiansSoFar = 0.0;
		// Loop through the points in threes, taking prevPoint, point and nextPoint
		for (DataPoint nextPoint : points)
		{
			if (point != null)
			{
				// Here we consider the point with the one before it and the one after it
				final double currRadians = DataPoint.calculateRadiansBetween(prevPoint, point);
				radiansSoFar += currRadians;
				final Double bearing;
				if (prevPoint == null) {
					// first line in segment, just take bearing from this point to the next one
					bearing = Bearing.calculateDegrees(point, nextPoint);
				}
				else if (nextPoint == null) {
					// last line in segment, just take bearing from prevPoint to point
					bearing = Bearing.calculateDegrees(prevPoint, point);
				}
				else
				{
					// line in the middle
					// Check difference between bearing(prevPrev->prev) and bearing(prev, point)
					final double angleDiff = Bearing.calculateDegreeChange(prevPoint, point, nextPoint);
					if (angleDiff > MAX_ANGLE_CHANGE_DEGREES) {
						bearing = null;
					}
					else {
						bearing = Bearing.calculateDegrees(prevPoint, nextPoint);
					}
				}
				if (bearing != null)
				{
					final double speedRadsPerSec = getSpeedRadsPerSec(prevPoint, point, nextPoint);
					inPoints.addPoint(new PointData(point, bearing, currRadians, radiansSoFar, speedRadsPerSec));
				}
			}
			prevPoint = point;
			point = nextPoint;
		}
	}

	private static double getSpeedRadsPerSec(DataPoint inPrevPoint, DataPoint inPoint, DataPoint inNextPoint)
	{
		final Timestamp firstStamp = (inPrevPoint == null ? inPoint.getTimestamp() : inPrevPoint.getTimestamp());
		final Timestamp secondStamp = (inNextPoint == null ? inPoint.getTimestamp() : inNextPoint.getTimestamp());
		final long millis = secondStamp.getMillisecondsSince(firstStamp);
		double totalDistanceRads = DataPoint.calculateRadiansBetween(inPrevPoint, inPoint)
			+ DataPoint.calculateRadiansBetween(inPoint, inNextPoint);
		return totalDistanceRads * 1000.0 / millis;
	}

	/** @return a list of the datapoints in the specified segment, ending with null */
	private static ArrayList<DataPoint> createPoints(Track inTrack, int inStartIndex)
	{
		ArrayList<DataPoint> points = new ArrayList<>();
		for (int i=inStartIndex; i<inTrack.getNumPoints(); i++)
		{
			DataPoint point = inTrack.getPoint(i);
			if (point == null || point.isWaypoint()) {
				continue;
			}
			if (point.getSegmentStart() && i > inStartIndex) {
				break; // next segment found
			}
			// TODO: Check for a minimum distance between previous point and this one?
			points.add(point);
		}
		// Add a null point at the end so that we can consider the last triplet
		points.add(null);
		return points;
	}

	private void fillLineData(PointSequence inPoints, ArrayList<LineAndBearing> inLines)
	{
		PointData prevPoint = null;
		for (PointData pd : inPoints.getPoints())
		{
			if (prevPoint != null) {
				inLines.add(new LineAndBearing(prevPoint, pd));
			}
			prevPoint = pd;
		}
	}

	boolean isBefore(SegmentData inOther) {
		return _points.getFirstTimestamp().isBefore(inOther._points.getFirstTimestamp());
	}

	// TODO: Could be an immutable copy?
	PointSequence getPoints() {
		return _points;
	}

	// TODO: Could be an immutable copy?
	List<LineAndBearing> getLines() {
		return _lines;
	}
}
