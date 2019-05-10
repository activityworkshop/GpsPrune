package tim.prune.data;

import tim.prune.config.Config;

/**
 * Represents a range of altitudes, taking units into account.
 * Values assumed to be >= 0.
 */
public class AltitudeRange
{
	/** Range of altitudes in metres */
	private IntegerRange _range = new IntegerRange();
	/** Flag for whether previous value exists or not */
	private boolean _gotPreviousValue;
	/** Previous metric value */
	private int _previousValue;
	/** Total climb in metres */
	private int _climb;
	/** Total descent in metres */
	private int _descent;
	/** Flags for whether minimum or maximum has been found */
	private boolean _gotPreviousMinimum = false, _gotPreviousMaximum = false;
	/** Integer values of previous minimum and maximum, if any */
	private int     _previousExtreme = 0;


	/**
	 * Constructor
	 */
	public AltitudeRange() {
		clear();
	}

	/**
	 * Clear the altitude range
	 */
	public void clear()
	{
		_range.clear();
		_climb = _descent = 0;
		_gotPreviousValue = false;
		_previousValue = 0;
		_gotPreviousMinimum = _gotPreviousMaximum = false;
		_previousExtreme = 0;
	}


	/**
	 * Add a value to the range
	 * @param inAltitude value to add, only positive values considered
	 */
	public void addValue(Altitude inAltitude)
	{
		final int wiggleLimit = Config.getConfigInt(Config.KEY_ALTITUDE_TOLERANCE) / 100;

		if (inAltitude != null && inAltitude.isValid())
		{
			int altValue = (int) inAltitude.getMetricValue();
			_range.addValue(altValue);
			// Compare with previous value if any
			if (_gotPreviousValue)
			{
				if (altValue != _previousValue)
				{
					// Got an altitude value which is different from the previous one
					final boolean locallyUp = (altValue > _previousValue);
					final boolean overallUp = _gotPreviousMinimum && _previousValue > _previousExtreme;
					final boolean overallDn = _gotPreviousMaximum && _previousValue < _previousExtreme;
					final boolean moreThanWiggle = Math.abs(altValue - _previousValue) > wiggleLimit;
					// Do we know whether we're going up or down yet?
					if (!_gotPreviousMinimum && !_gotPreviousMaximum)
					{
						// we don't know whether we're going up or down yet - check limit
						if (moreThanWiggle)
						{
							if (locallyUp) {_gotPreviousMinimum = true;}
							else {_gotPreviousMaximum = true;}
							_previousExtreme = _previousValue;
							_previousValue = altValue;
							_gotPreviousValue = true;
						}
					}
					else if (overallUp)
					{
						if (locallyUp) {
							// we're still going up - do nothing
							_previousValue = altValue;
						}
						else if (moreThanWiggle)
						{
							// we're going up but have dropped over a maximum
							// Add the climb from _previousExtreme up to _previousValue
							_climb += (_previousValue - _previousExtreme);
							_previousExtreme = _previousValue;
							_gotPreviousMinimum = false; _gotPreviousMaximum = true;
							_previousValue = altValue;
							_gotPreviousValue = true;
						}
					}
					else if (overallDn)
					{
						if (locallyUp) {
							if (moreThanWiggle)
							{
								// we're going down but have climbed up from a minimum
								// Add the descent from _previousExtreme down to _previousValue
								_descent += (_previousExtreme - _previousValue);
								_previousExtreme = _previousValue;
								_gotPreviousMinimum = true; _gotPreviousMaximum = false;
								_previousValue = altValue;
								_gotPreviousValue = true;
							}
						}
						else {
							// we're still going down - do nothing
							_previousValue = altValue;
							_gotPreviousValue = true;
						}
					}
					// TODO: Behaviour when WIGGLE_LIMIT == 0 should be same as before, all differences cumulated
				}
			}
			else
			{
				// we haven't got a previous value at all, so it's the start of a new segment
				_previousValue = altValue;
				_gotPreviousValue = true;
			}
		}
	}

	/**
	 * Reset the climb/descent calculations starting from the given value
	 * @param inAltitude altitude value
	 */
	public void ignoreValue(Altitude inAltitude)
	{
		// Process the previous value, if any, to update climb/descent as that's the end of the previous segment
		if (_gotPreviousValue && _gotPreviousMinimum && _previousValue > _previousExtreme) {
			_climb += (_previousValue - _previousExtreme);
		}
		else if (_gotPreviousValue && _gotPreviousMaximum && _previousValue < _previousExtreme) {
			_descent += (_previousExtreme - _previousValue);
		}
		// Eliminate the counting values to start the new segment
		_gotPreviousMinimum = _gotPreviousMaximum = false;
		_gotPreviousValue = false;
		// Now process this value if there is one
		if (inAltitude != null && inAltitude.isValid())
		{
			final int altValue = (int) inAltitude.getMetricValue();
			_range.addValue(altValue);
			_previousValue = altValue;
			_gotPreviousValue = true;
		}
	}

	/**
	 * @return true if altitude range found
	 */
	public boolean hasRange()
	{
		return _range.hasValues();
	}


	/**
	 * @param inUnit altitude units to use
	 * @return minimum value
	 */
	public int getMinimum(Unit inUnit)
	{
		return (int) (_range.getMinimum() * inUnit.getMultFactorFromStd());
	}

	/**
	 * @param inUnit altitude units to use
	 * @return maximum value
	 */
	public int getMaximum(Unit inUnit)
	{
		return (int) (_range.getMaximum() * inUnit.getMultFactorFromStd());
	}

	/**
	 * @param inUnit altitude units to use
	 * @return total climb
	 */
	public int getClimb(Unit inUnit)
	{
		// May need to add climb from last segment
		int lastSegmentClimb = 0;
		if (_gotPreviousValue && _gotPreviousMinimum && _previousValue > _previousExtreme) {
			lastSegmentClimb = _previousValue - _previousExtreme;
		}
		return (int) ((_climb + lastSegmentClimb) * inUnit.getMultFactorFromStd());
	}

	/**
	 * @param inUnit altitude units to use
	 * @return total descent
	 */
	public int getDescent(Unit inUnit)
	{
		// May need to add descent from last segment
		int lastSegmentDescent = 0;
		if (_gotPreviousValue && _gotPreviousMaximum && _previousValue < _previousExtreme) {
			lastSegmentDescent = _previousExtreme - _previousValue;
		}
		return (int) ((_descent + lastSegmentDescent) * inUnit.getMultFactorFromStd());
	}

	/**
	 * @return overall height gain in metres
	 */
	public double getMetricHeightDiff()
	{
		return getClimb(UnitSetLibrary.UNITS_METRES) - getDescent(UnitSetLibrary.UNITS_METRES);
	}
}
