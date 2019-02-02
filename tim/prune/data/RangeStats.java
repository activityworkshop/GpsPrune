package tim.prune.data;

import tim.prune.config.Config;

/**
 * Class to do calculations of range statistics such as distances, durations,
 * speeds, gradients etc, and to hold the results of the calculations.
 * Used by FullRangeDetails as well as the EstimateTime functions.
 */
public class RangeStats
{
	// MAYBE: Split into basic stats (quick to calculate, for detailsdisplay) and full stats (for other two)
	private boolean _valid = false;
	private int     _numPoints   = 0;
	private int     _startIndex = 0, _endIndex = 0;
	private int     _numSegments = 0;
	private AltitudeRange _totalAltitudeRange = null, _movingAltitudeRange = null;
	private AltitudeRange _gentleAltitudeRange = null, _steepAltitudeRange = null;
	private Timestamp _earliestTimestamp = null, _latestTimestamp = null;
	private long _movingMilliseconds = 0L;
	private boolean _timesIncomplete = false;
	private boolean _timesOutOfSequence = false;
	private double _totalDistanceRads = 0.0, _movingDistanceRads = 0.0;
	// Note, maximum speed is not calculated here, use the SpeedData class instead

	private static final double STEEP_ANGLE = 0.15; // gradient steeper than 15% counts as steep


	/**
	 * Constructor
	 * @param inTrack track to compile data for
	 * @param inStartIndex start index of range to examine
	 * @param inEndIndex end index (inclusive) of range to examine
	 */
	public RangeStats(Track inTrack, int inStartIndex, int inEndIndex)
	{
		if (inTrack != null && inStartIndex >= 0 && inEndIndex > inStartIndex
			&& inEndIndex < inTrack.getNumPoints())
		{
			_valid = calculateStats(inTrack, inStartIndex, inEndIndex);
		}
	}

	/**
	 * Calculate the statistics and populate the member variables with the results
	 * @param inTrack track
	 * @param inStartIndex start index of range
	 * @param inEndIndex end index (inclusive) of range
	 * @return true on success
	 */
	private boolean calculateStats(Track inTrack, int inStartIndex, int inEndIndex)
	{
		_startIndex = inStartIndex;
		_endIndex = inEndIndex;
		_numPoints = inEndIndex - inStartIndex + 1;
		_totalAltitudeRange  = new AltitudeRange();
		_movingAltitudeRange = new AltitudeRange();
		_gentleAltitudeRange = new AltitudeRange();
		_steepAltitudeRange  = new AltitudeRange();
		DataPoint prevPoint = null;
		Altitude prevAltitude = null;
		_totalDistanceRads = _movingDistanceRads = 0.0;
		double radsSinceLastAltitude = 0.0;
		_movingMilliseconds = 0L;

		// Loop over the points in the range
		for (int i=inStartIndex; i<= inEndIndex; i++)
		{
			DataPoint p = inTrack.getPoint(i);
			if (p == null) return false;
			// ignore all waypoints
			if (p.isWaypoint()) continue;

			if (p.getSegmentStart()) {
				_numSegments++;
			}
			// Get the distance to the previous track point
			if (prevPoint != null)
			{
				double rads = DataPoint.calculateRadiansBetween(prevPoint, p);
				_totalDistanceRads += rads;
				if (!p.getSegmentStart()) {
					_movingDistanceRads += rads;
				}
				// Keep track of rads since last point with an altitude
				radsSinceLastAltitude += rads;
			}
			// Get the altitude difference to the previous track point
			if (p.hasAltitude())
			{
				Altitude altitude = p.getAltitude();
				_totalAltitudeRange.addValue(altitude);
				if (p.getSegmentStart()) {
					_movingAltitudeRange.ignoreValue(altitude);
				}
				else
				{
					_movingAltitudeRange.addValue(altitude);
					if (prevAltitude != null)
					{
						// Work out gradient, see whether to ignore/add to gentle or steep
						double heightDiff = altitude.getMetricValue() - prevAltitude.getMetricValue();
						double metricDist = Distance.convertRadiansToDistance(radsSinceLastAltitude, UnitSetLibrary.UNITS_METRES);
						final boolean isSteep = metricDist < 0.001 || (Math.abs(heightDiff / metricDist) > STEEP_ANGLE);
						if (isSteep) {
							_steepAltitudeRange.ignoreValue(prevAltitude);
							_steepAltitudeRange.addValue(altitude);
						}
						else {
							_gentleAltitudeRange.ignoreValue(prevAltitude);
							_gentleAltitudeRange.addValue(altitude);
						}
					}
				}
				prevAltitude = altitude;
				radsSinceLastAltitude = 0.0;
			}

			if (p.hasTimestamp())
			{
				if (_earliestTimestamp == null || p.getTimestamp().isBefore(_earliestTimestamp)) {
					_earliestTimestamp = p.getTimestamp();
				}
				if (_latestTimestamp == null || p.getTimestamp().isAfter(_latestTimestamp)) {
					_latestTimestamp = p.getTimestamp();
				}
				// Work out duration without segment gaps
				if (!p.getSegmentStart() && prevPoint != null && prevPoint.hasTimestamp())
				{
					long millisLater = p.getTimestamp().getMillisecondsSince(prevPoint.getTimestamp());
					if (millisLater < 0) {_timesOutOfSequence = true;}
					else {
						_movingMilliseconds += millisLater;
					}
				}
			}
			else {
				_timesIncomplete = true;
			}

			prevPoint = p;
		}
		return true;
	}


