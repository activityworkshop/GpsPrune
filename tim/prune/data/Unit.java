package tim.prune.data;

/**
 * Class to represent a single distance or speed unit
 * such as kilometres, mph, feet etc
 */
public class Unit
{
	private String _nameKey = null;
	private double _multFactorFromStd = 1.0;
	private boolean _isStandard = false;

	/**
	 * Unit constructor
	 * @param inNameKey name key
	 * @param inMultFactor multiplication factor from standard units
	 */
	public Unit(String inNameKey, double inMultFactor)
	{
		_nameKey = inNameKey;
		_multFactorFromStd = inMultFactor;
		_isStandard = false;
	}

	/**
	 * Unit constructor for standard unit
	 * @param inNameKey name key
	 */
	public Unit(String inNameKey)
	{
		_nameKey = inNameKey;
		_multFactorFromStd = 1.0;
		_isStandard = true;
	}

	/**
	 * Unit constructor
	 * @param inParent parent unit
	 * @param inSuffix suffix to name key
	 */
	public Unit(Unit inParent, String inSuffix)
	{
		this(inParent, inSuffix, 1.0);
	}

	/**
	 * Unit constructor
	 * @param inParent parent unit
	 * @param inSuffix suffix to name key
	 * @param inFactor additional time factor to apply
	 */
	public Unit(Unit inParent, String inSuffix, double inFactor)
	{
		_nameKey = inParent._nameKey + inSuffix;
		_multFactorFromStd = inParent._multFactorFromStd * inFactor;
		_isStandard = inParent._isStandard;
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "units." + _nameKey;
	}

	/**
	 * @return shortname key
	 */
	public String getShortnameKey() {
		return getNameKey() + ".short";
	}

	/**
	 * @return multiplication factor from standard units
	 */
	public double getMultFactorFromStd() {
		return _multFactorFromStd;
	}

	/**
	 * @return true if this is the standard unit (mult factor 1.0)
	 */
	public boolean isStandard() {
		return _isStandard;
	}
}
