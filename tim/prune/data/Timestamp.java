package tim.prune.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to hold the timestamp of a track point
 * and provide conversion functions
 */
public class Timestamp
{
	private boolean _valid = false;
	private long _seconds = 0L;
	private String _text = null;
	private String _timeText = null;

	private static final DateFormat DEFAULT_DATE_FORMAT = DateFormat.getDateTimeInstance();
	private static final DateFormat DEFAULT_TIME_FORMAT = DateFormat.getTimeInstance();
	private static final DateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static final DateFormat ISO_8601_FORMAT_NOZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private static DateFormat[] ALL_DATE_FORMATS = null;
	private static Calendar CALENDAR = null;
	private static final Pattern GENERAL_TIMESTAMP_PATTERN
		= Pattern.compile("(\\d{4})\\D(\\d{2})\\D(\\d{2})\\D(\\d{2})\\D(\\d{2})\\D(\\d{2})");
	private static long SECS_SINCE_1970 = 0L;
	private static long SECS_SINCE_GARTRIP = 0L;
	private static long MSECS_SINCE_1970 = 0L;
	private static long MSECS_SINCE_1990 = 0L;
	private static long TWENTY_YEARS_IN_SECS = 0L;
	private static final long GARTRIP_OFFSET = 631065600L;

	/** Specifies original timestamp format */
	public static final int FORMAT_ORIGINAL = 0;
	/** Specifies locale-dependent timestamp format */
	public static final int FORMAT_LOCALE = 1;
	/** Specifies ISO 8601 timestamp format */
	public static final int FORMAT_ISO_8601 = 2;

	// Static block to initialise offsets
	static
	{
		CALENDAR = Calendar.getInstance();
		MSECS_SINCE_1970 = CALENDAR.getTimeInMillis();
		SECS_SINCE_1970 = MSECS_SINCE_1970 / 1000L;
		SECS_SINCE_GARTRIP = SECS_SINCE_1970 - GARTRIP_OFFSET;
		CALENDAR.add(Calendar.YEAR, -20);
		MSECS_SINCE_1990 = CALENDAR.getTimeInMillis();
		TWENTY_YEARS_IN_SECS = (MSECS_SINCE_1970 - MSECS_SINCE_1990) / 1000L;
		// Date formats
		ALL_DATE_FORMATS = new DateFormat[] {
			DEFAULT_DATE_FORMAT,
			new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy"),
			new SimpleDateFormat("HH:mm:ss dd MMM yyyy"),
			new SimpleDateFormat("dd MMM yyyy HH:mm:ss"),
			new SimpleDateFormat("yyyy MMM dd HH:mm:ss"),
			ISO_8601_FORMAT, ISO_8601_FORMAT_NOZ
		};
	}


	/**
	 * Constructor
	 * @param inString String containing timestamp
	 */
	public Timestamp(String inString)
	{
		// TODO: Does it really help to store timestamps in seconds rather than ms?
		if (inString != null && !inString.equals(""))
		{
			// Try to parse into a long
			try
			{
				long rawValue = Long.parseLong(inString.trim());
				_seconds = getSeconds(rawValue);
				_valid = true;
			}
			catch (NumberFormatException nfe)
			{
				// String is not a long, so try a date/time string instead
				// try each of the date formatters in turn
				Date date = null;
				for (int i=0; i<ALL_DATE_FORMATS.length && !_valid; i++)
				{
					try
					{
						date = ALL_DATE_FORMATS[i].parse(inString);
						CALENDAR.setTime(date);
						_seconds = CALENDAR.getTimeInMillis() / 1000L;
						_valid = true;
					}
					catch (ParseException e) {}
				}
				if (!_valid && inString.length() == 19)
				{
					final Matcher matcher = GENERAL_TIMESTAMP_PATTERN.matcher(inString);
					if (matcher.matches())
					{
						try {
							_seconds = getSeconds(Integer.parseInt(matcher.group(1)),
								Integer.parseInt(matcher.group(2)),
								Integer.parseInt(matcher.group(3)),
								Integer.parseInt(matcher.group(4)),
								Integer.parseInt(matcher.group(5)),
								Integer.parseInt(matcher.group(6)));
							_valid = true;
						}
						catch (NumberFormatException nfe2) {} // parse shouldn't fail if matcher matched
					}
				}
			}
		}
	}


	/**
	 * Constructor giving each field value individually
	 * @param inYear year
	 * @param inMonth month, beginning with 1
	 * @param inDay day of month, beginning with 1
	 * @param inHour hour of day, 0-24
	 * @param inMinute minute
	 * @param inSecond seconds
	 */
	public Timestamp(int inYear, int inMonth, int inDay, int inHour, int inMinute, int inSecond)
	{
		_seconds = getSeconds(inYear, inMonth, inDay, inHour, inMinute, inSecond);
		_valid = true;
	}


	/**
	 * Constructor giving millis
	 * @param inMillis milliseconds since 1970
	 */
	public Timestamp(long inMillis)
	{
		_seconds = inMillis / 1000;
		_valid = true;
	}


