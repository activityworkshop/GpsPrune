package tim.prune.function.filesleuth.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.TimeZone;

import tim.prune.data.Timestamp;
import tim.prune.data.TimestampUtc;

/**
 * Holds the information retrieved from the track file over which the searches can be done
 */
public class TrackContents
{
	private String _nameOrDesc = "";
	private HashSet<String> _strings = null;
	private final TimeZone _timezone;
	private Date _dateFrom = null, _dateTo = null;
	private final TrackBounds _trackBounds = new TrackBounds();


	/**
	 * @param inTimezone timezone to use for date parsing
	 */
	public TrackContents(TimeZone inTimezone) {
		_timezone = inTimezone;
	}

	public void setName(String inName) {
		_nameOrDesc = trimString(inName);
	}

	public void setDescription(String inDesc)
	{
		if (_nameOrDesc.isEmpty()) {
			_nameOrDesc = trimString(inDesc);
		}
	}

	public String getNameOrDescription() {
		return _nameOrDesc;
	}

	private String trimString(String inString) {
		return inString == null ? "" : inString.trim();
	}

	public void addString(String inString)
	{
		if (inString == null) {
			return;
		}
		String trimmedString = inString.trim();
		if (trimmedString.equals("")) {
			return;
		}
		if (_strings == null) {
			_strings = new HashSet<>();
		}
		_strings.add(trimmedString.toLowerCase());
	}

	public int getNumStrings() {
		return _strings == null ? 0 : _strings.size();
	}

	public Collection<String> getStrings() {
		return _strings;
	}

	public void addDateString(String inDate)
	{
		if (inDate == null || inDate.equals("")) {
			return;
		}
		Timestamp timestamp = new TimestampUtc(inDate);
		if (timestamp.isValid())
		{
			Date date = Date.parseString(timestamp.getIsoDateString(_timezone));
			if (date != null)
			{
				if (_dateFrom == null || _dateFrom.isAfter(date)) {
					_dateFrom = date;
				}
				if (date.isAfter(_dateTo)) {
					_dateTo = date;
				}
			}
		}
	}

	public DateRange getDateRange() {
		return new DateRange(_dateFrom, _dateTo);
	}

	public boolean matchesDateFilter(DateRange inFilter) {
		return inFilter.overlaps(getDateRange());
	}

	public void addCoordinates(double inLatitude, double inLongitude) {
		_trackBounds.addCoordinates(inLatitude, inLongitude);
	}

	/**
	 * Note, this is just the _coarse_ location filter
	 * using the bounding rectangle. If this matches, then
	 * the fine filter will be triggered separately.
	 */
	public boolean matchesLocationFilter(LocationFilter inFilter)
	{
		return inFilter == null
				|| inFilter.getPoint() == null
				|| _trackBounds.overlaps(inFilter);
	}
}
