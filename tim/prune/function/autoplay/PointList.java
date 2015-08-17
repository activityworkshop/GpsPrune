package tim.prune.function.autoplay;

/**
 * Class to hold a list of points and hold a running position
 */
public class PointList
{
	/** Array of milliseconds for each point */
	private long[] _millis = null;
	/** Array of indexes of corresponding points */
	private int[]  _indexes = null;
	/** Array index of current position */
	private int    _currentItem = 0;
	/** Max array index */
	private int    _maxItem = 0;

	/**
	 * Constructor
	 * @param inNumPoints number of points
	 */
	public PointList(int inNumPoints)
	{
		_millis = new long[inNumPoints];
		_indexes = new int[inNumPoints];
		_currentItem = 0;
		_maxItem = inNumPoints - 1;
	}

	/**
	 * Add a point to the array
	 * @param inMillis milliseconds since start
	 * @param inIndex point index
	 */
	public void setPoint(long inMillis, int inIndex)
	{
		_millis[_currentItem] = inMillis;
		_indexes[_currentItem] = inIndex;
		_currentItem++;
	}

	/**
	 * Set the position using the current milliseconds
	 * @param inMillis milliseconds since start
	 */
	public void set(long inMillis)
	{
		if (isFinished() || inMillis < _millis[_currentItem])
		{
			// must be reset
			_currentItem = 0;
		}
		while (_currentItem < _maxItem && _millis[_currentItem + 1] < inMillis)
		{
			_currentItem++;
		}
	}

	/**
	 * Normalize the list to cover the requested number of seconds duration
	 * @param inSeconds length of autoplay sequence in seconds
	 */
	public void normalize(int inSeconds)
	{
		if (_maxItem <= 0)
		{
			return; // nothing to normalize
		}
		long currentDuration = _millis[_maxItem] - _millis[0];
		if (currentDuration > 0L)
		{
			double multFactor = inSeconds * 1000.0 / currentDuration;
			for (int i=0; i<=_maxItem; i++)
			{
				_millis[i] = (long) (_millis[i] * multFactor);
			}
		}
	}

	/** @return the milliseconds of the current point */
	public long getCurrentMilliseconds()
	{
		if (isAtStart() || isFinished()) {
			return 0L;
		}
		return _millis[_currentItem];
	}

	/** @return the index of the current point */
	public int getCurrentPointIndex()
	{
		return _indexes[_currentItem];
	}

	/** @return true if we're on the first point */
	public boolean isAtStart() {
		return _currentItem == 0;
	}

	/** @return true if we're on the last point */
	public boolean isFinished() {
		return _currentItem >= _maxItem;
	}

	/**
	 * @param inCurrentMillis current time in milliseconds since start
	 * @return number of milliseconds to wait until next point is due
	 */
	public long getMillisUntilNextPoint(long inCurrentMillis)
	{
		if (isFinished() || _millis[_currentItem+1] < _millis[_currentItem]) {
			return 0; // no next point
		}
		return _millis[_currentItem+1] - inCurrentMillis;
	}
}
