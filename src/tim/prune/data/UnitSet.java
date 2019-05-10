package tim.prune.data;

/**
 * Class to hold a set of units for distance, altitude and speed
 */
public class UnitSet
{
	private String _nameKey = null;
	private Unit _distanceUnit = null;
	private Unit _speedUnit = null;
	private Unit _altitudeUnit = null;
	private Unit _vertSpeedUnit = null;

	/**
	 * Constructor
	 * @param inNameKey name key
	 * @param inDistanceUnit distance unit
	 * @param inAltitudeUnit altitude unit
	 * @param inAltitudeFormat default altitude format
	 * @param inSpeedUnit unit for horizontal speeds
	 * @param inVerticalSpeedUnit unit for vertical speeds
	 */
	public UnitSet(String inNameKey, Unit inDistanceUnit,
		Unit inAltitudeUnit, Unit inSpeedUnit, Unit inVerticalSpeedUnit)
	{
		_nameKey = inNameKey;
		_distanceUnit = inDistanceUnit;
		_altitudeUnit = inAltitudeUnit;
		_speedUnit = inSpeedUnit;
		_vertSpeedUnit = inVerticalSpeedUnit;
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return _nameKey;
	}

	/**
	 * @return distance unit
	 */
	public Unit getDistanceUnit() {
		return _distanceUnit;
	}

	/**
	 * @return speed unit
	 */
	public Unit getSpeedUnit() {
		return _speedUnit;
	}

	/**
	 * @return altitude unit
	 */
	public Unit getAltitudeUnit() {
		return _altitudeUnit;
	}

	/**
	 * @return vertical speed unit
	 */
	public Unit getVerticalSpeedUnit() {
		return _vertSpeedUnit;
	}

	/**
	 * @return default point creation options for this unit set
	 */
	public PointCreateOptions getDefaultOptions()
	{
		PointCreateOptions options = new PointCreateOptions();
		options.setAltitudeUnits(getAltitudeUnit());
		options.setSpeedUnits(getSpeedUnit());
		options.setVerticalSpeedUnits(getVerticalSpeedUnit(), true);
		return options;
	}
}
