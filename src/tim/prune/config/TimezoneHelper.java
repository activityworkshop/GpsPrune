package tim.prune.config;

import java.util.TimeZone;

public abstract class TimezoneHelper
{

	/**
	 * @return the timezone selected in the Config
	 */
	public static TimeZone getSelectedTimezone()
	{
		final String zoneId = Config.getConfigString(Config.KEY_TIMEZONE_ID);
		if (zoneId == null || zoneId.equals("")) {
			return TimeZone.getDefault();
		}
		else {
			return TimeZone.getTimeZone(zoneId);
		}
	}

}
