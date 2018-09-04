package tim.prune.data;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Class to hold a UTC-based timestamp, for example of a track point.
 * When the selected timezone changes, this timestamp will keep its
 * numerical value but the date and time will change accordingly.
 */
public class TimestampUtc extends Timestamp
{
	private boolean _valid = false;
	private long _milliseconds = 0L;
	private String _text = null;

	private static final DateFormat ISO_8601_FORMAT_NOZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private static DateFormat[] ALL_DATE_FORMATS = null;
	private static Calendar CALENDAR = null;
	private static final Pattern ISO8601_FRACTIONAL_PATTERN
		= Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})(?:[\\.,](\\d{1,3}))?(Z|[\\+-]\\d{2}(?::?\\d{2})?)?");
		//                    year     month     day T  hour    minute    sec             millisec   Z or +/-  hours  :   minutes
	private static final Pattern GENERAL_TIMESTAMP_PATTERN
		= Pattern.compile("(\\d{4})\\D(\\d{2})\\D(\\d{2})\\D(\\d{2})\\D(\\d{2})\\D(\\d{2})");
	private static long SECS_SINCE_1970 = 0L;
	private static long SECS_SINCE_GARTRIP = 0L;
	private static long MSECS_SINCE_1970 = 0L;
	private static long MSECS_SINCE_1990 = 0L;
	private static long TWENTY_YEARS_IN_SECS = 0L;
	private static final long GARTRIP_OFFSET = 631065600L;

	/** Identifier for the parsing strategy to use */
	private enum ParseType
	{
		NONE,
		ISO8601_FRACTIONAL,
		LONG,
		FIXED_FORMAT0,
		FIXED_FORMAT1,
		FIXED_FORMAT2,
		FIXED_FORMAT3,
		FIXED_FORMAT4,
		FIXED_FORMAT5,
		FIXED_FORMAT6,
		FIXED_FORMAT7,
		FIXED_FORMAT8,
		GENERAL_STRING
	}

	/** Array of parse types to loop through (first one is changed to last successful type) */
	private static ParseType[] ALL_PARSE_TYPES = {ParseType.NONE, ParseType.ISO8601_FRACTIONAL, ParseType.LONG,
		ParseType.FIXED_FORMAT0, ParseType.FIXED_FORMAT1, ParseType.FIXED_FORMAT2, ParseType.FIXED_FORMAT3,
		ParseType.FIXED_FORMAT4, ParseType.FIXED_FORMAT5, ParseType.FIXED_FORMAT6, ParseType.FIXED_FORMAT7,
		ParseType.FIXED_FORMAT8, ParseType.GENERAL_STRING};

	// Static block to initialise offsets
	static
	{
		CALENDAR = Calendar.getInstance();
		TimeZone gmtZone = TimeZone.getTimeZone("GMT");
		CALENDAR.setTimeZone(gmtZone);
		MSECS_SINCE_1970 = CALENDAR.getTimeInMillis();
		SECS_SINCE_1970 = MSECS_SINCE_1970 / 1000L;
		SECS_SINCE_GARTRIP = SECS_SINCE_1970 - GARTRIP_OFFSET;
		CALENDAR.add(Calendar.YEAR, -20);
		MSECS_SINCE_1990 = CALENDAR.getTimeInMillis();
		TWENTY_YEARS_IN_SECS = (MSECS_SINCE_1970 - MSECS_SINCE_1990) / 1000L;
		// Date formats
		ALL_DATE_FORMATS = new DateFormat[]
		{
			DEFAULT_DATETIME_FORMAT,
			new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy"),
			new SimpleDateFormat("HH:mm:ss dd MMM yyyy"),
			new SimpleDateFormat("dd MMM yyyy HH:mm:ss"),
			new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"),
			new SimpleDateFormat("yyyy MMM dd HH:mm:ss"),
			new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa"),
			ISO_8601_FORMAT, ISO_8601_FORMAT_NOZ
		};
		for (DateFormat df : ALL_DATE_FORMATS)
		{
			df.setLenient(false);
			df.setTimeZone(gmtZone);
		}
	}


	/**
	 * Constructor
	 * @param inString String containing timestamp
	 */
	public TimestampUtc(String inString)
	{
		_valid = false;
		_text = null;
		if (inString != null && !inString.equals(""))
		{
			// Try each of the parse types in turn
			for (ParseType type : ALL_PARSE_TYPES)
			{
				if (parseString(inString, type))
				{
					ALL_PARSE_TYPES[0] = type;
					_valid = true;
					_text = inString;
					return;
				}
			}
		}
	}

	/**
	 * Try to parse the given string in the specified way
	 * @param inString String to parse
	 * @param inType parse type to use
	 * @return true if successful
	 */
	private boolean parseString(String inString, ParseType inType)
	{
		if (inString == null || inString.equals("")) {
			return false;
		}
		switch (inType)
		{
			case NONE: return false;
			case LONG:
				// Try to parse into a long
				try
				{
					long rawValue = Long.parseLong(inString.trim());
					_milliseconds = getMilliseconds(rawValue);
					return true;
				}
				catch (NumberFormatException nfe)
				{}
				break;

			case ISO8601_FRACTIONAL:
				final Matcher fmatcher = ISO8601_FRACTIONAL_PATTERN.matcher(inString);
				if (fmatcher.matches())
				{
					try {
						_milliseconds = getMilliseconds(Integer.parseInt(fmatcher.group(1)), // year
							Integer.parseInt(fmatcher.group(2)), // month
							Integer.parseInt(fmatcher.group(3)), // day
							Integer.parseInt(fmatcher.group(4)), // hour
							Integer.parseInt(fmatcher.group(5)), // minute
							Integer.parseInt(fmatcher.group(6)), // second
							fmatcher.group(7),                   // fractional seconds
							fmatcher.group(8));                  // timezone, if any
						return true;
					}
					catch (NumberFormatException nfe) {}
				}
				break;

			case FIXED_FORMAT0: return parseString(inString, ALL_DATE_FORMATS[0]);
			case FIXED_FORMAT1: return parseString(inString, ALL_DATE_FORMATS[1]);
			case FIXED_FORMAT2: return parseString(inString, ALL_DATE_FORMATS[2]);
			case FIXED_FORMAT3: return parseString(inString, ALL_DATE_FORMATS[3]);
			case FIXED_FORMAT4: return parseString(inString, ALL_DATE_FORMATS[4]);
			case FIXED_FORMAT5: return parseString(inString, ALL_DATE_FORMATS[5]);
			case FIXED_FORMAT6: return parseString(inString, ALL_DATE_FORMATS[6]);
			case FIXED_FORMAT7: return parseString(inString, ALL_DATE_FORMATS[7]);
			case FIXED_FORMAT8: return parseString(inString, ALL_DATE_FORMATS[8]);

			case GENERAL_STRING:
				if (inString.length() == 19)
				{
					final Matcher matcher = GENERAL_TIMESTAMP_PATTERN.matcher(inString);
					if (matcher.matches())
					{
						try {
							_milliseconds = getMilliseconds(Integer.parseInt(matcher.group(1)),
								Integer.parseInt(matcher.group(2)),
								Integer.parseInt(matcher.group(3)),
								Integer.parseInt(matcher.group(4)),
								Integer.parseInt(matcher.group(5)),
								Integer.parseInt(matcher.group(6)),
								null, null); // no fractions of a second and no timezone
							return true;
						}
						catch (NumberFormatException nfe2) {} // parse shouldn't fail if matcher matched
					}
				}
				return false;
		}
		return false;
	}


	/**
	 * Try to parse the given string with the given date format
	 * @param inString String to parse
	 * @param inDateFormat Date format to use
	 * @return true if successful
	 */
	private boolean parseString(String inString, DateFormat inDateFormat)
	{
		ParsePosition pPos = new ParsePosition(0);
		Date date = inDateFormat.parse(inString, pPos);
		if (date != null && inString.length() == pPos.getIndex()) // require use of _all_ the string, not just the beginning
		{
			CALENDAR.setTime(date);
			_milliseconds = CALENDAR.getTimeInMillis();
			return true;
		}

		return false;
	}


	/**
	 * Constructor giving millis
	 * @param inMillis milliseconds since 1970
	 */
	public TimestampUtc(long inMillis)
	{
		_milliseconds = inMillis;
		_valid = true;
	}


	/**
	 * Convert the given timestamp parameters into a number of milliseconds
	 * @param inYear year
	 * @param inMonth month, beginning with 1
	 * @param inDay day of month, beginning with 1
	 * @param inHour hour of day, 0-24
	 * @param inMinute minute
	 * @param inSecond seconds
	 * @param inFraction fractions of a second
	 * @param inTimezone timezone, if any
	 * @return number of milliseconds
	 */
	private static long getMilliseconds(int inYear, int inMonth, int inDay,
		int inHour, int inMinute, int inSecond, String inFraction, String inTimezone)
	{
		Calendar cal = Calendar.getInstance();
		// Timezone, if any
		if (inTimezone == null || inTimezone.equals("") || inTimezone.equals("Z")) {
			// No timezone, use zulu
			cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		}
		else {
			// Timezone specified, pass to calendar
			cal.setTimeZone(TimeZone.getTimeZone("GMT" + inTimezone));
		}
		cal.set(Calendar.YEAR, inYear);
		cal.set(Calendar.MONTH, inMonth - 1);
		cal.set(Calendar.DAY_OF_MONTH, inDay);
		cal.set(Calendar.HOUR_OF_DAY, inHour);
		cal.set(Calendar.MINUTE, inMinute);
		cal.set(Calendar.SECOND, inSecond);
		int millis = 0;
		if (inFraction != null)
		{
			try {
				int frac = Integer.parseInt(inFraction);
				final int fracLen = inFraction.length();
				switch (fracLen) {
					case 1: millis = frac * 100; break;
					case 2: millis = frac * 10;  break;
					case 3: millis = frac;       break;
				}
			}
			catch (NumberFormatException nfe) {} // ignore errors, millis stay at 0
		}
		cal.set(Calendar.MILLISECOND, millis);
		return cal.getTimeInMillis();
	}

	/**
	 * Convert the given long parameters into a number of milliseconds
	 * @param inRawValue long value representing seconds / milliseconds
	 * @return number of milliseconds
	 */
	private static long getMilliseconds(long inRawValue)
	{
		// check for each format possibility and pick nearest
		long diff1 = Math.abs(SECS_SINCE_1970 - inRawValue);
		long diff2 = Math.abs(MSECS_SINCE_1970 - inRawValue);
		long diff3 = Math.abs(MSECS_SINCE_1990 - inRawValue);
		long diff4 = Math.abs(SECS_SINCE_GARTRIP - inRawValue);

		// Start off with "seconds since 1970" format
		long smallestDiff = diff1;
		long millis = inRawValue * 1000;
		// Now check millis since 1970
		if (diff2 < smallestDiff)
		{
			// milliseconds since 1970
			millis = inRawValue;
			smallestDiff = diff2;
		}
		// Now millis since 1990
		if (diff3 < smallestDiff)
		{
			// milliseconds since 1990
			millis = inRawValue + TWENTY_YEARS_IN_SECS * 1000L;
			smallestDiff = diff3;
		}
		// Lastly, check gartrip offset
		if (diff4 < smallestDiff)
		{
			// seconds since gartrip offset
			millis = (inRawValue + GARTRIP_OFFSET) * 1000L;
		}
		return millis;
	}

	/**
	 * @return true if timestamp is valid
	 */
	public boolean isValid()
	{
		return _valid;
	}

	/**
	 * @return true if the timestamp has non-zero milliseconds
	 */
	protected boolean hasMilliseconds()
	{
		return isValid() && (_milliseconds % 1000L) > 0;
	}

	/**
	 * @return the milliseconds according to the given timezone
	 */
	public long getMilliseconds(TimeZone inZone)
	{
		return _milliseconds;
	}


	/**
	 * Add the given number of seconds offset
	 * @param inOffset number of seconds to add/subtract
	 */
	public void addOffsetSeconds(long inOffset)
	{
		_milliseconds += (inOffset * 1000L);
		_text = null;
	}


	/**
	 * @param inFormat format of timestamp
	 * @param inTimezone timezone to use
	 * @return Description of timestamp in required format
	 */
	public String getText(Format inFormat, TimeZone inTimezone)
	{
		// Use the cached text if possible
		if (isValid()
			&& _text != null
			&& inFormat == Format.ORIGINAL)
		{
			return _text;
		}

		// Nothing cached, so use the regular one
		return super.getText(inFormat, inTimezone);
	}

	/**
	 * Utility method for formatting dates / times
	 * @param inFormat formatter object
	 * @param inTimezone timezone to use
	 * @return formatted String
	 */
	protected String format(DateFormat inFormat, TimeZone inTimezone)
	{
		CALENDAR.setTimeZone(TimeZone.getTimeZone("GMT"));
		inFormat.setTimeZone(inTimezone == null ? TimeZone.getTimeZone("GMT") : inTimezone);

		CALENDAR.setTimeInMillis(_milliseconds);
		return inFormat.format(CALENDAR.getTime());
	}

	/**
	 * @return a Calendar object representing this timestamp
	 */
	public Calendar getCalendar(TimeZone inZone)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(_milliseconds);
		return cal;
	}
}
