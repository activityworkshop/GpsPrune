package tim.prune.data;

import java.util.List;

import tim.prune.UpdateMessageBroker;
import tim.prune.edit.FieldEdit;
import tim.prune.edit.FieldEditList;


/**
 * Class to hold all track information,
 * including track points and waypoints
 */
public class Track
{
	// Broker object
	UpdateMessageBroker _broker = null;
	// Data points
	private DataPoint[] _dataPoints = null;
	// Scaled x, y values
	private double[] _xValues = null;
	private double[] _yValues = null;
	private boolean _scaled = false;
	private int _numPoints = 0;
	private boolean _mixedData = false;
	// Master field list
	private FieldList _masterFieldList = null;
	// variable ranges
	private AltitudeRange _altitudeRange = null;
	private DoubleRange _latRange = null, _longRange = null;
	private DoubleRange _xRange = null, _yRange = null;


	/**
	 * Constructor giving arrays of Fields and Objects
	 * @param inFieldArray field array
	 * @param inPointArray 2d array of field values
	 */
	public Track(UpdateMessageBroker inBroker)
	{
		_broker = inBroker;
		// create field list
		_masterFieldList = new FieldList(null);
		// make empty DataPoint array
		_dataPoints = new DataPoint[0];
		_numPoints = 0;
		// needs to be scaled
		_scaled = false;
	}


	/**
	 * Load method, for initialising and reinitialising data
	 * @param inFieldArray array of Field objects describing fields
	 * @param inPointArray 2d object array containing data
	 * @param inAltFormat altitude format
	 */
	public void load(Field[] inFieldArray, Object[][] inPointArray, int inAltFormat)
	{
		// copy field list
		_masterFieldList = new FieldList(inFieldArray);
		// make DataPoint object from each point in inPointList
		_dataPoints = new DataPoint[inPointArray.length];
		String[] dataArray = null;
		int pointIndex = 0;
		for (int p=0; p < inPointArray.length; p++)
		{
			dataArray = (String[]) inPointArray[p];
			// Convert to DataPoint objects
			DataPoint point = new DataPoint(dataArray, _masterFieldList, inAltFormat);
			if (point.isValid())
			{
				_dataPoints[pointIndex] = point;
				pointIndex++;
			}
		}
		_numPoints = pointIndex;
		// needs to be scaled
		_scaled = false;
	}


	////////////////// Modification methods //////////////////////


	/**
	 * Combine this Track with new data
	 * @param inOtherTrack
	 */
	public void combine(Track inOtherTrack)
	{
		// merge field list
		_masterFieldList = _masterFieldList.merge(inOtherTrack._masterFieldList);
		// expand data array and add other track's data points
		int totalPoints = getNumPoints() + inOtherTrack.getNumPoints();
		DataPoint[] mergedPoints = new DataPoint[totalPoints];
		System.arraycopy(_dataPoints, 0, mergedPoints, 0, getNumPoints());
		System.arraycopy(inOtherTrack._dataPoints, 0, mergedPoints, getNumPoints(), inOtherTrack.getNumPoints());
		_dataPoints = mergedPoints;
		// combine point count
		_numPoints = totalPoints;
		// needs to be scaled again
		_scaled = false;
		// inform listeners
		_broker.informSubscribers();
	}


	/**
	 * Crop the track to the given size - subsequent points are not (yet) deleted
	 * @param inNewSize new number of points in track
	 */
	public void cropTo(int inNewSize)
	{
		if (inNewSize >= 0 && inNewSize < getNumPoints())
		{
			_numPoints = inNewSize;
			// needs to be scaled again
			_scaled = false;
			_broker.informSubscribers();
		}
	}


