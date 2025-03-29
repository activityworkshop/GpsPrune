package tim.prune.function.comparesegments;

import java.util.TimeZone;

import tim.prune.data.Timestamp;

class DateForTable implements Comparable<DateForTable>
{
	private final Timestamp _timestamp;
	private final String _asString;
	DateForTable(Timestamp inTimestamp, TimeZone inTimezone)
	{
		_timestamp = inTimestamp;
		_asString = inTimestamp.getDateText(inTimezone);
	}

	/** Comparison function for sorting */
	public int compareTo(DateForTable inOther)
	{
		if (_timestamp.isBefore(inOther._timestamp)) {
			return -1;
		}
		else if (_timestamp.isEqual(inOther._timestamp)) {
			return 0;
		}
		return 1;
	}

	public String toString() {
		return _asString;
	}
}
