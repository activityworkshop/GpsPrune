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
	private UpdateMessageBroker _broker = null;
	private int _currentPoint = -1;
	private boolean _valid = false;
	private int _startIndex = -1, _endIndex = -1;
	private int _currentPhotoIndex = -1;
	private IntegerRange _altitudeRange = null;
	private int _climb = -1, _descent = -1;
	private int _altitudeFormat = Altitude.FORMAT_NONE;
	private long _seconds = 0L;
	private double _angDistance = -1.0; //, _averageSpeed = -1.0;


	/**
	 * Constructor
	 * @param inTrack track object
	 * @param inBroker broker object
	 */
	public Selection(Track inTrack, UpdateMessageBroker inBroker)
	{
		_track = inTrack;
		_broker = inBroker;
	}


	/**
	 * Reset selection to be recalculated
	 */
	private void reset()
	{
		_valid = false;
	}


	/**
	 * Select the point at the given index
	 * @param inIndex index number of selected point
	 */
	public void selectPoint(int inIndex)
	{
		if (inIndex >= -1)
		{
			_currentPoint = inIndex;
			check();
		}
	}

	/**
	 * Select the specified point and range in one go
	 * @param inPointIndex point selection
	 * @param inStart range start
	 * @param inEnd range end
	 */
	public void select(int inPointIndex, int inStart, int inEnd)
	{
		_currentPoint = inPointIndex;
		_startIndex = inStart;
		_endIndex = inEnd;
		reset();
		check();
	}


	/**
	 * Select the previous point
	 */
	public void selectPreviousPoint()
	{
		if (_currentPoint > 0)
			selectPoint(_currentPoint - 1);
	}

	/**
	 * Select the next point
	 */
	public void selectNextPoint()
	{
		selectPoint(_currentPoint + 1);
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
		_altitudeFormat = Altitude.FORMAT_NONE;
		if (_track.getNumPoints() > 0 && hasRangeSelected())
		{
			_altitudeRange = new IntegerRange();
			_climb = 0;
			_descent = 0;
			Altitude altitude = null;
			Timestamp time = null, startTime = null, endTime = null;
			DataPoint lastPoint = null, currPoint = null;
			_angDistance = 0.0;
			int altValue = 0;
			int lastAltValue = 0;
			boolean foundAlt = false;
			for (int i=_startIndex; i<=_endIndex; i++)
			{
				currPoint = _track.getPoint(i);
				altitude = currPoint.getAltitude();
				// Ignore waypoints in altitude calculations
				if (!currPoint.isWaypoint() && altitude.isValid())
				{
					altValue = altitude.getValue(_altitudeFormat);
					if (_altitudeFormat == Altitude.FORMAT_NONE)
						_altitudeFormat = altitude.getFormat();
					_altitudeRange.addValue(altValue);
					if (foundAlt)
					{
						if (altValue > lastAltValue)
							_climb += (altValue - lastAltValue);
						else
							_descent += (lastAltValue - altValue);
					}
					lastAltValue = altValue;
					foundAlt = true;
				}
				// Store the first and last timestamp in the range
				time = currPoint.getTimestamp();
				if (time.isValid())
				{
					if (startTime == null) startTime = time;
					endTime = time;
				}
				// Calculate distances, again ignoring waypoints
				if (!currPoint.isWaypoint())
				{
					if (lastPoint != null)
					{
						_angDistance += DataPoint.calculateRadiansBetween(lastPoint, currPoint);
					}
					lastPoint = currPoint;
				}
			}
			if (endTime != null)
			{
				_seconds = endTime.getSecondsSince(startTime);
			}
			else
			{
				_seconds = 0L;
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
	 * @return the altitude format, ie feet or metres
	 */
	public int getAltitudeFormat()
	{
		return _altitudeFormat;
	}

	/**
	 * @return altitude range
	 */
	public IntegerRange getAltitudeRange()
	{
		if (!_valid) recalculate();
		return _altitudeRange;
	}


	/**
	 * @return climb
	 */
	public int getClimb()
	{
		if (!_valid) recalculate();
		return _climb;
	}

	/**
	 * @return descent
	 */
	public int getDescent()
	{
		if (!_valid) recalculate();
		return _descent;
	}


	/**
	 * @return number of seconds spanned by selection
	 */
	public long getNumSeconds()
	{
		if (!_valid) recalculate();
		return _seconds;
	}


	/**
	 * @param inUnits distance units to use, from class Distance
	 * @return distance of Selection in specified units
	 */
	public double getDistance(int inUnits)
	{
		return Distance.convertRadiansToDistance(_angDistance, inUnits);
	}


	/**
	 * Clear selected point and range
	 */
	public void clearAll()
	{
		_currentPoint = -1;
		deselectRange();
		deselectPhoto();
	}


	/**
	 * Deselect range
	 */
	public void deselectRange()
	{
		_startIndex = _endIndex = -1;
		reset();
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
	public void selectRangeStart(int inStartIndex)
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
		reset();
		_broker.informSubscribers();
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
			if (_startIndex > _endIndex || _startIndex < 0)
			{
				_startIndex = 0;
			}
		}
		reset();
		_broker.informSubscribers();
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
			reset();
		}
		check();
	}


	/**
	 * Deselect photo
	 */
	public void deselectPhoto()
	{
		_currentPhotoIndex = -1;
		check();
	}


	/**
	 * Select the specified photo and point
	 * @param inPhotoIndex index of selected photo in PhotoList
	 * @param inPointIndex index of selected point
	 */
	public void selectPhotoAndPoint(int inPhotoIndex, int inPointIndex)
	{
		_currentPhotoIndex = inPhotoIndex;
		if (inPointIndex > -1)
		{
			// select associated point, if any
			selectPoint(inPointIndex);
		}
		else
		{
			// Check if not already done
			check();
		}
	}


	/**
	 * @return currently selected photo index
	 */
	public int getCurrentPhotoIndex()
	{
		return _currentPhotoIndex;
	}


	/**
	 * Check that the selection still makes sense
	 * and fire update message to listeners
	 */
	private void check()
	{
		if (_track != null)
		{
			if (_track.getNumPoints() > 0)
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
		}
		_broker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
	}
}