	/**
	 * Compress the track to the given resolution
	 * @param inResolution resolution
	 * @return number of points deleted
	 */
	public int compress(int inResolution)
	{
		// (maybe should be separate thread?)
		// (maybe should be in separate class?)
		// (maybe should be based on subtended angles instead of distances?)

		if (inResolution <= 0) return 0;
		int numCopied = 0;
		// Establish range of track and minimum range between points
		scalePoints();
		double wholeScale = _xRange.getMaximum() - _xRange.getMinimum();
		double yscale = _yRange.getMaximum() - _yRange.getMinimum();
		if (yscale > wholeScale) wholeScale = yscale;
		double minDist = wholeScale / inResolution;

		// Copy selected points
		DataPoint[] newPointArray = new DataPoint[_numPoints];
		int[] pointIndices = new int[_numPoints];
		for (int i=0; i<_numPoints; i++)
		{
			boolean keepPoint = true;
			if (!_dataPoints[i].isWaypoint())
			{
				// go through newPointArray to check for range
				for (int j=0; j<numCopied && keepPoint; j++)
				{
					// calculate distance between point j and current point
					double pointDist = Math.abs(_xValues[i] - _xValues[pointIndices[j]])
					 + Math.abs(_yValues[i] - _yValues[pointIndices[j]]);
					if (pointDist < minDist)
						keepPoint = false;
				}
			}
			if (keepPoint)
			{
				newPointArray[numCopied] = _dataPoints[i];
				pointIndices[numCopied] = i;
				numCopied++;
			}
		}

		// Copy array references
		int numDeleted = _numPoints - numCopied;
		if (numDeleted > 0)
		{
			_dataPoints = new DataPoint[numCopied];
			System.arraycopy(newPointArray, 0, _dataPoints, 0, numCopied);
			_numPoints = _dataPoints.length;
			_scaled = false;
			_broker.informSubscribers();
		}
		return numDeleted;
	}


	/**
	 * Halve the track by deleting alternate points
	 * @return number of points deleted
	 */
	public int halve()
	{
		if (_numPoints < 100) return 0;
		int newSize = _numPoints / 2;
		int numDeleted = _numPoints - newSize;
		DataPoint[] newPointArray = new DataPoint[newSize];
		// Delete alternate points
		for (int i=0; i<newSize; i++)
			newPointArray[i] = _dataPoints[i*2];
		// Copy array references
		_dataPoints = newPointArray;
		_numPoints = _dataPoints.length;
		_scaled = false;
		_broker.informSubscribers();
		return numDeleted;
	}


	/**
	 * Delete the specified point
	 * @return true if successful
	 */
	public boolean deletePoint(int inIndex)
	{
		boolean answer = deleteRange(inIndex, inIndex);
		return answer;
	}


	/**
	 * Delete the specified range of points from the Track
	 * @param inStart start of range (inclusive)
	 * @param inEnd end of range (inclusive)
	 * @return true if successful
	 */
	public boolean deleteRange(int inStart, int inEnd)
	{
		// TODO: Check for deleting photos?
		if (inStart < 0 || inEnd < 0 || inEnd < inStart)
		{
			// no valid range selected so can't delete
			return false;
		}
		// valid range, let's delete it
		int numToDelete = inEnd - inStart + 1;
		DataPoint[] newPointArray = new DataPoint[_numPoints - numToDelete];
		// Copy points before the selected range
		if (inStart > 0)
		{
			System.arraycopy(_dataPoints, 0, newPointArray, 0, inStart);
		}
		// Copy points after the deleted one(s)
		if (inEnd < (_numPoints - 1))
		{
			System.arraycopy(_dataPoints, inEnd + 1, newPointArray, inStart,
				_numPoints - inEnd - 1);
		}
		// Copy points over original array (careful!)
		_dataPoints = newPointArray;
		_numPoints -= numToDelete;
		// needs to be scaled again
		_scaled = false;
		return true;
	}


	/**
	 * Delete all the duplicate points in the track
	 * @return number of points deleted
	 */
	public int deleteDuplicates()
	{
		// loop through Track counting duplicates first
		boolean[] dupes = new boolean[_numPoints];
		int numDupes = 0;
		int i, j;
		for (i=1; i<_numPoints; i++)
		{
			DataPoint p1 = _dataPoints[i];
			// Loop through all points before this one
			for (j=0; j<i && !dupes[i]; j++)
			{
				DataPoint p2 = _dataPoints[j];
				if (p1.isDuplicate(p2))
				{
					dupes[i] = true;
					numDupes++;
				}
			}
		}
		if (numDupes > 0)
		{
			// Make new resized array and copy DataPoints over
			DataPoint[] newPointArray = new DataPoint[_numPoints - numDupes];
			j = 0;
			for (i=0; i<_numPoints; i++)
			{
				if (!dupes[i])
				{
					newPointArray[j] = _dataPoints[i];
					j++;
				}
			}
			// Copy array references
			_dataPoints = newPointArray;
			_numPoints = _dataPoints.length;
			_scaled = false;
			_broker.informSubscribers();
		}
		return numDupes;
	}


