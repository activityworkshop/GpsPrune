package tim.prune.function.estimate;

import java.text.NumberFormat;
import java.util.Locale;

import tim.prune.I18nManager;
import tim.prune.data.RangeStatsWithGradients;
import tim.prune.data.Unit;
import tim.prune.data.UnitSet;
import tim.prune.data.UnitSetLibrary;

/**
 * Class to hold, parse and convert the parameters for time estimation.
 * These are five (metric) values which can be loaded and saved from config,
 * and are used by the EstimateTime function
 */
public class EstimationParameters
{
	/** Minutes required for flat travel, fixed metric distance */
	private final double _flatMins;
	/** Minutes required for climbing, fixed metric distance */
	private final double _gentleClimbMins;
	private final double _steepClimbMins;
	/** Minutes required for descending, fixed metric distance */
	private final double _gentleDescentMins;
	private final double _steepDescentMins;

	/** Kilometres unit for comparison */
	private static final Unit KILOMETRES = UnitSetLibrary.UNITS_KILOMETRES;

	/** Default parameters */
	public static final EstimationParameters DEFAULT_PARAMS = new EstimationParameters(60.0, 12.0, 18.0, 0.0, 12.0);


	/**
	 * Populate with double metric values, for example the results of a Learning process
	 * @param inFlat time for 5km on the flat
	 * @param inGClimb time for 100m gentle climb
	 * @param inSClimb time for 100m steep climb
	 * @param inGDescent time for 100m gentle descent
	 * @param inSDescent time for 100m steep descent
	 */
	private EstimationParameters(double inFlat,
		double inGClimb, double inSClimb, double inGDescent, double inSDescent)
	{
		_flatMins = inFlat;
		_gentleClimbMins = inGClimb;
		_steepClimbMins  = inSClimb;
		_gentleDescentMins = inGDescent;
		_steepDescentMins  = inSDescent;
	}

