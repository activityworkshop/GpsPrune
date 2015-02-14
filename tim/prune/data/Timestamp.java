package tim.prune.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Class to hold the timestamp of a track point
 * and provide conversion functions
 */
public class Timestamp
{
	private boolean _valid = false;
	private long _seconds = 0L;
	private String _text = null;

	private static DateFormat DEFAULT_DATE_FORMAT = DateFormat.getDateTimeInstance();
	private static DateFormat[] ALL_DATE_FORMATS = null;
	private static Calendar CALENDAR = null;
	private static long SECS_SINCE_1970 = 0L;
	private static long SECS_SINCE_GARTRIP = 0L;
	private static long MSECS_SINCE_1970 = 0L;
	private static long MSECS_SINCE_1990 = 0L;
	private static long TWENTY_YEARS_IN_SECS = 0L;

	private static final long GARTRIP_OFFSET = 631065600L;

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
			new SimpleDateFormat("yyyy MMM dd HH:mm:ss")
		};
	}


	/**
	 * Constructor
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
				// check for each format possibility and pick nearest
				long diff1 = Math.abs(SECS_SINCE_1970 - rawValue);
				long diff2 = Math.abs(MSECS_SINCE_1970 - rawValue);
				long diff3 = Math.abs(MSECS_SINCE_1990 - rawValue);
				long diff4 = Math.abs(SECS_SINCE_GARTRIP - rawValue);

				// Start off with "seconds since 1970" format
				long smallestDiff = diff1;
				_seconds = rawValue;
				// Now check millis since 1970
				if (diff2 < smallestDiff)
				{
					// milliseconds since 1970
					_seconds = rawValue / 1000L;
					smallestDiff = diff2;
				}
				// Now millis since 1990
				if (diff3 < smallestDiff)
				{
					// milliseconds since 1990
					_seconds = rawValue / 1000L + TWENTY_YEARS_IN_SECS;
					smallestDiff = diff3;
				}
				// Lastly, check garmin offset
				if (diff4 < smallestDiff)
				{
					// seconds since garmin offset
					_seconds = rawValue + GARTRIP_OFFSET;
				}
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
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, inYear);
		cal.set(Calendar.MONTH, inMonth - 1);
		cal.set(Calendar.DAY_OF_MONTH, inDay);
		cal.set(Calendar.HOUR_OF_DAY, inHour);
		cal.set(Calendar.MINUTE, inMinute);
		cal.set(Calendar.SECOND, inSecond);
		cal.set(Calendar.MILLISECOND, 0);
		_seconds = cal.getTimeInMillis() / 1000;
		_valid = true;
	}


	/**
	 * Constructor giving millis since 1970
	 * @param inMillis
	 */
	public Timestamp(long inMillis)
	{
		_seconds = inMillis / 1000;
		_valid = true;
	}


	/**
	 * @return true if timestamp is valid
	 */
	public boolean isValid()
	{
		return _valid;
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
	 * @return Description of timestamp in locale-specific format
	 */
	public String getText()
	{
		if (_text == null)
		{
			if (_valid)
			{
				CALENDAR.setTimeInMillis(_seconds * 1000L);
				_text = DEFAULT_DATE_FORMAT.format(CALENDAR.getTime());
			}
			else _text = "";
		}
		return _text;
	}
}
