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
