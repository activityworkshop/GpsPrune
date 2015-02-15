package tim.prune.data;

/**
 * Class to hold either a horizontal speed or a vertical speed
 * including the units
 */
public class Speed
{
	private double _value = 0.0;
	private Unit   _unit  = null;
	private boolean _valid = false;

	/**
	 * Constructor
	 * @param inValue value
	 * @param inUnit unit, such as m/s or km/h
	 */
	public Speed(double inValue, Unit inUnit)
	{
		_value = inValue;
		_unit  = inUnit;
		_valid = isValidUnit(inUnit);
	}

	/**
	 * Constructor
	 * @param inValue value as string
	 * @param inUnit unit, such as m/s or km/h
	 */
	public Speed(String inValue, Unit inUnit)
	{
		try {
			_value = Double.parseDouble(inValue);
			_unit  = inUnit;
			_valid = isValidUnit(inUnit);
		}
		catch (Exception e)
		{
			_valid = false;
		}
	}

	/**
	 * Check if the given unit is valid for a speed
	 * @param inUnit unit
	 * @return true if it's valid
	 */
	private static boolean isValidUnit(Unit inUnit)
	{
		return inUnit != null && (inUnit == UnitSetLibrary.SPEED_UNITS_METRESPERSEC
				|| inUnit == UnitSetLibrary.SPEED_UNITS_KMPERHOUR
				|| inUnit == UnitSetLibrary.SPEED_UNITS_FEETPERSEC
				|| inUnit == UnitSetLibrary.SPEED_UNITS_MILESPERHOUR
				|| inUnit == UnitSetLibrary.SPEED_UNITS_KNOTS);
	}

	/**
	 * Invert the speed value, for example when vertical speeds are positive downwards
	 */
	public void invert() {
		if (_valid) _value = -_value;
	}

	/** @return the numerical value in whatever units they're in */
	public double getValue() {return _value;}

	/** @return the units they're in */
	public Unit getUnit() {return _unit;}

	/**
	 * @return speed value in metres per second
	 */
	public double getValueInMetresPerSec()
	{
		if (!_valid) return 0.0;
		return _value / _unit.getMultFactorFromStd();
	}

	/**
	 * @param inUnit specified speed units
	 * @return speed value in the specified units
	 */
	public double getValue(Unit inUnit)
	{
		if (!_valid || !isValidUnit(inUnit)) return 0.0;
		return getValueInMetresPerSec() * inUnit.getMultFactorFromStd();
	}

	/** @return true if this is valid */
	public boolean isValid() {return _valid;}

	/**
	 * Copy the values from the other object into this one, to make this one a clone
	 * @param inOther other speed object
	 */
	public void copyFrom(Speed inOther)
	{
		_value = inOther._value;
		_unit  = inOther._unit;
		_valid = inOther._valid;
	}
}
