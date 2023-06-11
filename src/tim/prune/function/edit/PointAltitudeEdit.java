package tim.prune.function.edit;

import tim.prune.data.Unit;

/**
 * Class to hold a single point edit for altitude, specifying the point
 * and the corresponding new altitude value
 */
public class PointAltitudeEdit
{
	private final int _pointIndex;
	private final String _value;
	private final Unit _altitudeUnit;

	/**
	 * Constructor
	 * @param inIndex point index
	 * @param inValue new value
	 * @param inUnit new unit
	 */
	public PointAltitudeEdit(int inIndex, String inValue, Unit inUnit)
	{
		_pointIndex = inIndex;
		_value = inValue;
		_altitudeUnit = inUnit;
	}


	/**
	 * @return the point index
	 */
	public int getPointIndex() {
		return _pointIndex;
	}

	/**
	 * @return the field value
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * @return altitude unit
	 */
	public Unit getUnit() {
		return _altitudeUnit;
	}
}
