package tim.prune.data;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 * Caching accessor for the number formatters
 */
public class CoordFormatters
{
	private final HashMap<Integer, NumberFormat> _localFormatters = new HashMap<>();
	private final HashMap<Integer, NumberFormat> _ukFormatters = new HashMap<>();


	/**
	 * Get a local formatter for the given number of decimal digits, creating and caching it if necessary
	 * @param inDigits number of digits after the decimal point
	 * @return number formatter
	 */
	public NumberFormat getLocalFormatter(int inDigits) {
		return getFormatter(_localFormatters, inDigits, null);
	}

	/**
	 * Get a UK formatter (using decimal dot) for the given number of decimal digits, creating and caching it if necessary
	 * @param inDigits number of digits after the decimal point
	 * @return number formatter
	 */
	public NumberFormat getUkFormatter(int inDigits) {
		// Select the UK locale for this formatter so that decimal point is always used (not comma)
		return getFormatter(_ukFormatters, inDigits, Locale.UK);
	}

	/**
	 * Get a formatter for the given number of decimal digits, creating and caching it if necessary
	 * @param inMap map of NumberFormat objects
	 * @param inDigits number of digits after the decimal point
	 * @param inLocale locale to use, or null for default
	 * @return number formatter
	 */
	private static NumberFormat getFormatter(HashMap<Integer, NumberFormat> inMap,
		int inDigits, Locale inLocale)
	{
		NumberFormat formatter = inMap.get(inDigits);
		if (formatter == null)
		{
			// Formatter doesn't exist yet, so create a new one
			formatter = (inLocale == null ? NumberFormat.getNumberInstance() : NumberFormat.getNumberInstance(inLocale));
			StringBuilder patternBuilder = new StringBuilder("0.");
			if (inDigits > 0) {
				patternBuilder.append("0".repeat(inDigits));
			}
			final String digitPattern = patternBuilder.toString();
			if (formatter instanceof DecimalFormat) ((DecimalFormat) formatter).applyPattern(digitPattern);
			// Store in map
			inMap.put(inDigits, formatter);
		}
		return formatter;
	}
}
