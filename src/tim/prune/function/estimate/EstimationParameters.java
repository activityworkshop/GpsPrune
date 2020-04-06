package tim.prune.function.estimate;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.RangeStatsWithGradients;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;

/**
 * Class to hold, parse and convert the parameters for time estimation.
 * These are five (metric) values which can be loaded and saved from config,
 * and are used by the EstimateTime function
 */
public class EstimationParameters
{
	/** Minutes required for flat travel, fixed metric distance */
	private double _flatMins = 0.0;
	/** Minutes required for climbing, fixed metric distance */
	private double _gentleClimbMins = 0.0, _steepClimbMins;
	/** Minutes required for descending, fixed metric distance */
	private double _gentleDescentMins = 0.0, _steepDescentMins;
	/** True if parsing from a string failed */
	private boolean _parseFailed = false;

	/** Kilometres unit for comparison */
	private static final Unit KILOMETRES = UnitSetLibrary.UNITS_KILOMETRES;


	/**
	 * Bare constructor using default values
	 */
	public EstimationParameters()
	{
		resetToDefaults();
	}

	/**
	 * Constructor from config string
	 * @param inConfigString single, semicolon-separated string from config
	 */
	public EstimationParameters(String inConfigString)
	{
		populateWithString(inConfigString);
		if (_parseFailed) {
			resetToDefaults();
		}
	}

	/**
	 * Reset all the values to their hardcoded defaults
	 */
	public void resetToDefaults()
	{
		_flatMins = 60.0;
		_gentleClimbMins = 12.0; _steepClimbMins = 18.0;
		_gentleDescentMins = 0.0; _steepDescentMins = 12.0;
		_parseFailed = false;
	}

	/**
	 * @return true if this set of parameters is the same as the default set
	 */
	public boolean sameAsDefaults()
	{
		EstimationParameters defaultParams = new EstimationParameters();
		return _flatMins == defaultParams._flatMins
			&& _gentleClimbMins == defaultParams._gentleClimbMins
			&& _steepClimbMins == defaultParams._steepClimbMins
			&& _gentleDescentMins == defaultParams._gentleDescentMins
			&& _steepDescentMins  == defaultParams._steepDescentMins;
	}

	/**
	 * Populate the values from the config, which means all values are metric
	 * @param inString semicolon-separated string of five parameters
	 */
	private void populateWithString(String inString)
	{
		if (inString != null && !inString.trim().equals(""))
		{
			String[] params = inString.trim().split(";");
			_parseFailed = (params == null || params.length != 5);
			if (!_parseFailed)
			{
				for (String p : params)
				{
					if (!isParamStringValid(p)) {
						_parseFailed = true;
					}
				}
			}
			if (!_parseFailed)
			{
				try
				{
					// Use fixed UK locale to parse these, because of fixed "." formatting
					NumberFormat twoDpFormatter = NumberFormat.getNumberInstance(Locale.UK);
					_flatMins          = twoDpFormatter.parse(params[0]).doubleValue();
					_gentleClimbMins   = twoDpFormatter.parse(params[1]).doubleValue();
					_steepClimbMins    = twoDpFormatter.parse(params[2]).doubleValue();
					_gentleDescentMins = twoDpFormatter.parse(params[3]).doubleValue();
					_steepDescentMins  = twoDpFormatter.parse(params[4]).doubleValue();
				}
				catch (Exception e) {
					_parseFailed = true;
				}
			}
		}
		else _parseFailed = true;
	}

	/**
	 * Populate the values using five user-entered strings (now Units-specific!)
	 * @param inFlat minutes for flat
	 * @param inGClimb minutes for gentle climb
	 * @param inSClimb minutes for steep climb
	 * @param inGDescent minutes for gentle descent
	 * @param inSDescent minutes for steep descent
	 */
	public void populateWithStrings(String inFlat, String inGClimb, String inSClimb, String inGDescent, String inSDescent)
	{
		if (isParamStringValid(inFlat) && isParamStringValid(inGClimb) && isParamStringValid(inSClimb)
			&& isParamStringValid(inGDescent) && isParamStringValid(inSDescent))
		{
			Unit distUnit = Config.getUnitSet().getDistanceUnit();
			Unit altUnit  = Config.getUnitSet().getAltitudeUnit();
			final double distFactor = (distUnit == KILOMETRES ? 1.0 : (5000/3.0 * distUnit.getMultFactorFromStd()));
			final double altFactor  = (altUnit.isStandard()  ? 1.0 : (1.0/3.0 * altUnit.getMultFactorFromStd()));
			NumberFormat localFormatter = NumberFormat.getNumberInstance();
			try
			{
				_flatMins = localFormatter.parse(inFlat).doubleValue() * distFactor;
				_gentleClimbMins = localFormatter.parse(inGClimb).doubleValue() * altFactor;
				_steepClimbMins  = localFormatter.parse(inSClimb).doubleValue() * altFactor;
				_gentleDescentMins = localFormatter.parse(inGDescent).doubleValue() * altFactor;
				_steepDescentMins  = localFormatter.parse(inSDescent).doubleValue() * altFactor;
			}
			catch (Exception e) {_parseFailed = true;}
		}
		else _parseFailed = true;
	}