	/**
	 * Reverse the specified range of points
	 * @return true if successful, false otherwise
	 */
	public boolean reverseRange(int inStart, int inEnd)
	{
		if (inStart < 0 || inEnd < 0 || inStart >= inEnd || inEnd >= _numPoints)
		{
			return false;
		}
		// calculate how many point swaps are required
		int numPointsToReverse = (inEnd - inStart + 1) / 2;
		DataPoint p = null;
		for (int i=0; i<numPointsToReverse; i++)
		{
			// swap pairs of points
			p = _dataPoints[inStart + i];
			_dataPoints[inStart + i] = _dataPoints[inEnd - i];
			_dataPoints[inEnd - i] = p;
		}
		// needs to be scaled again
		_scaled = false;
		_broker.informSubscribers();
		return true;
	}


	/**
	 * Collect all waypoints to the start or end of the track
	 * @param inAtStart true to collect at start, false for end
	 * @return true if successful, false if no change
	 */
	public boolean collectWaypoints(boolean inAtStart)
	{
		// Check for mixed data, numbers of waypoints & nons
		int numWaypoints = 0, numNonWaypoints = 0;
		boolean wayAfterNon = false, nonAfterWay = false;
		DataPoint[] waypoints = new DataPoint[_numPoints];
		DataPoint[] nonWaypoints = new DataPoint[_numPoints];
		DataPoint point = null;
		for (int i=0; i<_numPoints; i++)
		{
			point = _dataPoints[i];
			if (point.isWaypoint())
			{
				waypoints[numWaypoints] = point;
				numWaypoints++;
				wayAfterNon |= (numNonWaypoints > 0);
			}
			else
			{
				nonWaypoints[numNonWaypoints] = point;
				numNonWaypoints++;
				nonAfterWay |= (numWaypoints > 0);
			}
		}
		// Exit if the data is already in the specified order
		if (numWaypoints == 0 || numNonWaypoints == 0
			|| (inAtStart && !wayAfterNon && nonAfterWay)
			|| (!inAtStart && wayAfterNon && !nonAfterWay))
		{
			return false;
		}

		// Copy the arrays back into _dataPoints in the specified order
		if (inAtStart)
		{
			System.arraycopy(waypoints, 0, _dataPoints, 0, numWaypoints);
			System.arraycopy(nonWaypoints, 0, _dataPoints, numWaypoints, numNonWaypoints);
		}
		else
		{
			System.arraycopy(nonWaypoints, 0, _dataPoints, 0, numNonWaypoints);
			System.arraycopy(waypoints, 0, _dataPoints, numNonWaypoints, numWaypoints);
		}
		// needs to be scaled again
		_scaled = false;
		_broker.informSubscribers();
		return true;
	}


	/**
	 * Interleave all waypoints by each nearest track point
	 * @return true if successful, false if no change
	 */
	public boolean interleaveWaypoints()
	{
		// Separate waypoints and find nearest track point
		int numWaypoints = 0;
		DataPoint[] waypoints = new DataPoint[_numPoints];
		int[] pointIndices = new int[_numPoints];
		DataPoint point = null;
		int i = 0;
		for (i=0; i<_numPoints; i++)
		{
			point = _dataPoints[i];
			if (point.isWaypoint())
			{
				waypoints[numWaypoints] = point;
				pointIndices[numWaypoints] = getNearestPointIndex(
					_xValues[i], _yValues[i], -1.0, true);
				numWaypoints++;
			}
		}
		// Exit if data not mixed
		if (numWaypoints == 0 || numWaypoints == _numPoints)
			return false;

		// Loop round points copying to correct order
		DataPoint[] dataCopy = new DataPoint[_numPoints];
		int copyIndex = 0;
		for (i=0; i<_numPoints; i++)
		{
			point = _dataPoints[i];
			// if it's a track point, copy it
			if (!point.isWaypoint())
			{
				dataCopy[copyIndex] = point;
				copyIndex++;
			}
			// check for waypoints with this index
			for (int j=0; j<numWaypoints; j++)
			{
				if (pointIndices[j] == i)
				{
					dataCopy[copyIndex] = waypoints[j];
					copyIndex++;
				}
			}
		}
		// Copy data back to track
		_dataPoints = dataCopy;
		// needs to be scaled again to recalc x, y
		_scaled = false;
		_broker.informSubscribers();
		return true;
	}

