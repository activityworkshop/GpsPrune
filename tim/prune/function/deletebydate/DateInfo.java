package tim.prune.function.deletebydate;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class to hold the information about a date,
 * including how many points correspond to the date
 * and whether it has been selected for deletion or not
 */
public class DateInfo implements Comparable<DateInfo>
{
	/** Date, or null for no date - used for earlier/later comparison */
	private Date _date = null;
	/** String representation of date */
	private String _dateString = null;
	/** Number of points with this date */
	private int _numPoints = 0;
	/** Flag for deletion or retention */
	private boolean _toDelete = false;

	// Doesn't really matter what format is used here, as long as dates are different
	private static final DateFormat DEFAULT_DATE_FORMAT = DateFormat.getDateInstance();


	/**
	 * @param inZone Timezone to use for the date identification
	 */
	public static void setTimezone(TimeZone inZone)
	{
		DEFAULT_DATE_FORMAT.setTimeZone(inZone);
	}

	/**
	 * Constructor
	 * @param inDate date object from timestamp
	 */
	public DateInfo(Date inDate)
	{
		_date = inDate;
		if (_date == null) {
			_dateString = "";
		}
		else {
			_dateString = DEFAULT_DATE_FORMAT.format(_date);
		}
		_numPoints = 0;
		_toDelete = false;
	}

	/**
	 * @return true if this info is for dateless points (points without timestamp)
	 */
	public boolean isDateless() {
		return (_date == null);
	}

	/**
	 * @return string representation of date
	 */
	public String getString() {
		return _dateString;
	}

	/**
	 * Compare with a given Date object to see if they represent the same date
	 * @param inDate date to compare
	 * @return true if they're the same date
	 */
	public boolean isSameDate(Date inDate)
	{
		if (inDate == null) {
			return (_date == null);
		}
		else if (_dateString == null) {
			return false;
		}
		String otherDateString = DEFAULT_DATE_FORMAT.format(inDate);
		return _dateString.equals(otherDateString);
	}

	/**
	 * Increment the point count
	 */
	public void incrementCount() {
		_numPoints++;
	}

	/**
	 * @return point count
	 */
	public int getPointCount() {
		return _numPoints;
	}

	/**
	 * @param inFlag true to delete, false to keep
	 */
	public void setDeleteFlag(boolean inFlag) {
		_toDelete = inFlag;
	}

	/**
	 * @return true to delete, false to keep
	 */
	public boolean getDeleteFlag() {
		return _toDelete;
	}

	/**
	 * Compare with another DateInfo object for sorting
	 */
	public int compareTo(DateInfo inOther)
	{
		// Dateless goes first
		if (_date == null || _dateString == null) {return -1;}
		if (inOther._date == null || inOther._dateString == null) {return 1;}
		// Just compare dates
		return _date.compareTo(inOther._date);
	}
}
