package tim.prune.config;

import java.util.TimeZone;

public abstract class TimezoneHelper
{
	/**
	 * @param inConfig config object
	 * @return the timezone selected in the Config
	 */
	public static TimeZone getSelectedTimezone(Config inConfig)
	{
		final String zoneId = inConfig.getConfigString(Config.KEY_TIMEZONE_ID);
		if (zoneId == null || zoneId.equals("")) {
			return TimeZone.getDefault();
		}
		return TimeZone.getTimeZone(zoneId);
	}
}