	// TODO: Need to rearrange photo points too?

	/**
	 * Interpolate extra points between two selected ones
	 * @param inStartIndex start index of interpolation
	 * @param inNumPoints num points to insert
	 * @return true if successful
	 */
	public boolean interpolate(int inStartIndex, int inNumPoints)
	{
		// check parameters
		if (inStartIndex < 0 || inStartIndex >= _numPoints || inNumPoints <= 0)
			return false;

		// get start and end points
		DataPoint startPoint = getPoint(inStartIndex);
		DataPoint endPoint = getPoint(inStartIndex + 1);

		// Make array of points to insert
		DataPoint[] insertedPoints = startPoint.interpolate(endPoint, inNumPoints);

		// Insert points into track
		return insertRange(insertedPoints, inStartIndex + 1);
	}


	/**
	 * Append the specified points to the end of the track
	 * @param inPoints DataPoint objects to add
	 */
	public void appendPoints(DataPoint[] inPoints)
	{
		// Insert points into track
		if (inPoints != null && inPoints.length > 0)
		{
			insertRange(inPoints, _numPoints);
		}
		// needs to be scaled again to recalc x, y
		_scaled = false;
		_broker.informSubscribers();
	}


	//////// information methods /////////////


	/**
	 * Get the point at the given index
	 * @param inPointNum index number, starting at 0
	 * @return DataPoint object, or null if out of range
	 */
	public DataPoint getPoint(int inPointNum)
	{
		if (inPointNum > -1 && inPointNum < getNumPoints())
		{
			return _dataPoints[inPointNum];
		}
		return null;
	}


	/**
	 * @return altitude range of points as AltitudeRange object
	 */
	public AltitudeRange getAltitudeRange()
	{
		if (!_scaled) scalePoints();
		return _altitudeRange;
	}
	/**
	 * @return the number of (valid) points in the track
	 */
	public int getNumPoints()
	{
		return _numPoints;
	}

	/**
	 * @return The range of x values as a DoubleRange object
	 */
	public DoubleRange getXRange()
	{
		if (!_scaled) scalePoints();
		return _xRange;
	}

	/**
	 * @return The range of y values as a DoubleRange object
	 */
	public DoubleRange getYRange()
	{
		if (!_scaled) scalePoints();
		return _yRange;
	}

	/**
	 * @param inPointNum point index, starting at 0
	 * @return scaled x value of specified point
	 */
	public double getX(int inPointNum)
	{
		if (!_scaled) scalePoints();
		return _xValues[inPointNum];
	}

	/**
	 * @param inPointNum point index, starting at 0
	 * @return scaled y value of specified point
	 */
	public double getY(int inPointNum)
	{
		if (!_scaled) scalePoints();
		return _yValues[inPointNum];
	}

	/**
	 * @return the master field list
	 */
	public FieldList getFieldList()
	{
		return _masterFieldList;
	}


	/**
	 * Checks if any data exists for the specified field
	 * @param inField Field to examine
	 * @return true if data exists for this field
	 */
	public boolean hasData(Field inField)
	{
		return hasData(inField, 0, _numPoints-1);
	}


	/**
	 * Checks if any data exists for the specified field in the specified range
	 * @param inField Field to examine
	 * @param inStart start of range to check
	 * @param inEnd end of range to check (inclusive)
	 * @return true if data exists for this field
	 */
	public boolean hasData(Field inField, int inStart, int inEnd)
	{
		for (int i=inStart; i<=inEnd; i++)
		{
			if (_dataPoints[i].getFieldValue(inField) != null)
			{
				return true;
			}
		}
		return false;
	}


