package tim.prune.data;

import tim.prune.UpdateMessageBroker;

/**
 * Class to hold all track information, including data
 * and the selection information
 */
public class TrackInfo
{
	private UpdateMessageBroker _broker = null;
	private Track _track = null;
	private Selection _selection = null;
	private FileInfo _fileInfo = null;

	/**
	 * Constructor
	 * @param inTrack Track object
	 * @param inBroker broker object
	 */
	public TrackInfo(Track inTrack, UpdateMessageBroker inBroker)
	{
		_broker = inBroker;
		_track = inTrack;
		_selection = new Selection(_track, inBroker);
		_fileInfo = new FileInfo();
	}


	/**
	 * @return the Track object
	 */
	public Track getTrack()
	{
		return _track;
	}


	/**
	 * @return the Selection object
	 */
	public Selection getSelection()
	{
		return _selection;
	}


	/**
	 * @return the FileInfo object
	 */
	public FileInfo getFileInfo()
	{
		return _fileInfo;
	}

	/**
	 * Get the currently selected point, if any
	 * @return DataPoint if single point selected, otherwise null
	 */
	public DataPoint getCurrentPoint()
	{
		return _track.getPoint(_selection.getCurrentPointIndex());
	}


	/**
	 * Load the specified data into the Track
	 * @param inFieldArray array of Field objects describing fields
	 * @param inPointArray 2d object array containing data
	 * @param inAltFormat altitude format
	 */
	public void loadTrack(Field[] inFieldArray, Object[][] inPointArray, int inAltFormat)
	{
		_track.cropTo(0);
		_track.load(inFieldArray, inPointArray, inAltFormat);
		_selection.clearAll();
	}


	/**
	 * Delete the currently selected range of points
	 * @return true if successful
	 */
	public boolean deleteRange()
	{
		int currPoint = _selection.getCurrentPointIndex();
		int startSel = _selection.getStart();
		int endSel = _selection.getEnd();
		boolean answer = _track.deleteRange(startSel, endSel);
		// clear range selection
		_selection.modifyRangeDeleted();
		return answer;
	}


	/**
	 * Delete the currently selected point
	 * @return true if point deleted
	 */
	public boolean deletePoint()
	{
		if (_track.deletePoint(_selection.getCurrentPointIndex()))
		{
			_selection.modifyPointDeleted();
			_broker.informSubscribers();
			return true;
		}
		return false;
	}


	/**
	 * Compress the track to the given resolution
	 * @param inResolution resolution
	 * @return number of points deleted
	 */
	public int compress(int inResolution)
	{
		int numDeleted = _track.compress(inResolution);
		if (numDeleted > 0)
			_selection.clearAll();
		return numDeleted;
	}


	/**
	 * Delete all the duplicate points in the track
	 * @return number of points deleted
	 */
	public int deleteDuplicates()
	{
		int numDeleted = _track.deleteDuplicates();
		if (numDeleted > 0)
			_selection.clearAll();
		return numDeleted;
	}


	/**
	 * Clone the selected range of data points
	 * @return shallow copy of DataPoint objects
	 */
	public DataPoint[] cloneSelectedRange()
	{
		return _track.cloneRange(_selection.getStart(), _selection.getEnd());
	}


	/**
	 * Interpolate extra points between two selected ones
	 * @param inStartIndex start index of interpolation
	 * @param inNumPoints num points to insert
	 * @return true if successful
	 */
	public boolean interpolate(int inNumPoints)
	{
		boolean success = _track.interpolate(_selection.getStart(), inNumPoints);
		if (success)
			_selection.selectRangeEnd(_selection.getEnd() + inNumPoints);
		return success;
	}
}
