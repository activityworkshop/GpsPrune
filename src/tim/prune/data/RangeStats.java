package tim.prune.data;

/**
 * Class to do basic calculations of range statistics such as distances, durations,
 * and altitude ranges, and to hold the results of the calculations.
 */
public class RangeStats
{
	private int _numPoints   = 0;
	private int _numSegments = 0;
	private boolean _foundTrackPoint = false;
	protected AltitudeRange _totalAltitudeRange = new AltitudeRange();
	protected AltitudeRange _movingAltitudeRange = new AltitudeRange();
	private Timestamp _earliestTimestamp = null, _latestTimestamp = null;
	private long _movingMilliseconds = 0L;
	private boolean _timesIncomplete = false;
	private boolean _timesOutOfSequence = false;
	protected double _totalDistanceRads = 0.0, _movingDistanceRads = 0.0;
	protected DataPoint _prevPoint = null;


	/** Constructor */
	public RangeStats()
	{}

	/**
	 * Constructor giving Track
	 * @param inTrack track object to calculate with
	 */
	public RangeStats(Track inTrack, int inStartIndex, int inEndIndex)
	{
		populateFromTrack(inTrack, inStartIndex, inEndIndex);
	}

	/**
	 * Add the specified points from the given track to the calculations
	 * @param inTrack track object
	 * @param inStartIndex start index (inclusive)
	 * @param inEndIndex end index (inclusive)
	 */
	protected void populateFromTrack(Track inTrack, int inStartIndex, int inEndIndex)
	{
		for (int i=inStartIndex; i<=inEndIndex; i++)
		{
			addPoint(inTrack.getPoint(i));
		}
	}

	/**
	 * @param inPoint point to add to the calculations
	 */
	public void addPoint(DataPoint inPoint)
	{
		if (inPoint == null)
		{
			return;
		}
		_numPoints++;
		// ignore all waypoints
		if (inPoint.isWaypoint()) {
			return;
		}
		if (inPoint.getSegmentStart() || !_foundTrackPoint) {
			_numSegments++;
		}
		_foundTrackPoint = true;
		// Get the distance to the previous track point
		if (_prevPoint != null)
		{
			double rads = DataPoint.calculateRadiansBetween(_prevPoint, inPoint);
			_totalDistanceRads += rads;
			if (!inPoint.getSegmentStart()) {
				_movingDistanceRads += rads;
			}
		}

		// timestamps
		if (inPoint.hasTimestamp())
		{
			Timestamp currTstamp = inPoint.getTimestamp();
			if (_earliestTimestamp == null || currTstamp.isBefore(_earliestTimestamp)) {
				_earliestTimestamp = currTstamp;
			}
			if (_latestTimestamp == null || currTstamp.isAfter(_latestTimestamp)) {
				_latestTimestamp = currTstamp;
			}
			// Work out duration without segment gaps
			if (!inPoint.getSegmentStart() && _prevPoint != null && _prevPoint.hasTimestamp())
			{
				long millisLater = currTstamp.getMillisecondsSince(_prevPoint.getTimestamp());
				if (millisLater < 0) {
					_timesOutOfSequence = true;
				}
				else {
					_movingMilliseconds += millisLater;
				}
			}
		}
		else {
			_timesIncomplete = true;
		}

		// altitudes
		if (inPoint.hasAltitude())
		{
			Altitude altitude = inPoint.getAltitude();
			_totalAltitudeRange.addValue(altitude);
			if (inPoint.getSegmentStart()) {
				_movingAltitudeRange.ignoreValue(altitude);
			}
			else
			{
				_movingAltitudeRange.addValue(altitude);
			}
		}

		// allow child classes to do additional calculations
		doFurtherCalculations(inPoint);

		_prevPoint = inPoint;
	}

	/**
	 * Hook for subclasses to do what they want in addition
	 * @param inPoint incoming point
	 */
	protected void doFurtherCalculations(DataPoint inPoint)
	{
	}


	/** @return number of points in range */
	public int getNumPoints() {
		return _numPoints;
	}

	/** @return number of segments in range */
	public int getNumSegments() {
		return _numSegments;
	}

	/** @return altitude range of range including segment gaps */
	public AltitudeRange getTotalAltitudeRange() {
		return _totalAltitudeRange;
	}

	/** @return altitude range of range just within segments */
	public AltitudeRange getMovingAltitudeRange() {
		return _movingAltitudeRange;
	}

	/** @return the earliest timestamp found */
	public Timestamp getEarliestTimestamp() {
		return _earliestTimestamp;
	}

	/** @return the latest timestamp found */
	public Timestamp getLatestTimestamp() {
		return _latestTimestamp;
	}

	/** @return total number of seconds in the range */
	public long getTotalDurationInSeconds()
	{
		if (_earliestTimestamp != null && _latestTimestamp != null) {
			return _latestTimestamp.getSecondsSince(_earliestTimestamp);
		}
		return 0L;
	}

	/** @return number of seconds within the segments of the range */
	public long getMovingDurationInSeconds()
	{
		return _movingMilliseconds / 1000;
	}

	/** @return true if any timestamps are missing */
	public boolean getTimestampsIncomplete() {
		return _timesIncomplete;
	}

	/** @return true if any timestamps are out of sequence */
	public boolean getTimestampsOutOfSequence() {
		return _timesOutOfSequence;
	}

	/** @return total distance in the current distance units (km or mi) */
	public double getTotalDistance() {
		return Distance.convertRadiansToDistance(_totalDistanceRads);
	}

	/** @return moving distance in the current distance units (km or mi) */
	public double getMovingDistance() {
		return Distance.convertRadiansToDistance(_movingDistanceRads);
	}

	/** @return moving distance in km */
	public double getMovingDistanceKilometres() {
		return Distance.convertRadiansToDistance(_movingDistanceRads, UnitSetLibrary.UNITS_KILOMETRES);
	}

	/**
	 * @return the total vertical speed (including segment gaps) in metric units
	 */
	public double getTotalVerticalSpeed()
	{
		long time = getTotalDurationInSeconds();
		if (time > 0 && _totalAltitudeRange.hasRange()) {
			return _totalAltitudeRange.getMetricHeightDiff() / time;
		}
		return 0.0;
	}

	/**
	 * @return the moving vertical speed (ignoring segment gaps) in metric units
	 */
	public double getMovingVerticalSpeed()
	{
		long time = getMovingDurationInSeconds();
		if (time > 0 && _movingAltitudeRange.hasRange()) {
			return _movingAltitudeRange.getMetricHeightDiff() / time;
		}
		return 0.0;
	}
}