	/**
	 * Convert the given timestamp parameters into a number of seconds
	 * @param inYear year
	 * @param inMonth month, beginning with 1
	 * @param inDay day of month, beginning with 1
	 * @param inHour hour of day, 0-24
	 * @param inMinute minute
	 * @param inSecond seconds
	 * @return number of seconds
	 */
	private static long getSeconds(int inYear, int inMonth, int inDay, int inHour, int inMinute, int inSecond)
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, inYear);
		cal.set(Calendar.MONTH, inMonth - 1);
		cal.set(Calendar.DAY_OF_MONTH, inDay);
		cal.set(Calendar.HOUR_OF_DAY, inHour);
		cal.set(Calendar.MINUTE, inMinute);
		cal.set(Calendar.SECOND, inSecond);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis() / 1000;
	}

	/**
	 * Convert the given long parameters into a number of seconds
	 * @param inRawValue long value representing seconds / milliseconds
	 * @return number of seconds
	 */
	private static long getSeconds(long inRawValue)
	{
		// check for each format possibility and pick nearest
		long diff1 = Math.abs(SECS_SINCE_1970 - inRawValue);
		long diff2 = Math.abs(MSECS_SINCE_1970 - inRawValue);
		long diff3 = Math.abs(MSECS_SINCE_1990 - inRawValue);
		long diff4 = Math.abs(SECS_SINCE_GARTRIP - inRawValue);

		// Start off with "seconds since 1970" format
		long smallestDiff = diff1;
		long seconds = inRawValue;
		// Now check millis since 1970
		if (diff2 < smallestDiff)
		{
			// milliseconds since 1970
			seconds = inRawValue / 1000L;
			smallestDiff = diff2;
		}
		// Now millis since 1990
		if (diff3 < smallestDiff)
		{
			// milliseconds since 1990
			seconds = inRawValue / 1000L + TWENTY_YEARS_IN_SECS;
			smallestDiff = diff3;
		}
		// Lastly, check gartrip offset
		if (diff4 < smallestDiff)
		{
			// seconds since gartrip offset
			seconds = inRawValue + GARTRIP_OFFSET;
		}
		return seconds;
	}

	/**
	 * @return true if timestamp is valid
	 */
	public boolean isValid()
	{
		return _valid;
	}

	/**
	 * @param inOther other Timestamp
	 * @return true if this one is after the other
	 */
	public boolean isAfter(Timestamp inOther)
	{
		return _seconds > inOther._seconds;
	}

	/**
	 * Calculate the difference between two Timestamps in seconds
	 * @param inOther other, earlier Timestamp
	 * @return number of seconds since other timestamp
	 */
	public long getSecondsSince(Timestamp inOther)
	{
		return _seconds - inOther._seconds;
	}

	/**
	 * Add the given number of seconds offset
	 * @param inOffset number of seconds to add/subtract
	 */
	public void addOffset(long inOffset)
	{
		_seconds += inOffset;
		_text = null;
	}

	/**
	 * Add the given TimeDifference to this Timestamp
	 * @param inOffset TimeDifference to add
	 * @return new Timestamp object
	 */
	public Timestamp createPlusOffset(TimeDifference inOffset)
	{
		return createPlusOffset(inOffset.getTotalSeconds());
	}

	/**
	 * Add the given number of seconds to this Timestamp
	 * @param inSeconds number of seconds to add
	 * @return new Timestamp object
	 */
	public Timestamp createPlusOffset(long inSeconds)
	{
		return new Timestamp((_seconds + inSeconds) * 1000L);
	}


	/**
	 * Subtract the given TimeDifference from this Timestamp
	 * @param inOffset TimeDifference to subtract
	 * @return new Timestamp object
	 */
	public Timestamp createMinusOffset(TimeDifference inOffset)
	{
		return new Timestamp((_seconds - inOffset.getTotalSeconds()) * 1000L);
	}


	/**
	 * @return Description of timestamp in locale-specific format
	 */
	public String getText()
	{
		return getText(FORMAT_LOCALE);
	}

	/**
	 * @param inFormat format of timestamp
	 * @return Description of timestamp in required format
	 */
	public String getText(int inFormat)
	{
		if (!_valid) {return "";}
		if (inFormat == FORMAT_ISO_8601) {
			return format(ISO_8601_FORMAT);
		}
		if (_text == null) {
			_text = (_valid?format(DEFAULT_DATE_FORMAT):"");
		}
		return _text;
	}

	/**
	 * @return Description of time part of timestamp in locale-specific format
	 */
	public String getTimeText()
	{
		if (_timeText == null)
		{
			if (_valid) {
				_timeText = format(DEFAULT_TIME_FORMAT);
			}
			else _timeText = "";
		}
		return _timeText;
	}

	/**
	 * Utility method for formatting dates / times
	 * @param inFormat formatter object
	 * @return formatted String
	 */
	private String format(DateFormat inFormat)
	{
		CALENDAR.setTimeInMillis(_seconds * 1000L);
		return inFormat.format(CALENDAR.getTime());
	}

	/**
	 * @return a Calendar object representing this timestamp
	 */
	public Calendar getCalendar()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(_seconds * 1000L);
		return cal;
	}
}
