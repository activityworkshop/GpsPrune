package tim.prune.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Superclass of all timestamp implementations
 */
public abstract class Timestamp
{
	private static final DateFormat DEFAULT_DATE_FORMAT = DateFormat.getDateInstance();
	private static final DateFormat DEFAULT_TIME_FORMAT = DateFormat.getTimeInstance();

	protected static final DateFormat DEFAULT_DATETIME_FORMAT = DateFormat.getDateTimeInstance();

	// These date formats use Locale.US to guarantee that Arabic numerals (0-9) will be used
	// to format the timestamps, not whatever other numerals are configured on the default system locale.
	protected static final DateFormat ISO_8601_FORMAT
		= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
	protected static final DateFormat ISO_8601_FORMAT_WITH_MILLIS
		= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
	// This one doesn't need US because it's just for display on screen
	protected static final DateFormat ISO_8601_FORMAT_JUST_DATE
		= new SimpleDateFormat("yyyy-MM-dd");

	private static boolean _millisAddedToTimeFormat = false;


	/** Possible formats for parsing and displaying timestamps */
	public enum Format
	{
		ORIGINAL,
		LOCALE,
		ISO8601
	}


	// Static block to initialise date formats
	static
	{
		// Set timezone for output
		TimeZone gmtZone = TimeZone.getTimeZone("GMT");
		ISO_8601_FORMAT.setTimeZone(gmtZone);
		ISO_8601_FORMAT_WITH_MILLIS.setTimeZone(gmtZone);
		DEFAULT_DATETIME_FORMAT.setTimeZone(gmtZone);
	}


	/**
	 * @return true if valid
	 */
	public abstract boolean isValid();

	/**
	 * Get a calendar representing this timestamp
	 */
	public abstract Calendar getCalendar(TimeZone inZone);

	/**
	 * @return the milliseconds according to the given timezone
	 */
	public abstract long getMilliseconds(TimeZone inZone);

	/**
	 * @return true if this timestamp is after the other one
	 */
	public boolean isAfter(Timestamp inOther) {
		return getMillisecondsSince(inOther) > 0L;
	}

	/**
	 * @return true if this timestamp is before the other one
	 */
	public boolean isBefore(Timestamp inOther) {
		return getMillisecondsSince(inOther) < 0L;
	}

	/**
	 * @return true if this timestamp is equal to the other one
	 */
	public boolean isEqual(Timestamp inOther) {
		return getMillisecondsSince(inOther) == 0L;
	}

	/**
	 * @return the number of seconds since the other timestamp
	 */
	public long getSecondsSince(Timestamp inOther) {
		return getMillisecondsSince(inOther) / 1000L;
	}

	/**
	 * Calculate the difference between two Timestamps in milliseconds
	 * @param inOther other, earlier Timestamp
	 * @return number of milliseconds since other timestamp
	 */
	public long getMillisecondsSince(Timestamp inOther) {
		return getMilliseconds(null) - inOther.getMilliseconds(null);
	}

	/**
	 * @return the number of seconds since the other timestamp using the given timezone
	 */
	public long getSecondsSince(Timestamp inOther, TimeZone inTimezone) {
		return (getMilliseconds(inTimezone) - inOther.getMilliseconds(inTimezone)) / 1000L;
	}

	/**
	 * Add the given number of seconds offset
	 * @param inOffset number of seconds to add/subtract
	 * @return a new Timestamp offset from the current one
	 */
	public Timestamp addOffsetSeconds(long inOffset) {
		return addOffsetMilliseconds(inOffset * 1000L);
	}

	/**
	 * Add the given number of milliseconds offset
	 * @param inOffset number of milliseconds to add/subtract
	 * @return a new Timestamp offset from the current one
	 */
	public abstract Timestamp addOffsetMilliseconds(long inOffset);

	/**
	 * @return true if the timestamp has non-zero milliseconds
	 */
	protected abstract boolean hasMilliseconds();


	/**
	 * @return date part of timestamp in locale-specific format
	 */
	public String getDateText(TimeZone inTimezone) {
		return isValid() ? format(DEFAULT_DATE_FORMAT, inTimezone) : "";
	}

	/**
	 * @return Description of time part of timestamp in locale-specific format
	 */
	public String getTimeText(TimeZone inTimezone)
	{
		if (!isValid()) return "";
		// Maybe we should add milliseconds to this format?
		if (hasMilliseconds() && !_millisAddedToTimeFormat)
		{
			try
			{
				SimpleDateFormat sdf = (SimpleDateFormat) DEFAULT_TIME_FORMAT;
				String pattern = sdf.toPattern();
				if (pattern.indexOf("ss") > 0 && !pattern.contains("SS"))
				{
					sdf.applyPattern(pattern.replaceFirst("s+", "$0.SSS"));
					_millisAddedToTimeFormat = true;
				}
			}
			catch (ClassCastException ignored) {}
		}
		return format(DEFAULT_TIME_FORMAT, inTimezone);
	}

	/**
	 * Utility method for formatting dates / times
	 */
	protected abstract String format(DateFormat inFormat, TimeZone inTimezone);


	/**
	 * @return Description of timestamp in locale-specific format
	 */
	public String getText(TimeZone inTimezone) {
		return getText(Format.LOCALE, inTimezone);
	}

	/**
	 * @return date string in iso format (yyyy-mm-dd)
	 */
	public String getIsoDateString(TimeZone inTimezone) {
		return format(ISO_8601_FORMAT_JUST_DATE, inTimezone);
	}

	/**
	 * @param inFormat format of timestamp
	 * @return Description of timestamp in required format
	 */
	public String getText(Format inFormat, TimeZone inTimezone)
	{
		if (!isValid()) {
			return "";
		}
		switch (inFormat)
		{
			case ORIGINAL:
			case LOCALE:
			default:
				return format(DEFAULT_DATETIME_FORMAT, inTimezone);
			case ISO8601:
				DateFormat dateFormat = hasMilliseconds() ? ISO_8601_FORMAT_WITH_MILLIS : ISO_8601_FORMAT;
				return format(dateFormat, inTimezone);
		}
	}

}
