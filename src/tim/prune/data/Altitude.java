package tim.prune.data;

/**
 * Class to hold an altitude and provide conversion functions
 */
public class Altitude
{
	private boolean _valid = false;
	private double _value = 0.0;
	private Unit _unit = null;
	private String _stringValue = null;

	/** Constant to use for a lack of altitude */
	public static final Altitude NONE = new Altitude(null, null);

	/**
	 * Constructor using String
	 * @param inString string to parse
	 * @param inUnit of altitude, either metres or feet
	 */
	public Altitude(String inString, Unit inUnit) {
		set(inString, inUnit);
	}


	/**
	 * Constructor with double value
	 * @param inValue double value of altitude
	 * @param inUnit unit of altitude, either metres or feet
	 */
	public Altitude(double inValue, Unit inUnit)
	{
		_value = inValue;
		_unit = inUnit;
		_valid = true;
		_stringValue = "" + inValue;
	}

	/**
	 * Constructor with another Altitude value
	 */
	public Altitude(Altitude inOther)
	{
		_value = inOther._value;
		_unit = inOther._unit;
		_valid = inOther._valid;
		_stringValue = inOther._stringValue;
	}

	/**
	 * Reset the altitude parameters to the same as the given object
	 * @param inValue value to set as string
	 * @param inUnit units
	 */
	public void set(String inValue, Unit inUnit)
	{
		_stringValue = inValue;
		_unit = inUnit;
		final String trimmedValue = (inValue == null ? "" : inValue.trim());
		final Double result = NumberUtils.parseDoubleUsingLocale(trimmedValue);
		_value = (result != null ? result : 0.0);
		_valid = (result != null);
	}

	/**
	 * @return true if the value could be parsed
	 */
	public boolean isValid() {
		return _valid;
	}

	/**
	 * @return raw value as double
	 */
	public double getValue() {
		return _value;
	}

	/**
	 * @param inAltUnit altitude units to use
	 * @return value in specified units (unrounded)
	 */
	public double getValue(Unit inAltUnit)
	{
		if (inAltUnit == null) {
			return getValue();
		}
		return getMetricValue() * inAltUnit.getMultFactorFromStd();
	}

	/**
	 * @param inAltUnit altitude units to use
	 * @return value in specified units (rounded to the nearest int)
	 */
	public int getIntValue(Unit inAltUnit) {
		return (int) Math.round(getValue(inAltUnit));
	}

	/**
	 * @return unit of number
	 */
	public Unit getUnit() {
		return _unit;
	}

	/**
	 * @return value of altitude in metres, used for calculations and charts
	 */
	public double getMetricValue()
	{
		if (_unit == UnitSetLibrary.UNITS_METRES || _unit == null) {
			return _value;
		}
		return _value / _unit.getMultFactorFromStd();
	}

	/**
	 * Get a string version of the value
	 * @param inUnit specified unit
	 * @return string value, if possible the original one
	 */
	public String getStringValue(Unit inUnit)
	{
		if (!_valid) {
			return "";
		}
		// Return string value if the same format or "no format" was requested
		if ((inUnit == _unit || inUnit == null)
			&& _stringValue != null && !_stringValue.equals(""))
		{
			return _stringValue;
		}
		return "" + getValue(inUnit);
	}

	/**
	 * Get a locally-formatted string version of the value
	 * @param inUnit specified unit
	 * @return string value using the local formatting
	 */
	public String getLocalStringValue(Unit inUnit)
	{
		if (!_valid) {
			return "";
		}
		return NumberUtils.formatNumberLocalToMatch(getValue(inUnit), _stringValue);
	}

	/**
	 * Interpolate a new Altitude object between the given ones
	 * @param inStart start altitude
	 * @param inEnd end altitude
	 * @param inIndex index of interpolated point
	 * @param inNumSteps number of steps to interpolate
	 * @return Interpolated Altitude object
	 */
	public static Altitude interpolate(Altitude inStart, Altitude inEnd, int inIndex, int inNumSteps)
	{
		return interpolate(inStart, inEnd, 1.0 * (inIndex + 1) / (inNumSteps + 1));
	}


	/**
	 * Interpolate a new Altitude object between the given ones
	 * @param inStart start altitude
	 * @param inEnd end altitude
	 * @param inFrac fraction of distance from first point
	 * @return Interpolated Altitude object
	 */
	public static Altitude interpolate(Altitude inStart, Altitude inEnd, double inFrac)
	{
		// Check if altitudes are valid
		if (inStart == null || inEnd == null || !inStart.isValid() || !inEnd.isValid()) {
			return Altitude.NONE;
		}
		// Use altitude format of first point
		Unit altUnit = inStart.getUnit();
		double startValue = inStart.getValue();
		double endValue = inEnd.getValue(altUnit);
		// interpolate between start and end
		double newValue = startValue + (endValue - startValue) * inFrac;
		return new Altitude(newValue, altUnit);
	}
}
