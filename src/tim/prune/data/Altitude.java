package tim.prune.data;

/**
 * Class to hold an altitude and provide conversion functions
 */
public class Altitude
{
	private boolean _valid = false;
	private int _value = 0;
	private Unit _unit = null;
	private String _stringValue = null;

	/** Constant to use for a lack of altitude */
	public static final Altitude NONE = new Altitude(null, null);

	/**
	 * Constructor using String
	 * @param inString string to parse
	 * @param inUnit of altitude, either metres or feet
	 */
	public Altitude(String inString, Unit inUnit)
	{
		_unit = inUnit;
		if (inString != null && !inString.equals(""))
		{
			try
			{
				_stringValue = inString;
				_value = (int) Double.parseDouble(inString.trim());
				_valid = true;
			}
			catch (NumberFormatException nfe) {}
		}
	}


	/**
	 * Constructor with int value
	 * @param inValue int value of altitude
	 * @param inUnit unit of altitude, either metres or feet
	 */
	public Altitude(int inValue, Unit inUnit)
	{
		_value = inValue;
		_unit = inUnit;
		_valid = true;
		_stringValue = "" + inValue;
	}

	/**
	 * @return an exact copy of this Altitude object
	 */
	public Altitude clone()
	{
		return new Altitude(_stringValue, _unit);
	}

	/**
	 * Reset the altitude parameters to the same as the given object
	 * @param inClone clone object to copy
	 */
	public void reset(Altitude inClone)
	{
		_stringValue = inClone._stringValue;
		_value = inClone._value;
		_unit = inClone._unit;
		_valid = inClone._valid;
	}

	/**
	 * @return true if the value could be parsed
	 */
	public boolean isValid()
	{
		return _valid;
	}


	/**
	 * @return raw value as int
	 */
	public int getValue()
	{
		return _value;
	}

	/**
	 * @param inAltUnit altitude units to use
	 * @return rounded value in specified units
	 */
	public int getValue(Unit inAltUnit)
	{
		if (inAltUnit == null) {
			return getValue();
		}
		return (int) (getMetricValue() * inAltUnit.getMultFactorFromStd());
	}

	/**
	 * @return unit of number
	 */
	public Unit getUnit()
	{
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
		if (!_valid) {return "";}
		// Return string value if the same format or "no format" was requested
		if ((inUnit == _unit || inUnit == null)
		 && _stringValue != null && !_stringValue.equals("")) {
			return _stringValue;
		}
		return "" + getValue(inUnit);
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
		if (inStart == null || inEnd == null || !inStart.isValid() || !inEnd.isValid())
			return Altitude.NONE;
		// Use altitude format of first point
		Unit altUnit = inStart.getUnit();
		int startValue = inStart.getValue();
		int endValue = inEnd.getValue(altUnit);
		// interpolate between start and end
		int newValue = startValue + (int) ((endValue - startValue) * inFrac);
		return new Altitude(newValue, altUnit);
	}

	/**
	 * Add the given offset to the current altitude
	 * @param inOffset offset as double
	 * @param inUnit unit of offset, feet or metres
	 * @param inDecimals number of decimal places
	 */
	public void addOffset(double inOffset, Unit inUnit, int inDecimals)
	{
		// Use the maximum number of decimal places from current value and offset
		int numDecimals = NumberUtils.getDecimalPlaces(_stringValue);
		if (numDecimals < inDecimals) {numDecimals = inDecimals;}
		// Convert offset to correct units
		double offset = inOffset;
		if (inUnit != _unit && inUnit != null)
		{
			offset = inOffset / inUnit.getMultFactorFromStd() * _unit.getMultFactorFromStd();
		}
		// FIXME: The following will fail if _stringValue is null - not sure how it can get in that state!
		if (_stringValue == null) System.err.println("*** Altitude.addOffset - how did the string value get to be null?");
		// Add the offset
		double newValue = Double.parseDouble(_stringValue.trim()) + offset;
		_value = (int) newValue;
		_stringValue = NumberUtils.formatNumberUk(newValue, numDecimals);
	}
}
