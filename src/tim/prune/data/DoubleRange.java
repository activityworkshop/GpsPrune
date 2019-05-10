package tim.prune.data;

/**
 * Represents a range of doubles, holding the maximum and
 * minimum values.  Values can be positive or negative
 */
public class DoubleRange
{
	private boolean _empty = true;
	private double _min = 0.0, _max = 0.0;


	/** Empty constructor, cleared to zeroes */
	public DoubleRange() {}

	/**
	 * Constructor giving two initial values
	 * @param inValue1 first value
	 * @param inValue2 second value
	 */
	public DoubleRange(double inValue1, double inValue2)
	{
		addValue(inValue1);
		addValue(inValue2);
	}

	/**
	 * Clear for a new calculation
	 */
	public void clear()
	{
		_min = _max = 0.0;
		_empty = true;
	}


	/**
	 * Add a value to the range
	 * @param inValue value to add
	 */
	public void addValue(double inValue)
	{
		if (inValue < _min || _empty) _min = inValue;
		if (inValue > _max || _empty) _max = inValue;
		_empty = false;
	}

	/**
	 * Combine this range with another one
	 * @param inOtherRange other range to add to this one
	 */
	public void combine(DoubleRange inOtherRange)
	{
		if (inOtherRange != null && inOtherRange.getRange() > 1.0)
		{
			addValue(inOtherRange.getMinimum());
			addValue(inOtherRange.getMaximum());
		}
	}

	/**
	 * @return true if data values were found
	 */
	public boolean hasData()
	{
		return (!_empty);
	}


	/**
	 * @return minimum value, or 0.0 if none found
	 */
	public double getMinimum()
	{
		return _min;
	}


	/**
	 * @return maximum value, or 0.0 if none found
	 */
	public double getMaximum()
	{
		return _max;
	}

	/**
	 * @return range, as maximum - minimum
	 */
	public double getRange()
	{
		return _max - _min;
	}

	/**
	 * @return mid value, halfway between min and max
	 */
	public double getMidValue()
	{
		return (_max + _min) / 2.0;
	}

	/**
	 * Copy this range into a new object, which can then be modified without changing this one
	 * @return deep copy of this object
	 */
	public DoubleRange copy()
	{
		if (_empty) return new DoubleRange();
		return new DoubleRange(_min, _max);
	}
}
