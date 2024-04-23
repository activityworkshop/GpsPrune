package tim.prune.function.filesleuth.data;

public class DateRange
{
	private final Date _dateFrom, _dateTo;

	public static final DateRange EMPTY_RANGE = new DateRange(null, null);
	public static final DateRange INVALID_RANGE = new DateRange(null, new Date(-1, -1, -1));


	/** Constructor */
	public DateRange(Date inFrom, Date inTo)
	{
		boolean inOrder = inFrom == null || inTo == null || inTo.isAfter(inFrom);
		_dateFrom = inOrder ? inFrom : inTo;
		_dateTo = inOrder ? inTo : inFrom;
	}

	/** @return a DateRange for a single day */
	public static DateRange parseValues(int inYear, int inMonth, int inDay) {
		return parseValues(inYear, inMonth, inDay, inYear, inMonth, inDay);
	}

	public static DateRange parseValues(int inYearFrom, int inMonthFrom, int inDayFrom,
		int inYearTo, int inMonthTo, int inDayTo)
	{
		if (inYearFrom < 1000 || inYearFrom > 9999 || inYearTo < 1000 || inYearTo > 9999) {
			return null;
		}
		// Whole year(s)
		if (inMonthFrom <= 0 && inDayFrom <= 0 && inMonthTo <= 0 && inDayTo <= 0) {
			return new DateRange(new Date(inYearFrom, 1, 1), new Date(inYearTo, 12, 31));
		}
		// Range of months
		if (inMonthFrom > 0 && inDayFrom <= 0 && inMonthTo > 0 && inDayTo <= 0)
		{
			int daysInMonth = Date.getDaysInMonth(inYearTo, inMonthTo);
			return new DateRange(new Date(inYearFrom, inMonthFrom, 1),
					new Date(inYearTo, inMonthTo, daysInMonth));
		}
		// Range of days
		if (inMonthFrom > 0 && inDayFrom > 0 && inMonthTo > 0 && inDayTo > 0)
		{
			return new DateRange(new Date(inYearFrom, inMonthFrom, inDayFrom),
					new Date(inYearTo, inMonthTo, inDayTo));
		}
		return null;
	}

	public static DateRange parseString(String inValue)
	{
		final String value = (inValue == null ? "" : inValue.trim());
		if (value.isEmpty()) {
			return EMPTY_RANGE;
		}
		if (value.length() < 4) {
			return INVALID_RANGE;
		}
		if (value.length() == 4)
		{
			// Single year
			try {
				final int year = Integer.parseInt(value);
				if (year >= 1000) {
					return new DateRange(new Date(year, 1, 1), new Date(year, 12, 31));
				}
			}
			catch (NumberFormatException e) {}
		}
		else if (value.length() == 7 && Date.isSeparator(value.charAt(4)))
		{
			// single month
			try {
				final int year = Integer.parseInt(value.substring(0, 4));
				final int month = Integer.parseInt(value.substring(5));
				if (year >= 1000 && month >= 1 && month <= 12)
				{
					return new DateRange(new Date(year, month, 1),
							new Date(year, month, Date.getDaysInMonth(year, month)));
				}
			}
			catch (NumberFormatException e) {}
		}
		else if (value.length() == 10 && Date.isSeparator(value.charAt(4)) && Date.isSeparator(value.charAt(7)))
		{
			// single date
			Date date = Date.parseString(value);
			if (date != null) {
				return new DateRange(date, date);
			}
		}
		else if (value.length() >= 21) {
			return parseDatePair(value);
		}
		return INVALID_RANGE;
	}

	public boolean isEmpty() {
		return _dateFrom == null;
	}

	public boolean isValid()
	{
		return this != INVALID_RANGE
			&& _dateFrom != null && _dateFrom.isValid()
			&& _dateTo != null && _dateTo.isValid();
	}

	public Date getDateFrom() {
		return _dateFrom;
	}

	public Date getDateTo() {
		return _dateTo;
	}

	public boolean isYearMonthDay() {
		return toShortString().length() <= 10;
	}

	private static DateRange parseDatePair(String value)
	{
		Date firstDate = Date.parseString(value.substring(0, 10));
		if (firstDate == null || !firstDate.isValid()) {
			return INVALID_RANGE;
		}
		int startOfSecondDate = value.length() - 10;
		for (int pos = 11; pos < startOfSecondDate; pos++)
		{
			if (!Date.isSeparator(value.charAt(pos))) {
				return INVALID_RANGE;
			}
		}
		Date secondDate = Date.parseString(value.substring(startOfSecondDate));
		if (secondDate == null || !secondDate.isValid()) {
			return INVALID_RANGE;
		}
		if (firstDate.isAfter(secondDate)) {
			return new DateRange(secondDate, firstDate); // reverse order
		}
		return new DateRange(firstDate, secondDate);
	}

	public String toString()
	{
		if (isEmpty()) {
			return "";
		}
		if (_dateFrom.equals(_dateTo)) {
			// single date
			return _dateFrom.toString();
		}
		else {
			// different start and end date
			return String.format("%s - %s", _dateFrom.toString(), _dateTo.toString());
		}
	}

	public String toShortString()
	{
		if (isEmpty()) {
			return "";
		}
		final int year = _dateFrom.getYear();
		if (_dateFrom.getYear() == _dateTo.getYear())
		{
			// is it a single year?
			if (_dateFrom.getMonth() == 1 && _dateTo.getMonth() == 12
				&& _dateFrom.getDay() == 1 && _dateTo.getDay() == 31)
			{
				return "" + year;
			}
			// is it a single month?
			final int month = _dateFrom.getMonth();
			if (month >= 1 && month == _dateTo.getMonth()
				&& _dateFrom.getDay() == 1 && _dateTo.getDay() == Date.getDaysInMonth(year, month))
			{
				return String.format("%d-%02d", year, month);
			}
			// single day?
			if (_dateFrom.equals(_dateTo)) {
				return _dateFrom.toString();
			}
		}
		return toString();
	}

	public boolean contains(Date date)
	{
		if (isEmpty()) {
			return true; // no filter or invalid filter
		}
		if (date == null) {
			return false; // filter but no date
		}
		if (_dateFrom.isAfter(date)) {
			return false; // date is before filter
		}
		return _dateTo == null || !date.isAfter(_dateTo);
	}

	public boolean overlaps(DateRange inTrackRange)
	{
		if (isEmpty()) {
			return true;	// no filter
		}
		if (inTrackRange == null || inTrackRange.isEmpty()) {
			return false;	// no dates in track
		}
		if (_dateFrom.isAfter(inTrackRange._dateTo) || inTrackRange._dateFrom.isAfter(_dateTo)) {
			return false;	// no overlap
		}
		return true;
	}

	/** Comparison with another DateRange */
	public boolean equals(DateRange inOtherRange)
	{
		if (isEmpty()) {
			return inOtherRange == null || inOtherRange.isEmpty();
		}
		return _dateFrom.equals(inOtherRange._dateFrom) && _dateTo.equals(inOtherRange._dateTo);
	}

	/** Comparison with an Object */
	public boolean equals(Object inOther)
	{
		DateRange range = (inOther instanceof DateRange ? (DateRange) inOther : null);
		return equals(range);
	}

	public boolean includes(DateRange inTrackRange)
	{
		if (isEmpty()) {
			return inTrackRange == null || inTrackRange.isEmpty();
		}
		if (inTrackRange == null || inTrackRange.isEmpty()) {
			return true;
		}
		return !_dateFrom.isAfter(inTrackRange._dateFrom) && !inTrackRange._dateTo.isAfter(_dateTo);
	}
}