	/** @return true if results are valid */
	public boolean isValid() {
		return _valid;
	}

	/** @return start index of range */
	public int getStartIndex() {
		return _startIndex;
	}

	/** @return end index of range */
	public int getEndIndex() {
		return _endIndex;
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

	/** @return altitude range of range just considering low gradient bits */
	public AltitudeRange getGentleAltitudeRange() {
		return _gentleAltitudeRange;
	}

	/** @return altitude range of range just considering high gradient bits */
	public AltitudeRange getSteepAltitudeRange() {
		return _steepAltitudeRange;
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

	/** @return the total gradient in % (including segment gaps) */
	public double getTotalGradient()
	{
		double dist = Distance.convertRadiansToDistance(_totalDistanceRads, UnitSetLibrary.UNITS_METRES);
		if (dist > 0.0 && _totalAltitudeRange.hasRange()) {
			return _totalAltitudeRange.getMetricHeightDiff() / dist * 100.0;
		}
		return 0.0;
	}

	/** @return the moving gradient in % (ignoring segment gaps) */
	public double getMovingGradient()
	{
		double dist = Distance.convertRadiansToDistance(_movingDistanceRads, UnitSetLibrary.UNITS_METRES);
		if (dist > 0.0 && _movingAltitudeRange.hasRange()) {
			return _movingAltitudeRange.getMetricHeightDiff() / dist * 100.0;
		}
		return 0.0;
	}

	/** @return the total vertical speed (including segment gaps) in current vspeed units */
	public double getTotalVerticalSpeed()
	{
		long time = getTotalDurationInSeconds();
		if (time > 0 && _totalAltitudeRange.hasRange()) {
			return _totalAltitudeRange.getMetricHeightDiff() / time * Config.getUnitSet().getVerticalSpeedUnit().getMultFactorFromStd();
		}
		return 0.0;
	}

	/** @return the moving vertical speed (ignoring segment gaps) in current vspeed units */
	public double getMovingVerticalSpeed()
	{
		long time = getMovingDurationInSeconds();
		if (time > 0 && _movingAltitudeRange.hasRange()) {
			return _movingAltitudeRange.getMetricHeightDiff() / time * Config.getUnitSet().getVerticalSpeedUnit().getMultFactorFromStd();
		}
		return 0.0;
	}
}
