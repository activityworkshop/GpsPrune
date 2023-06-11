package tim.prune.data;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 * Caching accessor for the number formatters
 */
public abstract class CoordFormatters
{
	private static final HashMap<Integer, NumberFormat> _formatters = new HashMap<>();

	/**
	 * Get a formatter for the given number of decimal digits, creating and caching it if necessary
	 * @param inDigits number of digits after the decimal point
	 * @return number formatter
	 */
	public static NumberFormat getFormatter(int inDigits)
	{
		NumberFormat formatter = _formatters.get(inDigits);
		if (formatter == null)
		{
			// Formatter doesn't exist yet, so create a new one
			formatter = NumberFormat.getNumberInstance(Locale.UK);
			// Select the UK locale for this formatter so that decimal point is always used (not comma)
			if (formatter instanceof DecimalFormat)
			{
				// Now make a pattern with the right number of digits
				StringBuilder patternBuilder = new StringBuilder("0.");
				for (int i=0; i<inDigits; i++) {
					patternBuilder.append('0');
				}
				((DecimalFormat) formatter).applyPattern(patternBuilder.toString());
			}
			// Store in map
			_formatters.put(inDigits, formatter);
		}
		return formatter;
	}
}
