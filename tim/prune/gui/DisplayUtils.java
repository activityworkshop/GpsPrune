package tim.prune.gui;

import tim.prune.I18nManager;

/**
 * Class to provide general display util methods
 */
public abstract class DisplayUtils
{
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
}
