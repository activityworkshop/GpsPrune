package tim.prune.data;

/**
 * Class to hold a set of units for distance, altitude and speed
 */
public class UnitSet
{
	private final String _nameKey;
	private final Unit _distanceUnit;
	private final Unit _speedUnit;
	private final Unit _altitudeUnit;
	private final Unit _vertSpeedUnit;

	/**
	 * Constructor
	 * @param inNameKey name key
	 * @param inDistanceUnit distance unit
	 * @param inAltitudeUnit altitude unit
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

	public boolean isMetric() {
		return _distanceUnit == UnitSetLibrary.UNITS_KILOMETRES
				|| _distanceUnit == UnitSetLibrary.UNITS_METRES;
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