	/**
	 * Populate with double metric values, for example the results of a Learning process
	 * @param inFlat time for 5km on the flat
	 * @param inGClimb time for 100m gentle climb
	 * @param inSClimb time for 100m steep climb
	 * @param inGDescent time for 100m gentle descent
	 * @param inSDescent time for 100m steep descent
	 */
	public void populateWithMetrics(double inFlat, double inGClimb, double inSClimb, double inGDescent, double inSDescent)
	{
		_flatMins = inFlat;
		_gentleClimbMins = inGClimb;
		_steepClimbMins  = inSClimb;
		_gentleDescentMins = inGDescent;
		_steepDescentMins  = inSDescent;
	}

	/**
	 * @param inString parameter string to check
	 * @return true if it looks valid (no letters, at least one digit)
	 */
	private static boolean isParamStringValid(String inString)
	{
		if (inString == null) {return false;}
		boolean hasDigit = false, currPunc = false, prevPunc = false;
		for (int i=0; i<inString.length(); i++)
		{
			char c = inString.charAt(i);
			if (Character.isLetter(c)) {return false;} // no letters allowed
			currPunc = (c == '.' || c == ',');
			if (currPunc && prevPunc) {return false;} // no consecutive . or , allowed
			prevPunc = currPunc;
			hasDigit = hasDigit || Character.isDigit(c);
		}
		return hasDigit; // must have at least one digit!
	}

	/**
	 * @return true if the parameters are valid, with no parsing errors
	 */
	public boolean isValid()
	{
		return !_parseFailed; // && _flatMins > 0.0 && _gentleClimbMins >= 0.0 && _steepClimbMins >= 0.0;
	}

	/**
	 * @return five strings for putting in text fields for editing / display
	 */
	public String[] getStrings()
	{
		Unit distUnit = Config.getUnitSet().getDistanceUnit();
		Unit altUnit  = Config.getUnitSet().getAltitudeUnit();
		double distFactor = (distUnit == KILOMETRES ? 1.0 : (5000/3.0 * distUnit.getMultFactorFromStd()));
		double altFactor  = (altUnit.isStandard()  ? 1.0 : (1.0/3.0 * altUnit.getMultFactorFromStd()));
		// Use locale-specific number formatting, eg commas for german
		NumberFormat numFormatter = NumberFormat.getNumberInstance();
		if (numFormatter instanceof DecimalFormat) {
			((DecimalFormat) numFormatter).applyPattern("0.00");
		}
		// Conversion between units
		return new String[] {
			numFormatter.format(_flatMins / distFactor),
			numFormatter.format(_gentleClimbMins / altFactor), numFormatter.format(_steepClimbMins / altFactor),
			numFormatter.format(_gentleDescentMins / altFactor), numFormatter.format(_steepDescentMins / altFactor)
		};
	}

	/**
	 * @return unit-specific string describing the distance for the flat time (5km/3mi/3NM)
	 */
	public static String getStandardDistance()
	{
		Unit distUnit = Config.getUnitSet().getDistanceUnit();
		return (distUnit == KILOMETRES ? "5 " : "3 ") + I18nManager.getText(distUnit.getShortnameKey());
	}

	/**
	 * @return unit-specific string describing the height difference for the climbs/descents (100m/300ft)
	 */
	public static String getStandardClimb()
	{
		Unit altUnit  = Config.getUnitSet().getAltitudeUnit();
		return (altUnit.isStandard() ? "100 " : "300 ") + I18nManager.getText(altUnit.getShortnameKey());
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
	private static String twoDp(double inNum)
	{
		if (inNum < 0.0) return "-" + twoDp(-inNum);
		int hundreds = (int) (inNum * 100 + 0.5);
		return "" + (hundreds / 100) + "." + (hundreds % 100);
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
		if (inFraction < 0.0 || inFraction > 1.0 || inOther == null) {
			return null;
		}
		// inFraction is the weight of this one, weight of the other one is 1-inFraction
		final double fraction2 = 1 - inFraction;
		EstimationParameters combined = new EstimationParameters();
		combined._flatMins = inFraction * _flatMins + fraction2 * inOther._flatMins;
		combined._gentleClimbMins = inFraction * _gentleClimbMins + fraction2 * inOther._gentleClimbMins;
		combined._gentleDescentMins = inFraction * _gentleDescentMins + fraction2 * inOther._gentleDescentMins;
		combined._steepClimbMins = inFraction * _steepClimbMins + fraction2 * inOther._steepClimbMins;
		combined._steepDescentMins = inFraction * _steepDescentMins + fraction2 * inOther._steepDescentMins;
		return combined;
	}
}
