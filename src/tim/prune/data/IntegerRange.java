package tim.prune.data;

/**
 * Represents a range of integers, holding the maximum and
 * minimum values.
 */
public class IntegerRange
{
	private int _min = -1, _max = -1;
	private boolean _foundValues = false;


	/**
	 * Clear for a new range calculation
	 */
	public void clear()
	{
		_min = -1;
		_max = -1;
		_foundValues = false;
	}


	/**
	 * Add a value to the range
	 * @param inValue value to add
	 */
	public void addValue(int inValue)
	{
		if (inValue < _min || !_foundValues) {
			_min = inValue;
		}
		if (inValue > _max || !_foundValues) {
			_max = inValue;
		}
		_foundValues = true;
	}

	/**
	 * @return true if any values added to the range
	 */
	public boolean hasValues() {
		return _foundValues;
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
