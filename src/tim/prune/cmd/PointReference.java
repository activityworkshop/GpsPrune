package tim.prune.cmd;

import tim.prune.data.Timestamp;

/**
 * Reference to a point used for sorting, to calculate the new indices
 */
public class PointReference implements Comparable<PointReference>
{
	private final int _originalIndex;
	private final String _name;
	private final Timestamp _timestamp;
	private final int _desiredIndex;

	/**
	 * Constructor
	 * @param inOriginalIndex original index in track
	 * @param inName waypoint name or media filename, if sorting by name
	 * @param inTimestamp point timestamp or media timestamp, if sorting by time
	 */
	public PointReference(int inOriginalIndex, String inName, Timestamp inTimestamp)
	{
		_originalIndex = inOriginalIndex;
		_name = inName;
		_timestamp = inTimestamp;
		_desiredIndex = 0;
	}

	/**
	 * Constructor
	 * @param inOriginalIndex original index in track
	 * @param inDesiredIndex artificial sort index
	 */
	public PointReference(int inOriginalIndex, int inDesiredIndex)
	{
		_originalIndex = inOriginalIndex;
		_name = null;
		_timestamp = null;
		_desiredIndex = inDesiredIndex;
	}

	/** @return original index */
	public int getIndex() {
		return _originalIndex;
	}

	@Override
	public int compareTo(PointReference other)
	{
		int compareVal = _desiredIndex - other._desiredIndex;
		if (compareVal == 0)
		{
			compareVal = compareNames(other);
			if (compareVal == 0)
			{
				compareVal = compareTimes(other);
				if (compareVal == 0) {
					compareVal = _originalIndex - other._originalIndex;
				}
			}
		}
		return compareVal;
	}

	/**
	 * Compare this reference's name with the other one
	 * @param other other point reference
	 * @return -1, 0 or 1 depending on ordering
	 */
	private int compareNames(PointReference other)
	{
		if (_name == null && other._name == null) {
			return 0;
		}
		if (_name == null || other._name == null) {
			return _name == null ? 1 : -1;
		}
		return _name.compareTo(other._name);
	}

	/**
	 * Compare this reference's timestamp with the other one
	 * @param other other point reference
	 * @return -1, 0 or 1 depending on ordering
	 */
	private int compareTimes(PointReference other)
	{
		if (_timestamp == null && other._timestamp == null) {
			return 0;
		}
		if (_timestamp == null || other._timestamp == null) {
			return _timestamp == null ? 1 : -1;
		}
		final long diff =  _timestamp.getMillisecondsSince(other._timestamp);
		return Long.compare(diff, 0L);
	}
}
