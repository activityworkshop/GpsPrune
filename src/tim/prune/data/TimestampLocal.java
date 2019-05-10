package tim.prune.data;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;


/**
 * Class to hold a timestamp based on a local timezone, for example
 * from a camera or audio recorder.
 * When the selected timezone changes, this timestamp will keep its
 * date and time but the numerical value will change accordingly.
 */
public class TimestampLocal extends Timestamp
{
	private boolean _valid = false;
	private int _year=0, _month=0, _day=0;
	private int _hour=0, _minute=0, _second=0;


	/**
	 * Constructor giving each field value individually
	 * @param inYear year
	 * @param inMonth month, beginning with 1
	 * @param inDay day of month, beginning with 1
	 * @param inHour hour of day, 0-24
	 * @param inMinute minute
	 * @param inSecond seconds
	 */
	public TimestampLocal(int inYear, int inMonth, int inDay, int inHour, int inMinute, int inSecond)
	{
		_valid = inYear > 0 && inYear < 3000
			&& inMonth > 0 && inMonth < 13
			&& inDay > 0 && inDay < 32
			&& inHour >= 0 && inHour < 24
			&& inMinute >= 0 && inMinute < 60
			&& inSecond >= 0 && inSecond < 60;
		if (_valid)
		{
			_year = inYear;
			_month = inMonth;
			_day = inDay;
			_hour = inHour;
			_minute = inMinute;
			_second = inSecond;
		}
	}


	/** @return true if valid */
	public boolean isValid()
	{
		return _valid;
	}

	@Override
	public Calendar getCalendar(TimeZone inZone)
	{
		Calendar cal = Calendar.getInstance();
		if (inZone != null) {
			cal.setTimeZone(inZone);
		}
		cal.set(Calendar.YEAR, _year);
		cal.set(Calendar.MONTH, _month - 1);
		cal.set(Calendar.DAY_OF_MONTH, _day);
		cal.set(Calendar.HOUR_OF_DAY, _hour);
		cal.set(Calendar.MINUTE, _minute);
		cal.set(Calendar.SECOND, _second);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	@Override
	public long getMilliseconds(TimeZone inZone)
	{
		return getCalendar(inZone).getTimeInMillis();
	}

	@Override
	public void addOffsetSeconds(long inOffset)
	{
		System.err.println("Local timestamps don't support offsets.");
	}

	@Override
	protected boolean hasMilliseconds()
	{
		return false;
	}

	/**
	 * Utility method for formatting dates / times
	 * @param inFormat formatter object
	 * @param inTimezone timezone to use, or null
	 * @return formatted String
	 */
	@Override
	protected String format(DateFormat inFormat, TimeZone inTimezone)
	{
		Calendar cal = getCalendar(inTimezone);
		if (inTimezone != null)
		{
			inFormat.setTimeZone(inTimezone);
		}
		return inFormat.format(cal.getTime());
	}
}
