package tim.prune.correlate;

/**
 * Simple class to hold a time and an index.
 * Used in a TreeSet for calculating median time difference
 */
public class TimeIndexPair implements Comparable<TimeIndexPair>
{
	/** Time as long */
	private long _time = 0L;
	/** Index as int */
	private int _index = 0;


	/**
	 * Constructor
	 * @param inTime time as long
	 * @param inIndex index as int
	 */
	public TimeIndexPair(long inTime, int inIndex)
	{
		_time = inTime;
		_index = inIndex;
	}


	/**
	 * @return the index
	 */
	public int getIndex()
	{
		return _index;
	}


	/**
	 * Compare two TimeIndexPair objects
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TimeIndexPair inOther)
	{
		int compare = (int) (_time - inOther._time);
		if (compare == 0) {compare = _index - inOther._index;}
		return compare;
	}

	/**
	 * Override equals method to match compareTo
	 */
	public boolean equals(Object inOther)
	{
		if (inOther instanceof TimeIndexPair) {
			TimeIndexPair otherPair = (TimeIndexPair) inOther;
			return _time == otherPair._time;
		}
		return false;
	}
}
