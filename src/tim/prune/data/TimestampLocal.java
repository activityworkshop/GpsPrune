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
	private final boolean _valid;
	private final int _year, _month, _day;
	private final int _hour, _minute, _second;


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
		_year = _valid ? inYear : 0;
		_month = _valid ? inMonth : 0;
		_day = _valid ? inDay : 0;
		_hour = _valid ? inHour : 0;
		_minute = _valid ? inMinute : 0;
		_second = _valid ? inSecond : 0;
	}


	/** @return true if valid */
	public boolean isValid() {
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
	public long getMilliseconds(TimeZone inZone) {
		return getCalendar(inZone).getTimeInMillis();
	}

	@Override
	public Timestamp addOffsetMilliseconds(long inOffset) {
		throw new IllegalArgumentException("Local timestamps don't support offsets.");
	}

	@Override
	protected boolean hasMilliseconds() {
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
		if (inTimezone != null) {
			inFormat.setTimeZone(inTimezone);
		}
		return inFormat.format(cal.getTime());
	}
}