	/**
	 * @return true if track contains waypoints and trackpoints
	 */
	public boolean hasMixedData()
	{
		if (!_scaled) scalePoints();
		return _mixedData;
	}


	/**
	 * Collect all the waypoints into the given List
	 * @param inList List to fill with waypoints
	 */
	public void getWaypoints(List inList)
	{
		// clear list
		inList.clear();
		// loop over points and copy all waypoints into list
		for (int i=0; i<=_numPoints-1; i++)
		{
			if (_dataPoints[i].isWaypoint())
			{
				inList.add(_dataPoints[i]);
			}
		}
	}
	// TODO: Make similar method to get list of photos


	/**
	 * Search for the given Point in the track and return the index
	 * @param inPoint Point to look for
	 * @return index of Point, if any or -1 if not found
	 */
	public int getPointIndex(DataPoint inPoint)
	{
		if (inPoint != null)
		{
			// Loop over points in track
			for (int i=0; i<=_numPoints-1; i++)
			{
				if (_dataPoints[i] == inPoint)
				{
					return i;
				}
			}
		}
		// not found
		return -1;
	}


	///////// Internal processing methods ////////////////


	/**
	 * Scale all the points in the track to gain x and y values
	 * ready for plotting
	 */
	private void scalePoints()
	{
		// Loop through all points in track, to see limits of lat, long and altitude
		_longRange = new DoubleRange();
		_latRange = new DoubleRange();
		_altitudeRange = new AltitudeRange();
		int p;
		boolean hasWaypoint = false, hasTrackpoint = false;
		for (p=0; p < getNumPoints(); p++)
		{
			DataPoint point = getPoint(p);
			if (point != null && point.isValid())
			{
				_longRange.addValue(point.getLongitude().getDouble());
				_latRange.addValue(point.getLatitude().getDouble());
				if (point.getAltitude().isValid())
				{
					_altitudeRange.addValue(point.getAltitude());
				}
				if (point.isWaypoint())
					hasWaypoint = true;
				else
					hasTrackpoint = true;
			}
		}
		_mixedData = hasWaypoint && hasTrackpoint;

		// Use medians to centre at 0
		double longMedian = (_longRange.getMaximum() + _longRange.getMinimum()) / 2.0;
		double latMedian = (_latRange.getMaximum() + _latRange.getMinimum()) / 2.0;
		double longFactor = Math.cos(latMedian / 180.0 * Math.PI); // Function of median latitude

		// Loop over points and calculate scales
		_xValues = new double[getNumPoints()];
		_yValues = new double[getNumPoints()];
		_xRange = new DoubleRange();
		_yRange = new DoubleRange();
		for (p=0; p < getNumPoints(); p++)
		{
			DataPoint point = getPoint(p);
			if (point != null)
			{
				_xValues[p] = (point.getLongitude().getDouble() - longMedian) * longFactor;
				_xRange.addValue(_xValues[p]);
				_yValues[p] = (point.getLatitude().getDouble() - latMedian);
				_yRange.addValue(_yValues[p]);
			}
		}
		_scaled = true;
	}


	/**
	 * Find the nearest point to the specified x and y coordinates
	 * or -1 if no point is within the specified max distance
	 * @param inX x coordinate
	 * @param inY y coordinate
	 * @param inMaxDist maximum distance from selected coordinates
	 * @param inJustTrackPoints true if waypoints should be ignored
	 * @return index of nearest point or -1 if not found
	 */
	public int getNearestPointIndex(double inX, double inY, double inMaxDist, boolean inJustTrackPoints)
	{
		int nearestPoint = 0;
		double nearestDist = -1.0;
		double currDist;
		for (int i=0; i < getNumPoints(); i++)
		{
			if (!inJustTrackPoints || !_dataPoints[i].isWaypoint())
			{
				currDist = Math.abs(_xValues[i] - inX) + Math.abs(_yValues[i] - inY);
				if (currDist < nearestDist || nearestDist < 0.0)
				{
					nearestPoint = i;
					nearestDist = currDist;
				}
			}
		}
		// Check whether it's within required distance
		if (nearestDist > inMaxDist && inMaxDist > 0.0)
		{
			return -1;
		}
		return nearestPoint;
	}


	////////////////// Cloning and replacing ///////////////////

