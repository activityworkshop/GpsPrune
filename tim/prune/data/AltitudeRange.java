package tim.prune.data;

/**
 * Represents a range of altitudes, taking units into account.
 * Values assumed to be >= 0.
 */
public class AltitudeRange
{
	private IntegerRange _range = new IntegerRange();
	private Altitude.Format _format = Altitude.Format.NO_FORMAT;


	/**
	 * Clear the altitude range
	 */
	public void clear()
	{
		_range.clear();
		_format = Altitude.Format.NO_FORMAT;
	}


	/**
	 * Add a value to the range
	 * @param inAltitude value to add, only positive values considered
	 */
	public void addValue(Altitude inAltitude)
	{
		if (inAltitude != null)
		{
			int altValue = inAltitude.getValue(_format);
			_range.addValue(altValue);
			if (_format == Altitude.Format.NO_FORMAT)
			{
				_format = inAltitude.getFormat();
			}
		}
	}


	/**
	 * @return true if altitude range found
	 */
	public boolean hasRange()
	{
		return _range.getMaximum() > _range.getMinimum();
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
	public Altitude.Format getFormat()
	{
		return _format;
	}
}
