package tim.prune.data;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Abstract class to offer general number manipulation functions
 */
public abstract class NumberUtils
{
	/** Locale-specific number formatter */
	private static final NumberFormat LOCAL_FORMAT = NumberFormat.getNumberInstance();

	/** UK-specific number formatter object to avoid lots of instantiations */
	private static final NumberFormat UK_FORMAT = NumberFormat.getNumberInstance(Locale.UK);
	// Select the UK locale for this formatter so that decimal point is always used (not comma)
	static {
		if (UK_FORMAT instanceof DecimalFormat) ((DecimalFormat) UK_FORMAT).applyPattern("0.000");
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
		UK_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
		return UK_FORMAT.format(inNumber);
	}

	/**
	 * Format the given number in the local format to the given number of decimal places
	 * @param inNumber double number to format
	 * @param inDecimalPlaces number of decimal places
	 */
	public static String formatNumberLocal(double inNumber, int inDecimalPlaces)
	{
		final int numDecimals = (inDecimalPlaces < 0 ? 3 : inDecimalPlaces);
		LOCAL_FORMAT.setMaximumFractionDigits(numDecimals);
		LOCAL_FORMAT.setMinimumFractionDigits(numDecimals);
		LOCAL_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
		return LOCAL_FORMAT.format(inNumber);
	}

	/**
	 * @return the number of decimal places in the given string
	 * or -1 if the string doesn't represent a number
	 */
	public static int numberOfDecimalPlaces(String inString)
	{
		String value = (inString == null ? "" : inString.trim());
		int cPos = value.length() - 1;
		int digits = 0;
		boolean hasAnyDigits = false;
		while (cPos >= 0)
		{
			char c = value.charAt(cPos);
			if (!Character.isDigit(c))
			{
				if (c == '.' || c == ',') {
					return (hasAnyDigits || digits == 0) ? digits : -1;
				}
				return hasAnyDigits ? 0 : -1;
			}
			hasAnyDigits = true;
			cPos--;
			digits++;
		}
		return hasAnyDigits ? 0 : -1;
	}

	/**
	 * Format the given number in the local format to match the number of decimal places
	 * @param inNumber double number to format
	 * @param inPattern example string showing decimals
	 */
	public static String formatNumberLocalToMatch(double inNumber, String inPattern) {
		return formatNumberLocal(inNumber, numberOfDecimalPlaces(inPattern));
	}

	/**
	 * @param inValue string value, can be null or empty
	 * @return value extracted using either java formatting or locale formatting
	 */
	static Double parseDoubleUsingLocale(String inValue)
	{
		final String trimmedValue = (inValue == null ? "" : inValue.trim());
		if (trimmedValue.isEmpty()) {
			return null;
		}
		// We have some contents, so try parsing using the regular java formatting (decimal dot)
		try {
			return Double.parseDouble(trimmedValue);
		}
		catch (NumberFormatException ignored) {
		}

		// That threw an exception, so try with the system's locale instead (for example, using decimal comma)
		if (isLocalDecimal(inValue))
		{
			try
			{
				NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
				return nf.parse(trimmedValue).doubleValue();
			}
			catch (ParseException ignored) {
			}
		}
		// Neither worked
		return null;
	}

	/** @return true if the given string looks like a local decimal value */
	private static boolean isLocalDecimal(String inValue)
	{
		if (inValue == null || inValue.isEmpty()) {
			return false;
		}
		String allowedChars = "-0123456789";
		NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
		if (nf instanceof DecimalFormat) {
			allowedChars += ((DecimalFormat) nf).getDecimalFormatSymbols().getDecimalSeparator();
		}
		for (int i=0; i<inValue.length(); i++)
		{
			if (allowedChars.indexOf(inValue.charAt(i)) < 0) {
				return false; // another character found
			}
		}
		// only contains allowed characters
		return true;
	}

	/** @return String to use between numbers, depending on the decimal character */
	public static String getValueSeparator()
	{
		NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
		if (nf instanceof DecimalFormat)
		{
			char decimalChar = ((DecimalFormat) nf).getDecimalFormatSymbols().getDecimalSeparator();
			if (decimalChar == ',') {
				return "; ";
			}
		}
		return ", ";
	}

	public static int getIntOrZero(String inString)
	{
		try {
			return Integer.parseInt(inString);
		}
		catch (NumberFormatException ignored) {
			return 0;
		}
	}

	public static double getDoubleOrZero(String inString)
	{
		try {
			return Double.parseDouble(inString);
		}
		catch (NumberFormatException | NullPointerException ignored) {
			return 0.0;
		}
	}
}
