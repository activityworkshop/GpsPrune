package tim.prune.function.compress;

import tim.prune.data.DataPoint;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;

/**
 * Class to hold details about a track
 * which might be useful for compression
 */
public class TrackDetails
{
	/** Track object */
	private final Track _track;
	/** Range span */
	private double _trackSpan = -1.0;
	/** Track radians */
	private double _trackRadians = -1.0;
	/** Markers for start of segment */
	private boolean[] _segmentStarts = null;
	/** Markers for end of segment */
	private boolean[] _segmentEnds = null;
	/** Markers for waypoints */
	private boolean[] _waypoints = null;
	/** Mean distance between track points in radians */
	private double _meanRadians = 0.0;


	/**
	 * Constructor
	 * @param inTrack track object
	 */
	public TrackDetails(Track inTrack) {
		_track = inTrack;
	}

	/**
	 * Recalculate all details
	 */
	public void initialise()
	{
		// calculate track span
		double xRange = _track.getXRange().getRange();
		double yRange = _track.getYRange().getRange();
		_trackSpan = Math.max(xRange, yRange);
		_trackRadians = calculateMaxRadians(_track);

		// Calculate segment starts / ends
		int numPoints = _track.getNumPoints();
		_segmentStarts = new boolean[numPoints];
		_segmentEnds = new boolean[numPoints];
		_waypoints = new boolean[numPoints];
		int prevTrackPointIndex = -1;
		int numDistances = 0; double totalRadians = 0.0;
		// Loop over points
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = _track.getPoint(i);
			if (!point.isWaypoint())
			{
				// track point, check for segment flag
				if (point.getSegmentStart())
				{
					// set start of segment and end of previous
					_segmentStarts[i] = true;
					if (prevTrackPointIndex >= 0) {
						_segmentEnds[prevTrackPointIndex] = true;
					}
				}
				else
				{
					// Add up distances between points within the same track segment
					if (prevTrackPointIndex >= 0) {
						numDistances++;
						totalRadians += DataPoint.calculateRadiansBetween(_track.getPoint(prevTrackPointIndex), point);
					}
				}
				prevTrackPointIndex = i;
				_waypoints[i] = false;
			}
			else {
				_waypoints[i] = true;
			}
		}
		// last segment
		if (prevTrackPointIndex >= 0) {
			_segmentEnds[prevTrackPointIndex] = true;
		}
		// Mean radians between points
		_meanRadians = totalRadians / numDistances;
	}

	/** @return the approximate maximum radians between extreme points in the track */
	private static double calculateMaxRadians(Track inTrack)
	{
		DataPoint northernPoint = null, southernPoint = null;
		DataPoint westernPoint = null, easternPoint = null;
		for (int i=0; i<inTrack.getNumPoints(); i++)
		{
			DataPoint p = inTrack.getPoint(i);
			if (p.isWaypoint()) {
				continue;
			}
			final double latitude = p.getLatitude().getDouble();
			if (northernPoint == null || northernPoint.getLatitude().getDouble() < latitude) {
				northernPoint = p;
			}
			if (southernPoint == null || southernPoint.getLatitude().getDouble() > latitude) {
				southernPoint = p;
			}
			final double longitude = p.getLongitude().getDouble();
			if (westernPoint == null || westernPoint.getLongitude().getDouble() > longitude) {
				westernPoint = p;
			}
			if (easternPoint == null || easternPoint.getLongitude().getDouble() < longitude) {
				easternPoint = p;
			}
		}
		// Shouldn't happen because we should have found at least one point by now!
		if (northernPoint == null || southernPoint == null || easternPoint == null || westernPoint == null) {
			return 1.0;
		}
		final double northSouth = DataPoint.calculateRadiansBetween(northernPoint, southernPoint);
		final double eastWest = DataPoint.calculateRadiansBetween(easternPoint, westernPoint);
		return Math.max(northSouth, eastWest);
	}

	/**
	 * @return the track span
	 */
	public double getTrackSpan()
	{
		if (_trackSpan < 0.0) {
			initialise();
		}
		return _trackSpan;
	}

	/** @return the maximum radians */
	public double getMaxRadians()
	{
		if (_trackRadians < 0.0) {
			initialise();
		}
		return _trackRadians;
	}

	/**
	 * @param inPointIndex index of point to check
	 * @return true if specified point is start of segment
	 */
	public boolean isSegmentStart(int inPointIndex)
	{
		if (_segmentStarts == null ||
			_segmentStarts.length != _track.getNumPoints())
		{
			initialise();
		}
		return _segmentStarts[inPointIndex];
	}

	/**
	 * @param inPointIndex index of point to check
	 * @return true if specified point is end of segment
	 */
	public boolean isSegmentEnd(int inPointIndex)
	{
		if (_segmentEnds == null ||
			_segmentEnds.length != _track.getNumPoints())
		{
			initialise();
		}
		return _segmentEnds[inPointIndex];
	}

	/**
	 * @return mean radians between adjacent track points
	 */
	public double getMeanRadians()
	{
		if (_meanRadians == 0.0) {
			initialise();
		}
		return _meanRadians;
	}

	/**
	 * @param inPointIndex index of point to check
	 * @return true if specified point is a waypoint
	 */
	public boolean isWaypoint(int inPointIndex)
	{
		if (_waypoints == null ||
			_waypoints.length != _track.getNumPoints())
		{
			initialise();
		}
		return _waypoints[inPointIndex];
	}

	public TrackDetails modifyUsingMarkings(MarkingData inMarkings)
	{
		initialise();
		TrackDetails modified = new TrackDetails(_track);
		modified.initialise();
		int numPoints = _track.getNumPoints();
		int prevTrackPointIndex = -1;
		boolean setNextSegment = false;
		// Loop over points
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = _track.getPoint(i);
			if (point.isWaypoint()) {
				continue;
			}
			if (inMarkings.isPointMarkedForDeletion(i))
			{
				setNextSegment = setNextSegment
						|| _segmentStarts[i]
						|| point.getSegmentStart()
						|| inMarkings.isPointMarkedForSegmentBreak(i);
				continue;
			}
			if (setNextSegment)
			{
				modified._segmentStarts[i] = true;
				if (prevTrackPointIndex >= 0) {
					modified._segmentEnds[prevTrackPointIndex] = true;
				}
				setNextSegment = false;
			}
			prevTrackPointIndex = i;
		}
		if (prevTrackPointIndex >= 0) {
			modified._segmentEnds[prevTrackPointIndex] = true;
		}
		return modified;
	}
}
