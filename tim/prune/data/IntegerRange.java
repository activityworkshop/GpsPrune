package tim.prune.data;

/**
 * Represents a range of integers, holding the maximum and
 * minimum values.  Values assumed to be >= 0.
 */
public class IntegerRange
{
	private int _min = -1, _max = -1;


	/**
	 * Clear for a new range calculation
	 */
	public void clear()
	{
		_min = -1;
		_max = -1;
	}


	/**
	 * Add a value to the range
	 * @param inValue value to add, only positive values considered
	 */
	public void addValue(int inValue)
	{
		if (inValue >= 0)
		{
			if (inValue < _min || _min < 0) _min = inValue;
			if (inValue > _max) _max = inValue;
		}
	}


	/**
	 * @return minimum value, or -1 if none found
	 */
	public int getMinimum()
	{
		return _min;
	}


	/**
	 * @return maximum value, or -1 if none found
	 */
	public int getMaximum()
	{
		return _max;
	}
}