	/**
	 * Clone the array of DataPoints
	 * @return shallow copy of DataPoint objects
	 */
	public DataPoint[] cloneContents()
	{
		DataPoint[] clone = new DataPoint[getNumPoints()];
		System.arraycopy(_dataPoints, 0, clone, 0, getNumPoints());
		return clone;
	}


	/**
	 * Clone the specified range of data points
	 * @param inStart start index (inclusive)
	 * @param inEnd end index (inclusive)
	 * @return shallow copy of DataPoint objects
	 */
	public DataPoint[] cloneRange(int inStart, int inEnd)
	{
		int numSelected = 0;
		if (inEnd >= 0 && inEnd >= inStart)
		{
			numSelected = inEnd - inStart + 1;
		}
		DataPoint[] result = new DataPoint[numSelected>0?numSelected:0];
		if (numSelected > 0)
		{
			System.arraycopy(_dataPoints, inStart, result, 0, numSelected);
		}
		return result;
	}


	/**
	 * Re-insert the specified point at the given index
	 * @param inPoint point to insert
	 * @param inIndex index at which to insert the point
	 * @return true if it worked, false otherwise
	 */
	public boolean insertPoint(DataPoint inPoint, int inIndex)
	{
		if (inIndex > _numPoints || inPoint == null)
		{
			return false;
		}
		// Make new array to copy points over to
		DataPoint[] newPointArray = new DataPoint[_numPoints + 1];
		if (inIndex > 0)
		{
			System.arraycopy(_dataPoints, 0, newPointArray, 0, inIndex);
		}
		newPointArray[inIndex] = inPoint;
		if (inIndex < _numPoints)
		{
			System.arraycopy(_dataPoints, inIndex, newPointArray, inIndex+1, _numPoints - inIndex);
		}
		// Change over to new array
		_dataPoints = newPointArray;
		_numPoints++;
		// needs to be scaled again
		_scaled = false;
		_broker.informSubscribers();
		return true;
	}


	/**
	 * Re-insert the specified point range at the given index
	 * @param inPoints point array to insert
	 * @param inIndex index at which to insert the points
	 * @return true if it worked, false otherwise
	 */
	public boolean insertRange(DataPoint[] inPoints, int inIndex)
	{
		if (inIndex > _numPoints || inPoints == null)
		{
			return false;
		}
		// Make new array to copy points over to
		DataPoint[] newPointArray = new DataPoint[_numPoints + inPoints.length];
		if (inIndex > 0)
		{
			System.arraycopy(_dataPoints, 0, newPointArray, 0, inIndex);
		}
		System.arraycopy(inPoints, 0, newPointArray, inIndex, inPoints.length);
		if (inIndex < _numPoints)
		{
			System.arraycopy(_dataPoints, inIndex, newPointArray, inIndex+inPoints.length, _numPoints - inIndex);
		}
		// Change over to new array
		_dataPoints = newPointArray;
		_numPoints += inPoints.length;
		// needs to be scaled again
		_scaled = false;
		_broker.informSubscribers();
		return true;
	}


	/**
	 * Replace the track contents with the given point array
	 * @param inContents array of DataPoint objects
	 */
	public boolean replaceContents(DataPoint[] inContents)
	{
		// master field array stays the same
		// (would need to store field array too if we wanted to redo a load)
		// replace data array
		_dataPoints = inContents;
		_numPoints = _dataPoints.length;
		_scaled = false;
		_broker.informSubscribers();
		return true;
	}


	/**
	 * Edit the specified point
	 * @param inPoint point to edit
	 * @param inEditList list of edits to make
	 * @return true if successful
	 */
	public boolean editPoint(DataPoint inPoint, FieldEditList inEditList)
	{
		if (inPoint != null && inEditList != null && inEditList.getNumEdits() > 0)
		{
			// go through edits one by one
			int numEdits = inEditList.getNumEdits();
			for (int i=0; i<numEdits; i++)
			{
				FieldEdit edit = inEditList.getEdit(i);
				inPoint.setFieldValue(edit.getField(), edit.getValue());
			}
			// possibly needs to be scaled again
			_scaled = false;
			// trigger listeners
			_broker.informSubscribers();
			return true;
		}
		return false;
	}
}
