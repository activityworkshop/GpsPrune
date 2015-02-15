package tim.prune.data;

/**
 * Class to hold the options when creating (or loading) a new point,
 * such as units for altitudes, speeds and vertical speeds
 */
public class PointCreateOptions
{
	private Unit _altitudeUnit  = UnitSetLibrary.UNITS_METRES;
	private Unit _speedUnit     = UnitSetLibrary.SPEED_UNITS_METRESPERSEC;
	private Unit _vertSpeedUnit = UnitSetLibrary.SPEED_UNITS_METRESPERSEC;
	private boolean _vertSpeedsUpwards = true;

	/**
	 * @param inUnit altitude units (only metres or feet accepted)
	 */
	public void setAltitudeUnits(Unit inUnit)
	{
		if (inUnit == UnitSetLibrary.UNITS_METRES || inUnit == UnitSetLibrary.UNITS_FEET) {
			_altitudeUnit = inUnit;
		}
	}

	/** @return altitude units */
	public Unit getAltitudeUnits() {return _altitudeUnit;}

	/**
	 * @param inUnit speed units (only m/s, ft/s, km/h and mph accepted)
	 */
	public void setSpeedUnits(Unit inUnit)
	{
		if (inUnit == UnitSetLibrary.SPEED_UNITS_METRESPERSEC
			|| inUnit == UnitSetLibrary.SPEED_UNITS_FEETPERSEC
			|| inUnit == UnitSetLibrary.SPEED_UNITS_KMPERHOUR
			|| inUnit == UnitSetLibrary.SPEED_UNITS_MILESPERHOUR)
		{
			_speedUnit = inUnit;
		}
	}

	/** @return speed units */
	public Unit getSpeedUnits() {return _speedUnit;}

	/**
	 * @param inUnit speed units (only m/s, ft/s, km/h and mph accepted)
	 * @param inUpwards true if positive speeds are upwards, negative downwards
	 */
	public void setVerticalSpeedUnits(Unit inUnit, boolean inUpwards)
	{
		if (inUnit == UnitSetLibrary.SPEED_UNITS_METRESPERSEC
			|| inUnit == UnitSetLibrary.SPEED_UNITS_FEETPERSEC
			|| inUnit == UnitSetLibrary.SPEED_UNITS_KMPERHOUR
			|| inUnit == UnitSetLibrary.SPEED_UNITS_MILESPERHOUR)
		{
			_vertSpeedUnit = inUnit;
			_vertSpeedsUpwards = inUpwards;
		}
	}

	/** @return vertical speed units */
	public Unit getVerticalSpeedUnits() {return _vertSpeedUnit;}

	/** @return true if positive speeds are upwards, negative downwards */
	public boolean getVerticalSpeedsUpwards() {return _vertSpeedsUpwards;}

	/** for debug */
	public String toString()
	{
		return "options: altitude " + _altitudeUnit.getNameKey() + ", speed " + _speedUnit.getNameKey() +
			", vspeed " + _vertSpeedUnit.getNameKey() + (_vertSpeedsUpwards ? " (upwards)" : " (downwards)");
	}
}
