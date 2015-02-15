package tim.prune.data;

/**
 * List of all possible unit sets, for example
 * metric, imperial, nautical
 */
public abstract class UnitSetLibrary
{
	// Distance units - all conversion factors are from metres
	/** Units for feet (used for loading and converting values) */
	public static final Unit UNITS_FEET       = new Unit("feet", 3.2808);
	/** Units for metres */
	public static final Unit UNITS_METRES     = new Unit("metres");
	/** Units for km */
	public static final Unit UNITS_KILOMETRES = new Unit("kilometres", 1/1000.0);
	/** Units for miles */
	public static final Unit UNITS_MILES      = new Unit("miles", 1/1609.3);
	/** Units for nautical miles */
	public static final Unit UNITS_NAUTICAL_MILES = new Unit("nauticalmiles", 1/1852.0);

	/** Array of available unit sets */
	private static UnitSet[] _sets = {
		new UnitSet("unitset.kilometres", UNITS_KILOMETRES, UNITS_METRES, Altitude.Format.METRES),
		new UnitSet("unitset.miles", UNITS_MILES, UNITS_FEET, Altitude.Format.FEET),
		new UnitSet("unitset.nautical", UNITS_NAUTICAL_MILES, UNITS_FEET, Altitude.Format.FEET)
	};

	/**
	 * @return number of available unit sets
	 */
	public static int getNumUnitSets() {
		return _sets.length;
	}

	/**
	 * Get the specified unit set
	 * @param inIndex index of set starting from 0
	 * @return specified unit set or the default one if index out of range
	 */
	public static UnitSet getUnitSet(int inIndex)
	{
		if (inIndex >= 0 && inIndex < getNumUnitSets()) {
			return _sets[inIndex];
		}
		return _sets[0];
	}

	/**
	 * Get the unit set specified by the given key
	 * @param inKey key to look for
	 * @return unit set with given key, or default set if key not found
	 */
	public static UnitSet getUnitSet(String inKey)
	{
		// Loop over all available unit sets
		for (int i=0; i<getNumUnitSets(); i++)
		{
			UnitSet set = getUnitSet(i);
			if (set.getNameKey().equals(inKey)) {
				return set;
			}
		}
		// Not found in list, so just return the first one
		return getUnitSet(0);
	}
}
