package tim.prune.data;

import tim.prune.DataSubscriber;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;

/**
 * Class to represent a selected portion of a Track and its properties
 */
public class Selection
{
	private final Track _track;
	private int _currentPoint = -1;
	private int _prevNumPoints = 0;
	private int _startIndex = -1, _endIndex = -1;
	private int _currentPhotoIndex = -1;
	private int _currentAudioIndex = -1;


	/**
	 * Constructor
	 * @param inTrack track object
	 */
	public Selection(Track inTrack) {
		_track = inTrack;
	}


	/**
	 * @return the current point index
	 */
	public int getCurrentPointIndex() {
		return _currentPoint;
	}


	/**
	 * @return true if range is selected
	 */
	public boolean hasRangeSelected() {
		return _startIndex >= 0 && _endIndex > _startIndex;
	}


	/**
	 * Recalculate all selection details
	 */
	private void recalculate()
	{
		final int numPoints = _track.getNumPoints();
		// Recheck if the number of points has changed
		if (numPoints != _prevNumPoints)
		{
			_prevNumPoints = numPoints;
			check();
		}
	}

	/**
	 * @param inConfig Config object for altitude tolerance
	 * @return stats about current range
	 */
	public RangeStats getRangeStats(Config inConfig)
	{
		final int altitudeTolerance = inConfig.getConfigInt(Config.KEY_ALTITUDE_TOLERANCE) / 100;
		if (_track.getNumPoints() > 0 && hasRangeSelected()) {
			return new RangeStats(_track, _startIndex, _endIndex, altitudeTolerance);
		}
		else {
			return new RangeStats(altitudeTolerance);
		}
	}


	/**
	 * @return start index
	 */
	public int getStart()
	{
		recalculate();
		return _startIndex;
	}


	/**
	 * @return end index
	 */
	public int getEnd()
	{
		recalculate();
		return _endIndex;
	}

	/**
	 * Clear selected point, range, photo and audio
	 */
	public void clearAll()
	{
		_currentPoint = -1;
		selectRange(-1, -1);
		_currentPhotoIndex = -1;
		_currentAudioIndex = -1;
		check();
	}


	/**
	 * Select range from start to end
	 * @param inStartIndex index of start of range
	 * @param inEndIndex index of end of range
	 */
	public void selectRange(int inStartIndex, int inEndIndex)
	{
		_startIndex = inStartIndex;
		_endIndex = inEndIndex;
		check();
	}


	/**
	 * Select the range from the current point
	 */
	public void selectRangeStart() {
		selectRangeStart(_currentPoint);
	}


	/**
	 * Set the index for the start of the range selection
	 * @param inStartIndex start index
	 */
	private void selectRangeStart(int inStartIndex)
	{
		if (inStartIndex < 0) {
			_startIndex = _endIndex = -1;
		}
		else
		{
			_startIndex = inStartIndex;
			// Move end of selection to max if necessary
			if (_endIndex <= _startIndex) {
				_endIndex = _track.getNumPoints() - 1;
			}
		}
		UpdateMessageBroker.informSubscribers();
	}


	/**
	 * Select the range up to the current point
	 */
	public void selectRangeEnd() {
		selectRangeEnd(_currentPoint);
	}


	/**
	 * Set the index for the end of the range selection
	 * @param inEndIndex end index
	 */
	public void selectRangeEnd(int inEndIndex)
	{
		if (inEndIndex < 0) {
			_startIndex = _endIndex = -1;
		}
		else
		{
			_endIndex = inEndIndex;
			// Move start of selection to min if necessary
			if (_startIndex > _endIndex || _startIndex < 0) {
				_startIndex = 0;
			}
		}
		UpdateMessageBroker.informSubscribers();
	}


	/**
	 * Modify the selection when a point has been deleted
	 * @param inPointIndex index of point which was deleted
	 */
	public void modifyPointDeleted(int inPointIndex)
	{
		// range needs to get altered if deleted point was inside or before
		if (hasRangeSelected() && inPointIndex <= _endIndex)
		{
			_endIndex--;
			if (inPointIndex < _startIndex) {
				_startIndex--;
			}
		}
		check();
	}

	/**
	 * Modify the selection when a point is inserted
	 * @param inPointIndex index of newly inserted point
	 */
	public void modifyPointInserted(int inPointIndex)
	{
		if (hasRangeSelected() && inPointIndex <= _endIndex)
		{
			_endIndex++;
			if (inPointIndex <= _startIndex) {
				_startIndex++;
			}
			check();
		}
	}

	/**
	 * Select the specified photo and point
	 * @param inPointIndex index of selected point
	 * @param inPhotoIndex index of selected photo in PhotoList
	 * @param inAudioIndex index of selected audio item
	 */
	public void selectPointPhotoAudio(int inPointIndex, int inPhotoIndex, int inAudioIndex)
	{
		_currentPoint = inPointIndex;
		_currentPhotoIndex = inPhotoIndex;
		_currentAudioIndex = inAudioIndex;
		check();
	}


	/**
	 * @return currently selected photo index
	 */
	public int getCurrentPhotoIndex() {
		return _currentPhotoIndex;
	}

	/**
	 * @return currently selected audio index
	 */
	public int getCurrentAudioIndex() {
		return _currentAudioIndex;
	}

	/**
	 * Check that the selection still makes sense
	 * and fire update message to listeners
	 */
	private void check()
	{
		if (_track != null && _track.getNumPoints() > 0)
		{
			int maxIndex = _track.getNumPoints() - 1;
			if (_currentPoint > maxIndex) {
				_currentPoint = maxIndex;
			}
			if (_endIndex > maxIndex) {
				_endIndex = maxIndex;
			}
			if (_startIndex > maxIndex) {
				_startIndex = maxIndex;
			}
		}
		else
		{
			// track is empty, clear selections
			_currentPoint = _startIndex = _endIndex = -1;
		}
		UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
	}
}
