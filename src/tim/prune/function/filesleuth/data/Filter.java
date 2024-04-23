package tim.prune.function.filesleuth.data;

import tim.prune.function.filesleuth.SearchResult;

public class Filter
{
	public enum Comparison {SAME, DIFFERENT, WIDER, NARROWER}

	private final String _filterString;
	private final DateRange _dateRange;
	private final LocationFilter _locationFilter;

	public static final Filter EMPTY_FILTER = new Filter(null, null, null);


	public Filter(String inString) {
		this(inString, null, null);
	}

	public Filter(String inString, String inDateRange, LocationFilter inLocationFilter)
	{
		_filterString = (inString == null ? "" : inString.trim().toLowerCase());
		_dateRange = DateRange.parseString(inDateRange);
		_locationFilter = inLocationFilter;
	}

	public String getString() {
		return _filterString;
	}

	public static Comparison compare(Filter inFilter1, Filter inFilter2)
	{
		boolean firstEmpty = inFilter1 == null || inFilter1.isEmpty();
		boolean secondEmpty = inFilter2 == null || inFilter2.isEmpty();
		if (firstEmpty) {
			return secondEmpty ? Comparison.SAME : Comparison.NARROWER;
		}
		if (secondEmpty) {
			return Comparison.WIDER;
		}
		Comparison textComparison = compareTextFilters(inFilter1, inFilter2);
		Comparison dateComparison = compareDateFilters(inFilter1, inFilter2);
		Comparison locationComparison = compareLocationFilters(inFilter1, inFilter2);
		return combineComparisons(combineComparisons(textComparison, dateComparison),
			locationComparison);
	}

	private static Comparison combineComparisons(Comparison comp1, Comparison comp2)
	{
		if (comp1 == Comparison.SAME || comp1 == comp2) {
			return comp2;
		}
		if (comp2 == Comparison.SAME) {
			return comp1;
		}
		return Comparison.DIFFERENT;
	}

	private static Comparison compareTextFilters(Filter inFilter1, Filter inFilter2)
	{
		boolean firstTextEmpty = !inFilter1.hasTextFilter();
		boolean secondTextEmpty = !inFilter2.hasTextFilter();
		if (firstTextEmpty && !secondTextEmpty) {
			return Comparison.NARROWER;
		}
		if (!firstTextEmpty && secondTextEmpty) {
			return Comparison.WIDER;
		}
		// Neither is empty, so we compare the strings
		final String text1 = inFilter1.getString();
		final String text2 = inFilter2.getString();
		if (text2.equals(text1)) {
			return Comparison.SAME;
		}
		if (text1.contains(text2)) {
			return Comparison.WIDER;
		}
		if (text2.contains(text1)) {
			return Comparison.NARROWER;
		}
		return Comparison.DIFFERENT;
	}

	private static Comparison compareDateFilters(Filter inFilter1, Filter inFilter2)
	{
		boolean firstDateEmpty = !inFilter1.hasDateFilter();
		boolean secondDateEmpty = !inFilter2.hasDateFilter();
		if (firstDateEmpty && secondDateEmpty) {
			return Comparison.SAME;
		}
		if (firstDateEmpty && !secondDateEmpty) {
			return Comparison.NARROWER;
		}
		if (!firstDateEmpty && secondDateEmpty) {
			return Comparison.WIDER;
		}
		// Neither is empty, so we compare the date ranges
		DateRange range1 = inFilter1.getDateFilter();
		DateRange range2 = inFilter2.getDateFilter();
		if (range1.equals(range2)) {
			return Comparison.SAME;
		}
		if (range1.includes(range2)) {
			return Comparison.NARROWER;
		}
		if (range2.includes(range1)) {
			return Comparison.WIDER;
		}
		return Comparison.DIFFERENT;
	}

	private static Comparison compareLocationFilters(Filter inFilter1, Filter inFilter2)
	{
		boolean firstLocEmpty = !inFilter1.hasLocationFilter();
		boolean secondLocEmpty = !inFilter2.hasLocationFilter();
		if (firstLocEmpty && secondLocEmpty) {
			return Comparison.SAME;
		}
		if (firstLocEmpty && !secondLocEmpty) {
			return Comparison.NARROWER;
		}
		if (!firstLocEmpty && secondLocEmpty) {
			return Comparison.WIDER;
		}
		if (inFilter1._locationFilter.equals(inFilter2._locationFilter)) {
			return Comparison.SAME;
		}
		return Comparison.DIFFERENT;
	}

	private boolean isEmpty() {
		return !hasTextFilter() && !hasDateFilter() && !hasLocationFilter();
	}

	private boolean hasTextFilter() {
		return !_filterString.isEmpty();
	}

	private boolean hasDateFilter() {
		return _dateRange != null && _dateRange.isValid();
	}

	public DateRange getDateFilter() {
		return _dateRange == null ? DateRange.EMPTY_RANGE : _dateRange;
	}

	public boolean hasLocationFilter() {
		return _locationFilter != null;
	}

	public LocationFilter getLocationFilter() {
		return _locationFilter;
	}

	public void apply(SearchResult inResult)
	{
		TrackFile track = inResult.getTrackFile();
		boolean matchesFilter = true;
		if (hasTextFilter())
		{
			matchesFilter = track.matchesStringFilter(_filterString, inResult)
					|| track.matchesFilename(_filterString);
		}
		else {
			inResult.setContents(track.getNameOrDescription());
		}
		if (hasDateFilter() && track.hasContents()) {
			matchesFilter = matchesFilter && track.matchesDateFilter(_dateRange);
		}
		if (hasLocationFilter() && track.hasContents()) {
			matchesFilter = matchesFilter && track.matchesLocationFilter(_locationFilter);
		}
		inResult.setIsMatch(matchesFilter);
	}
}
