package tim.prune.function.filesleuth.extract;

import java.util.TimeZone;

import tim.prune.function.filesleuth.data.LocationFilter;
import tim.prune.function.filesleuth.data.TrackContents;

public interface ContentExtractor
{
	/** Just interested in the summary */
	TrackContents getContents(TimeZone inTimezone);

	/** Fine location filtering */
	boolean matchesFilter(LocationFilter inFilter);
}
