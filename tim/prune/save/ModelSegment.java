package tim.prune.save;

/**
 * Class to hold a single track segment of a data model
 */
public class ModelSegment
{
	/** Start index of segment */
	private int _startIndex = 0;
	/** End index of segment */
	private int _endIndex = 0;
	/** Number of track points within segment */
	private int _numTrackPoints = 0;


	/**
	 * Constructor
	 * @param inStartIndex start index of segment
	 */
	public ModelSegment(int inStartIndex)
	{
		_startIndex = inStartIndex;
	}

	/**
	 * @return start index of segment
	 */
	public int getStartIndex()
	{
		return _startIndex;
	}

	/**
	 * @param inEndIndex end index of segment
	 */
	public void setEndIndex(int inEndIndex)
	{
		_endIndex = inEndIndex;
	}

	/**
	 * @return end index of segment
	 */
	public int getEndIndex()
	{
		return _endIndex;
	}

	/**
	 * @param inNumPoints number of track points in segment
	 */
	public void setNumTrackPoints(int inNumPoints)
	{
		_numTrackPoints = inNumPoints;
	}

	/**
	 * @return number of track points in segment
	 */
	public int getNumTrackPoints()
	{
		return _numTrackPoints;
	}
}
