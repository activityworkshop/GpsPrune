package tim.prune.data;

/**
 * Represents a range of altitudes, taking units into account.
 * Values assumed to be >= 0.
 */
public class AltitudeRange
{
	/** Range of altitudes in metres */
	private IntegerRange _range = new IntegerRange();
	/** Empty flag */
	private boolean _empty;
	/** Previous metric value */
	private int _prevValue;
	/** Total climb in metres */
	private double _climb;
	/** Total descent in metres */
	private double _descent;


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
		_climb = 0.0;
		_descent = 0.0;
		_empty = true;
		_prevValue = 0;
	}


	/**
	 * Add a value to the range
	 * @param inAltitude value to add, only positive values considered
	 */
	public void addValue(Altitude inAltitude)
	{
		if (inAltitude != null && inAltitude.isValid())
		{
			int altValue = (int) inAltitude.getMetricValue();
			_range.addValue(altValue);
			// Compare with previous value if any
			if (!_empty)
			{
				if (altValue > _prevValue)
					_climb += (altValue - _prevValue);
				else
					_descent += (_prevValue - altValue);
			}
			_prevValue = altValue;
			_empty = false;
		}
	}

	/**
	 * Reset the climb/descent calculations starting from the given value
	 * @param inAltitude altitude value
	 */
	public void ignoreValue(Altitude inAltitude)
	{
		// If we set the empty flag to true, that has the same effect as restarting a segment
		_empty = true;
		addValue(inAltitude);
	}

	/**
	 * @return true if altitude range found
	 */
	public boolean hasRange()
	{
		return _range.getMaximum() > _range.getMinimum();
	}


	/**
	 * @param inUnit altitude units to use
	 * @return minimum value, or -1 if none found
	 */
	public int getMinimum(Unit inUnit)
	{
		if (_range.getMinimum() <= 0) return _range.getMinimum();
		return (int) (_range.getMinimum() * inUnit.getMultFactorFromStd());
	}

	/**
	 * @param inUnit altitude units to use
	 * @return maximum value, or -1 if none found
	 */
	public int getMaximum(Unit inUnit)
	{
		if (_range.getMaximum() <= 0) return _range.getMaximum();
		return (int) (_range.getMaximum() * inUnit.getMultFactorFromStd());
	}

	/**
	 * @param inUnit altitude units to use
	 * @return total climb
	 */
	public int getClimb(Unit inUnit)
	{
		return (int) (_climb * inUnit.getMultFactorFromStd());
	}

	/**
	 * @param inUnit altitude units to use
	 * @return total descent
	 */
	public int getDescent(Unit inUnit)
	{
		return (int) (_descent * inUnit.getMultFactorFromStd());
	}

	/**
	 * @return overall height gain in metres
	 */
	public double getMetricHeightDiff()
	{
		return _climb - _descent;
	}
}