	/**
	 * Constructor from config string
	 * @param inString single, semicolon-separated string from config with five params
	 * @return parameters object, or null if parsing failed
	 */
	public static EstimationParameters fromConfigString(String inString)
	{
		if (inString == null || inString.equals("")) {
			return null;
		}
		String[] params = inString.trim().split(";");
		if (params == null || params.length != 5) {
			return null;
		}
		try
		{
			// Use fixed UK locale to parse these, because of fixed "." formatting
			NumberFormat twoDpFormatter = NumberFormat.getNumberInstance(Locale.UK);
			final double flatMins          = twoDpFormatter.parse(params[0]).doubleValue();
			final double gentleClimbMins   = twoDpFormatter.parse(params[1]).doubleValue();
			final double steepClimbMins    = twoDpFormatter.parse(params[2]).doubleValue();
			final double gentleDescentMins = twoDpFormatter.parse(params[3]).doubleValue();
			final double steepDescentMins  = twoDpFormatter.parse(params[4]).doubleValue();
			return new EstimationParameters(flatMins, gentleClimbMins, steepClimbMins, gentleDescentMins, steepDescentMins);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Populate the values using five user-entered values (now Units-specific!)
	 * @param inFlat minutes for flat
	 * @param inGClimb minutes for gentle climb
	 * @param inSClimb minutes for steep climb
	 * @param inGDescent minutes for gentle descent
	 * @param inSDescent minutes for steep descent
	 */
	public static EstimationParameters fromLocalUnits(UnitSet inUnitSet, double inFlat,
		double inGClimb, double inSClimb, double inGDescent, double inSDescent)
	{
		Unit distUnit = inUnitSet.getDistanceUnit();
		Unit altUnit  = inUnitSet.getAltitudeUnit();
		final double distFactor = (distUnit == KILOMETRES ? 1.0 : (5000/3.0 * distUnit.getMultFactorFromStd()));
		final double altFactor  = (altUnit.isStandard()  ? 1.0 : (1.0/3.0 * altUnit.getMultFactorFromStd()));

		double flatMins = inFlat * distFactor;
		double gentleClimbMins = inGClimb * altFactor;
		double steepClimbMins  = inSClimb * altFactor;
		double gentleDescentMins = inGDescent * altFactor;
		double steepDescentMins  = inSDescent * altFactor;
		return new EstimationParameters(flatMins, gentleClimbMins, steepClimbMins,
				gentleDescentMins, steepDescentMins);
	}

	/**
	 * Populate the values using five calculated values (metric)
	 * @param inFlat minutes for flat
	 * @param inGClimb minutes for gentle climb
	 * @param inSClimb minutes for steep climb
	 * @param inGDescent minutes for gentle descent
	 * @param inSDescent minutes for steep descent
	 */
	public static EstimationParameters fromMetricUnits(double inFlat,
		double inGClimb, double inSClimb, double inGDescent, double inSDescent)
	{
		return new EstimationParameters(inFlat, inGClimb, inSClimb, inGDescent, inSDescent);
	}

	/**
	 * @return true if this set of parameters is the same as the default set
	 */
	public boolean sameAsDefaults() {
		return toConfigString().equals(DEFAULT_PARAMS.toConfigString());
	}


	/** @return number of minutes for flat travel, local distance units */
	public double getFlatMinutesLocal(Unit inDistUnit) {
		return convertDistanceToLocal(_flatMins, inDistUnit);
	}

	/** @return the given number of minutes converted to the given distance units */
	private static double convertDistanceToLocal(double minutes, Unit inDistUnit)
	{
		double distFactor = (inDistUnit == KILOMETRES ? 1.0 : (5000/3.0 * inDistUnit.getMultFactorFromStd()));
		return minutes / distFactor;
	}

	/** @return number of minutes for gentle climb, local distance units */
	public double getGentleClimbMinutesLocal(Unit inAltUnit) {
		return convertAltitudeToLocal(_gentleClimbMins, inAltUnit);
	}

	/** @return number of minutes for steep climb, local distance units */
	public double getSteepClimbMinutesLocal(Unit inAltUnit) {
		return convertAltitudeToLocal(_steepClimbMins, inAltUnit);
	}

	/** @return number of minutes for gentle descent, local distance units */
	public double getGentleDescentMinutesLocal(Unit inAltUnit) {
		return convertAltitudeToLocal(_gentleDescentMins, inAltUnit);
	}

	/** @return number of minutes for steep descent, local distance units */
	public double getSteepDescentMinutesLocal(Unit inAltUnit) {
		return convertAltitudeToLocal(_steepDescentMins, inAltUnit);
	}

	/** @return the given number of minutes converted to local altitude units */
	private static double convertAltitudeToLocal(double minutes, Unit inAltUnit)
	{
		double altFactor  = (inAltUnit.isStandard() ? 1.0 : (1.0/3.0 * inAltUnit.getMultFactorFromStd()));
		return minutes / altFactor;
	}

	/**
	 * @return unit-specific string describing the distance for the flat time (5km/3mi/3NM)
	 */
	public static String getStandardDistance(Unit inDistUnit)
	{
		return (inDistUnit == KILOMETRES ? "5 " : "3 ") + I18nManager.getText(inDistUnit.getShortnameKey());
	}

	/**
	 * @return unit-specific string describing the height difference for the climbs/descents (100m/300ft)
	 */
	public static String getStandardClimb(Unit inAltUnit)
	{
		return (inAltUnit.isStandard() ? "100 " : "300 ") + I18nManager.getText(inAltUnit.getShortnameKey());
	}

	/**
	 * @return contents of parameters as a semicolon-separated (metric) string for the config
	 */
	public String toConfigString()
	{
		return "" + twoDp(_flatMins) + ";" + twoDp(_gentleClimbMins) + ";" + twoDp(_steepClimbMins) + ";"
			+ twoDp(_gentleDescentMins) + ";" + twoDp(_steepDescentMins);
	}

	/**
	 * @param inNum number to output
	 * @return formatted string to two decimal places, with decimal point
	 */
	public static String twoDp(double inNum)
	{
		if (inNum < 0.0) {
			return "-" + twoDp(-inNum);
		}
		int hundreds = (int) (inNum * 100 + 0.5);
		return "" + (hundreds / 100) + "." + ((hundreds/10) % 10) + (hundreds % 10);
	}

	/**
	 * Apply the parameters to the given range stats
	 * @param inStats stats of current range
	 * @return estimated number of minutes required
	 */
	public double applyToStats(RangeStatsWithGradients inStats)
	{
		if (inStats == null) {
			return 0.0;
		}
		final Unit METRES = UnitSetLibrary.UNITS_METRES;
		final double STANDARD_CLIMB = 100.0; // metres
		final double STANDARD_DISTANCE = 5.0; // kilometres
		return _flatMins * inStats.getMovingDistanceKilometres() / STANDARD_DISTANCE
			+ _gentleClimbMins * inStats.getGentleAltitudeRange().getClimb(METRES) / STANDARD_CLIMB
			+ _steepClimbMins  * inStats.getSteepAltitudeRange().getClimb(METRES) / STANDARD_CLIMB
			+ _gentleDescentMins * inStats.getGentleAltitudeRange().getDescent(METRES) / STANDARD_CLIMB
			+ _steepDescentMins  * inStats.getSteepAltitudeRange().getDescent(METRES) / STANDARD_CLIMB;
	}

	/**
	 * Combine two sets of parameters together
	 * @param inOther other set
	 * @param inFraction fractional weight
	 * @return combined set
	 */
	public EstimationParameters combine(EstimationParameters inOther, double inFraction)
	{
		if (inFraction >= 1.0 || inOther == null) {
			return this;
		}
		if (inFraction <= 0.0) {
			return inOther;
		}
		// inFraction is the weight of this one, weight of the other one is 1-inFraction
		final double fraction2 = 1 - inFraction;
		double flatMins = inFraction * _flatMins + fraction2 * inOther._flatMins;
		double gentleClimbMins = inFraction * _gentleClimbMins + fraction2 * inOther._gentleClimbMins;
		double gentleDescentMins = inFraction * _gentleDescentMins + fraction2 * inOther._gentleDescentMins;
		double steepClimbMins = inFraction * _steepClimbMins + fraction2 * inOther._steepClimbMins;
		double steepDescentMins = inFraction * _steepDescentMins + fraction2 * inOther._steepDescentMins;
		return new EstimationParameters(flatMins, gentleClimbMins, steepClimbMins, gentleDescentMins, steepDescentMins);
	}
}
