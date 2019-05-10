package tim.prune.data;

/**
 * Holder for a speed value, including a boolean valid flag
 */
public class SpeedValue
{
	/** Valid flag */
	private boolean _valid = false;
	/** Value as a double, using current units */
	private double  _value = 0.0;


	/**
	 * Set the flag to invalid
	 */
	public void setInvalid()
	{
		_valid = false;
		_value = 0.0;
	}

	/**
	 * @param inValue speed value to set
	 */
	public void setValue(double inValue)
	{
		_valid = true;
		_value = inValue;
	}

	/**
	 * @return true if value is valid
	 */
	public boolean isValid() {
		return _valid;
	}

	/**
	 * @return numeric value
	 */
	public double getValue() {
		return _value;
	}
}
