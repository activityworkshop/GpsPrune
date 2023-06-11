package tim.prune.function.edit;

/**
 * Class to hold a single point edit for a single Field, specifying the point
 * and the corresponding new value
 */
public class PointEdit
{
	private final int _pointIndex;
	private final String _value;

	/**
	 * Constructor
	 * @param inIndex point index
	 * @param inValue new value
	 */
	public PointEdit(int inIndex, String inValue)
	{
		_pointIndex = inIndex;
		_value = inValue;
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
}
