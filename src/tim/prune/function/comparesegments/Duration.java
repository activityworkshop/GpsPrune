package tim.prune.function.comparesegments;

import tim.prune.gui.DisplayUtils;

/**
 * Defines a duration in the table, so that the presentation
 * is independent of the sort order
 */
class Duration implements Comparable<Duration>
{
	private final long _seconds;
	private final String _displayValue;

	public Duration(long inSeconds)
	{
		_seconds = inSeconds;
		_displayValue = DisplayUtils.buildDurationString(inSeconds);
	}

	public String toString() {
		return _displayValue;
	}

	public int compareTo(Duration inOther) {
		return Long.compare(_seconds, inOther._seconds);
	}
}
