package tim.prune.load.xml;

/**
 * Class to hold a single tag value from a gpx file
 */
public class GpxTag
{
	/** value of tag */
	private String _value = null;

	/**
	 * @param inVal value to set
	 */
	public void setValue(String inVal) {
		_value = inVal;
	}

	/**
	 * @return value
	 */
	public String getValue() {
		return _value;
	}
}
