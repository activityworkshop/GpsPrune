package tim.prune.data;

/**
 * Represents a range of altitudes, taking units into account.
 * Values assumed to be >= 0.
 */
public class AltitudeRange
{
	private IntegerRange _range = new IntegerRange();
	private int _format = Altitude.FORMAT_NONE;


	/**
	 * Add a value to the range
	 * @param inValue value to add, only positive values considered
	 */
	public void addValue(Altitude inAltitude)
	{
		if (inAltitude != null)
		{
			int altValue = inAltitude.getValue(_format);
			_range.addValue(altValue);
			if (_format == Altitude.FORMAT_NONE)
			{
				_format = inAltitude.getFormat();
			}
		}
	}


	/**
	 * @return true if positive data values were found
	 */
	public boolean hasData()
	{
		return (_range.hasData());
	}


	/**
	 * @return minimum value, or -1 if none found
	 */
	public int getMinimum()
	{
		return _range.getMinimum();
	}


	/**
	 * @return maximum value, or -1 if none found
	 */
	public int getMaximum()
	{
		return _range.getMaximum();
	}


	/**
	 * @return the altitude format used
	 */
	public int getFormat()
	{
		return _format;
	}
}
