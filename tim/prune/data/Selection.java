package tim.prune.data;

import tim.prune.DataSubscriber;
import tim.prune.UpdateMessageBroker;

/**
 * Class to represent a selected portion of a Track
 * and its properties
 */
public class Selection
{
	private Track _track = null;
	private int _currentPoint = -1;
	private boolean _valid = false;
	private int _prevNumPoints = 0;
	private int _startIndex = -1, _endIndex = -1;
	private int _currentPhotoIndex = -1;
	private int _currentAudioIndex = -1;
	private AltitudeRange _altitudeRange = null;
	private long _movingMilliseconds = 0L;
	private double _angMovingDistance = -1.0;


	/**
	 * Constructor
	 * @param inTrack track object
	 */
	public Selection(Track inTrack)
	{
		_track = inTrack;
	}


	/**
	 * Mark selection invalid so it will be recalculated
	 */
	public void markInvalid()
	{
		_valid = false;
	}


	/**
	 * @return the current point index
	 */
	public int getCurrentPointIndex()
	{
		return _currentPoint;
	}


	/**
	 * @return true if range is selected
	 */
	public boolean hasRangeSelected()
	{
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
		if (numPoints > 0 && hasRangeSelected())
		{
			_altitudeRange = new AltitudeRange();
			Altitude altitude = null;
			Timestamp time = null, previousTime = null;
			DataPoint lastPoint = null, currPoint = null;
			_angMovingDistance = 0.0;
			_movingMilliseconds = 0L;
			// Loop over points in selection
			for (int i=_startIndex; i<=_endIndex; i++)
			{
				currPoint = _track.getPoint(i);
				altitude = currPoint.getAltitude();
				// Ignore waypoints in altitude calculations
				if (!currPoint.isWaypoint() && altitude.isValid())
				{
					if (currPoint.getSegmentStart()) {
						_altitudeRange.ignoreValue(altitude);
					}
					else {
						_altitudeRange.addValue(altitude);
					}
				}
				// Compare timestamps within the segments
				time = currPoint.getTimestamp();
				if (time.isValid())
				{
					// add moving time
					if (!currPoint.getSegmentStart() && previousTime != null && time.isAfter(previousTime)) {
						_movingMilliseconds += time.getMillisecondsSince(previousTime);
					}
					previousTime = time;
				}
				// Calculate distances, again ignoring waypoints
				if (!currPoint.isWaypoint())
				{
					if (lastPoint != null)
					{
						double radians = DataPoint.calculateRadiansBetween(lastPoint, currPoint);
						if (!currPoint.getSegmentStart()) {
							_angMovingDistance += radians;
						}
					}
					lastPoint = currPoint;
				}
			}
		}
		_valid = true;
	}


	/**
	 * @return start index
	 */
	public int getStart()
	{
		if (!_valid) recalculate();
		return _startIndex;
	}


	/**
	 * @return end index
	 */
	public int getEnd()
	{
		if (!_valid) recalculate();
		return _endIndex;
	}

	/**
	 * @return altitude range
	 */
	public AltitudeRange getAltitudeRange()
	{
		if (!_valid) recalculate();
		return _altitudeRange;
	}


	/**
	 * @return number of seconds spanned by segments within selection
	 */
	public long getMovingSeconds()
	{
		if (!_valid) recalculate();
		return _movingMilliseconds / 1000L;
	}

	/**
	 * @return moving distance of Selection in current units
	 */
	public double getMovingDistance()
	{
		return Distance.convertRadiansToDistance(_angMovingDistance);
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
		markInvalid();
		check();
	}


	/**
	 * Select the range from the current point
	 */
	public void selectRangeStart()
	{
		selectRangeStart(_currentPoint);
	}


	/**
	 * Set the index for the start of the range selection
	 * @param inStartIndex start index
	 */
	private void selectRangeStart(int inStartIndex)
	{
		if (inStartIndex < 0)
		{
			_startIndex = _endIndex = -1;
		}
		else
		{
			_startIndex = inStartIndex;
			// Move end of selection to max if necessary
			if (_endIndex <= _startIndex)
			{
				_endIndex = _track.getNumPoints() - 1;
			}
		}
		markInvalid();
		UpdateMessageBroker.informSubscribers();
	}


	/**
	 * Select the range up to the current point
	 */
	public void selectRangeEnd()
	{
		selectRangeEnd(_currentPoint);
	}


	/**
	 * Set the index for the end of the range selection
	 * @param inEndIndex end index
	 */
	public void selectRangeEnd(int inEndIndex)
	{
		if (inEndIndex < 0)
		{
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
		markInvalid();
		UpdateMessageBroker.informSubscribers();
	}


	/**
	 * Modify the selection given that the selected range has been deleted
	 */
	public void modifyRangeDeleted()
	{
		// Modify current point, if any
		if (_currentPoint > _endIndex)
		{
			_currentPoint -= (_endIndex - _startIndex);
		}
		else if (_currentPoint > _startIndex)
		{
			_currentPoint = _startIndex;
		}
		// Clear selected range
		_startIndex = _endIndex = -1;
		// Check for consistency and fire update
		check();
	}


	/**
	 * Modify the selection when a point is deleted
	 */
	public void modifyPointDeleted()
	{
		// current point index doesn't change, just gets checked
		// range needs to get altered if deleted point is inside or before
		if (hasRangeSelected() && _currentPoint <= _endIndex)
		{
			_endIndex--;
			if (_currentPoint < _startIndex)
				_startIndex--;
			markInvalid();
		}
		check();
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
	public int getCurrentPhotoIndex()
	{
		return _currentPhotoIndex;
	}

	/**
	 * @return currently selected audio index
	 */
	public int getCurrentAudioIndex()
	{
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
			if (_currentPoint > maxIndex)
			{
				_currentPoint = maxIndex;
			}
			if (_endIndex > maxIndex)
			{
				_endIndex = maxIndex;
			}
			if (_startIndex > maxIndex)
			{
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
