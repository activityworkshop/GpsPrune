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

	// Speed units - all conversion factors from metres per second
	public static final Unit SPEED_UNITS_METRESPERSEC = new Unit(UNITS_METRES, "persec");
	public static final Unit SPEED_UNITS_FEETPERSEC   = new Unit(UNITS_FEET, "persec");
	public static final Unit SPEED_UNITS_MILESPERHOUR = new Unit(UNITS_MILES, "perhour", 60.0 * 60.0);
	public static final Unit SPEED_UNITS_KNOTS        = new Unit(UNITS_NAUTICAL_MILES, "perhour", 60.0 * 60.0);
	public static final Unit SPEED_UNITS_KMPERHOUR    = new Unit(UNITS_KILOMETRES, "perhour", 60.0 * 60.0);
	public static final Unit[] ALL_SPEED_UNITS = {SPEED_UNITS_METRESPERSEC, SPEED_UNITS_KMPERHOUR,
		SPEED_UNITS_FEETPERSEC, SPEED_UNITS_MILESPERHOUR};

	/** Array of available unit sets */
	private static UnitSet[] _sets =
	{
		new UnitSet("unitset.kilometres", UNITS_KILOMETRES, UNITS_METRES, SPEED_UNITS_KMPERHOUR, SPEED_UNITS_METRESPERSEC),
		new UnitSet("unitset.miles", UNITS_MILES, UNITS_FEET, SPEED_UNITS_MILESPERHOUR, SPEED_UNITS_FEETPERSEC),
		new UnitSet("unitset.nautical", UNITS_NAUTICAL_MILES, UNITS_FEET, SPEED_UNITS_KNOTS, SPEED_UNITS_FEETPERSEC)
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
		for (UnitSet set : _sets)
		{
			if (set.getNameKey().equals(inKey)) {
				return set;
			}
		}
		// Not found in list, so just return the first one
		return getUnitSet(0);
	}
}
