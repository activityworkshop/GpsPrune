package tim.prune.data;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Abstract class to offer general number manipulation functions
 */
public abstract class NumberUtils
{
	/** UK-specific number formatter object to avoid lots of instantiations */
	private static final NumberFormat UK_FORMAT = NumberFormat.getNumberInstance(Locale.UK);
	// Select the UK locale for this formatter so that decimal point is always used (not comma)
	static {
		if (UK_FORMAT instanceof DecimalFormat) ((DecimalFormat) UK_FORMAT).applyPattern("0.000");
	}

	/**
	 * Find the number of decimal places represented in the String
	 * @param inString String to check
	 * @return number of decimal places, or 0 for integer value
	 */
	public static int getDecimalPlaces(String inString)
	{
		if (inString == null || inString.equals("")) {return 0;}
		int places = 0;
		final int sLen = inString.length();
		for (int i=sLen-1; i>=0; i--) {
			char c = inString.charAt(i);
			if (c >= '0' && c <= '9') {
				// Numeric character found
				places++;
			}
			else {
				// Non-numeric character found, return places
				return places;
			}
		}
		// No non-numeric characters found, so must be integer
		return 0;
	}

	/**
	 * Format the given number in UK format (decimal point) to the given number of decimal places
	 * @param inNumber double number to format
	 * @param inDecimalPlaces number of decimal places
	 */
	public static String formatNumberUk(double inNumber, int inDecimalPlaces)
	{
		UK_FORMAT.setMaximumFractionDigits(inDecimalPlaces);
		UK_FORMAT.setMinimumFractionDigits(inDecimalPlaces);
		return UK_FORMAT.format(inNumber);
	}
}