package tim.prune.function.compress;

/** Enumerates the available compression methods and their keys */
public enum CompressionMethodType
{
	NONE(0, ""),
	DUPLICATES(1, "DUP"),
	NEARBY_WITH_FACTOR(2, "NEF"),
	WACKY_POINTS(3, "WAC"),
	SINGLETONS(4, "SIN"),
	DOUGLAS_PEUCKER(5, "DPC"),
	NEARBY_WITH_DISTANCE(6, "NED"),
	TIME_DIFFERENCE(7, "TSA"),
	TOO_SLOW(8, "SLO"),
	TOO_FAST(9, "FAS"),
	SKI_LIFTS(10, "SKI");

	private final int _index;
	private final String _key;

	/** Constructor */
	CompressionMethodType(int inIndex, String inKey) {
		_index = inIndex;
		_key = inKey + ":";
	}

	public int getIndex() {
		return _index;
	}

	public String getKey() {
		return _key;
	}
}
