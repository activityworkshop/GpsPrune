package tim.prune.data;

/**
 * Class to do additional range calculations including gradients
 * Used by full details display as well as the EstimateTime functions.
 */
public class RangeStatsWithGradients extends RangeStats
{
	private AltitudeRange _gentleAltitudeRange = new AltitudeRange();
	private AltitudeRange _steepAltitudeRange = new AltitudeRange();
	private Altitude _prevAltitude = null;
	private double _radsSinceLastAltitude = 0.0;

	private static final double STEEP_ANGLE = 0.15; // gradient steeper than 15% counts as steep


	/**
	 * Default constructor
	 */
	public RangeStatsWithGradients()
	{
		super();
	}

	/**
	 * Constructor
	 * @param inTrack track object
	 * @param inStartIndex start index
	 * @param inEndIndex end index
	 */
	public RangeStatsWithGradients(Track inTrack, int inStartIndex, int inEndIndex)
	{
		super();
		populateFromTrack(inTrack, inStartIndex, inEndIndex);
	}

	/**
	 * Add the given point to the calculations
	 * @param inPoint incoming point
	 */
	protected void doFurtherCalculations(DataPoint inPoint)
	{
		if (_prevPoint != null)
		{
			// Keep track of rads since last point with an altitude
			double rads = DataPoint.calculateRadiansBetween(_prevPoint, inPoint);
			_radsSinceLastAltitude += rads;
		}

		if (inPoint.hasAltitude())
		{
			Altitude altitude = inPoint.getAltitude();

			if (!inPoint.getSegmentStart() && _prevAltitude != null)
			{
				// Work out gradient, see whether to ignore/add to gentle or steep
				double heightDiff = altitude.getMetricValue() - _prevAltitude.getMetricValue();
				double metricDist = Distance.convertRadiansToDistance(_radsSinceLastAltitude, UnitSetLibrary.UNITS_METRES);
				final boolean isSteep = metricDist < 0.001 || (Math.abs(heightDiff / metricDist) > STEEP_ANGLE);
				if (isSteep)
				{
					_steepAltitudeRange.ignoreValue(_prevAltitude);
					_steepAltitudeRange.addValue(altitude);
				}
				else
				{
					_gentleAltitudeRange.ignoreValue(_prevAltitude);
					_gentleAltitudeRange.addValue(altitude);
				}
			}
			_prevAltitude = altitude;
			_radsSinceLastAltitude = 0.0;
		}

	}

	/** @return altitude range of range just considering low gradient bits */
	public AltitudeRange getGentleAltitudeRange() {
		return _gentleAltitudeRange;
	}

	/** @return altitude range of range just considering high gradient bits */
	public AltitudeRange getSteepAltitudeRange() {
		return _steepAltitudeRange;
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
}
