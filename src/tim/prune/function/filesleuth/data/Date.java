package tim.prune.function.filesleuth.data;

import java.util.Calendar;


public class Date
{
	private final int _year;
	private final int _month; // 1 to 12
	private final int _day; // 1 to 31

	/** Constructor */
	Date(int inYear, int inMonth, int inDay)
	{
		_year = inYear;
		_month = inMonth;
		_day = inDay;
	}

	/** @return true if the given character is a recognised date separator */
	static boolean isSeparator(char inChar) {
		return " .-:".indexOf(inChar) >= 0;
	}

	static Date parseString(String inValue)
	{
		String value = (inValue == null ? "" : inValue.trim());
		if (value.length() != 10) {
			return null;
		}
		try
		{
			final int year = Integer.parseInt(value.substring(0, 4));
			final int month = Integer.parseInt(value.substring(5, 7));
			final int day = Integer.parseInt(value.substring(8));
			if (year >= 1000 && month >= 1 && month <= 12
					&& day >= 1 && day <= getDaysInMonth(year, month))
			{
				return new Date(year, month, day);
			}
		}
		catch (NumberFormatException ignored) {}
		return null;
	}

	/** Return number of days in the specified month */
	static int getDaysInMonth(int year, int month)
	{
		if (month >= 1 && month <= 12)
		{
			if (month == 2)
			{
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, month); // gives us the month _after_ the one we want
				cal.set(Calendar.DATE, 1);	  // set to the first of the next month, then subtract a day
				cal.add(Calendar.DATE, -1);
				return cal.get(Calendar.DAY_OF_MONTH);
			}
			else
			{
				final int[] daysInMonth = new int[]{0, 31, 0, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
				return daysInMonth[month];
			}
		}
		return 1;   // actually an error but 1 is ok
	}

	public int getYear() {
		return _year;
	}

	public int getMonth() {
		return _month;
	}

	public int getDay() {
		return _day;
	}

	public String toString() {
		return String.format("%d-%02d-%02d", _year, _month, _day);
	}

	public boolean isAfter(Date inOther) {
		return inOther == null || toString().compareTo(inOther.toString()) > 0;
	}

	public boolean isValid()
	{
		return _year >= 1000 && _year <= 9999
			&& _month >= 1 && _month <= 12
			&& _day >= 1 && _day <= getDaysInMonth(_year, _month);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Date date = (Date) o;
		return _year == date._year && _month == date._month && _day == date._day;
	}
}
