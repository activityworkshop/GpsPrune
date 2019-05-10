package tim.prune.function.compress;

import tim.prune.data.DataPoint;
import tim.prune.data.Track;

/**
 * Class to hold details about a track
 * which might be useful for compression
 */
public class TrackDetails
{
	/** Track object */
	private Track _track = null;
	/** Range span */
	private double _trackSpan = -1.0;
	/** Markers for start of segment */
	private boolean[] _segmentStarts = null;
	/** Markers for end of segment */
	private boolean[] _segmentEnds = null;
	/** Mean distance between track points in radians */
	private double _meanRadians = 0.0;


	/**
	 * Constructor
	 * @param inTrack track object
	 */
	public TrackDetails(Track inTrack)
	{
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
		_trackSpan = (xRange > yRange ? xRange : yRange);

		// Calculate segment starts / ends
		int numPoints = _track.getNumPoints();
		_segmentStarts = new boolean[numPoints];
		_segmentEnds = new boolean[numPoints];
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
				else {
					// Add up distances between points within the same track segment
					if (prevTrackPointIndex >= 0) {
						numDistances++;
						totalRadians += DataPoint.calculateRadiansBetween(_track.getPoint(prevTrackPointIndex), point);
					}
				}
				prevTrackPointIndex = i;
			}
		}
		// last segment
		_segmentEnds[prevTrackPointIndex] = true;
		// Mean radians between points
		_meanRadians = totalRadians / numDistances;
	}


	/**
	 * @return the track span
	 */
	public double getTrackSpan()
	{
		if (_trackSpan < 0.0) {initialise();}
		return _trackSpan;
	}

	/**
	 * @param inPointIndex index of point to check
	 * @return true if specified point is start of segment
	 */
	public boolean isSegmentStart(int inPointIndex)
	{
		if (_segmentStarts == null ||
			_segmentStarts.length != _track.getNumPoints()) {initialise();}
		return _segmentStarts[inPointIndex];
	}

	/**
	 * @param inPointIndex index of point to check
	 * @return true if specified point is end of segment
	 */
	public boolean isSegmentEnd(int inPointIndex)
	{
		if (_segmentEnds == null ||
			_segmentEnds.length != _track.getNumPoints()) {initialise();}
		return _segmentEnds[inPointIndex];
	}

	/**
	 * @return mean radians between adjacent track points
	 */
	public double getMeanRadians()
	{
		if (_meanRadians == 0.0) {initialise();}
		return _meanRadians;
	}
}
