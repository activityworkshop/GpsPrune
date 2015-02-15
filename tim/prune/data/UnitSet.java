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
	private Altitude.Format _defaultAltitudeFormat = Altitude.Format.METRES;

	/**
	 * Constructor
	 * @param inNameKey name key
	 * @param inDistanceUnit distance unit
	 * @param inAltitudeUnit altitude unit
	 * @param inAltitudeFormat default altitude format
	 */
	public UnitSet(String inNameKey, Unit inDistanceUnit,
		Unit inAltitudeUnit, Altitude.Format inAltitudeFormat)
	{
		_nameKey = inNameKey;
		_distanceUnit = inDistanceUnit;
		_speedUnit = new Unit(_distanceUnit, "perhour");
		_altitudeUnit = inAltitudeUnit;
		_defaultAltitudeFormat = inAltitudeFormat;
		_vertSpeedUnit = new Unit(_altitudeUnit, "persec");
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
	 * @return default altitude format
	 */
	public Altitude.Format getDefaultAltitudeFormat() {
		return _defaultAltitudeFormat;
	}
}
