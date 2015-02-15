package tim.prune.gui;

import java.text.NumberFormat;

import tim.prune.I18nManager;

/**
 * Class to provide general display util methods
 */
public abstract class DisplayUtils
{
	/** Number formatter for one decimal place */
	private static final NumberFormat FORMAT_ONE_DP = NumberFormat.getNumberInstance();

	/** Static block to initialise the one d.p. formatter */
	static
	{
		FORMAT_ONE_DP.setMaximumFractionDigits(1);
		FORMAT_ONE_DP.setMinimumFractionDigits(1);
	}
	/** Flexible number formatter with different decimal places */
	private static final NumberFormat FORMAT_FLEX = NumberFormat.getNumberInstance();


	/**
	 * Build a String to describe a time duration
	 * @param inNumSecs number of seconds
	 * @return time as a string, days, hours, mins, secs as appropriate
	 */
	public static String buildDurationString(long inNumSecs)
	{
		if (inNumSecs <= 0L) return "";
		if (inNumSecs < 60L) return "" + inNumSecs + I18nManager.getText("display.range.time.secs");
		if (inNumSecs < 3600L) return "" + (inNumSecs / 60) + I18nManager.getText("display.range.time.mins")
			+ " " + (inNumSecs % 60) + I18nManager.getText("display.range.time.secs");
		if (inNumSecs < 86400L) return "" + (inNumSecs / 60 / 60) + I18nManager.getText("display.range.time.hours")
			+ " " + ((inNumSecs / 60) % 60) + I18nManager.getText("display.range.time.mins");
		if (inNumSecs < 432000L) return "" + (inNumSecs / 86400L) + I18nManager.getText("display.range.time.days")
			+ " " + (inNumSecs / 60 / 60) % 24 + I18nManager.getText("display.range.time.hours");
		if (inNumSecs < 86400000L) return "" + (inNumSecs / 86400L) + I18nManager.getText("display.range.time.days");
		return "big";
	}

	/**
	 * @param inNumber number to format
	 * @return formatted number to one decimal place
	 */
	public static String formatOneDp(double inNumber)
	{
		return FORMAT_ONE_DP.format(inNumber);
	}

	/**
	 * Format a number to a sensible precision
	 * @param inVal value to format
	 * @return formatted String using local format
	 */
	public static String roundedNumber(double inVal)
	{
		// Set precision of formatter
		int numDigits = 0;
		if (inVal < 1.0)
			numDigits = 3;
		else if (inVal < 10.0)
			numDigits = 2;
		else if (inVal < 100.0)
			numDigits = 1;
		// set formatter
		FORMAT_FLEX.setMaximumFractionDigits(numDigits);
		FORMAT_FLEX.setMinimumFractionDigits(numDigits);
		return FORMAT_FLEX.format(inVal);
	}

	/**
	 * Convert the given hour and minute values into a string H:MM
	 * @param inHour hour of day, 0-24
	 * @param inMinute minute, 0-59
	 * @return string, either H:MM or HH:MM
	 */
	public static String makeTimeString(int inHour, int inMinute)
	{
		StringBuilder sb = new StringBuilder();
		final int hour = (inHour >= 0 && inHour <= 24) ? inHour : 0;
		sb.append(hour);

		sb.append(':');

		final int minute = (inMinute >= 0 && inMinute <= 59) ? inMinute : 0;
		if (minute <= 9) {sb.append('0');}
		sb.append(minute);
		return sb.toString();
	}
}
