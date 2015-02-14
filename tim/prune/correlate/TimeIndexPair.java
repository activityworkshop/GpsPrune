package tim.prune.correlate;

/**
 * Simple class to hold a time and an index.
 * Used in a TreeSet for calculating median time difference
 */
public class TimeIndexPair implements Comparable
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
	public int compareTo(Object inOther)
	{
		TimeIndexPair other = (TimeIndexPair) inOther;
		return (int) (_time - other._time);
	}
}
