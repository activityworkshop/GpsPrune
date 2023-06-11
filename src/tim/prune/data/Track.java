package tim.prune.data;

import java.util.List;

import tim.prune.UpdateMessageBroker;
import tim.prune.gui.map.MapUtils;


/**
 * Class to hold all track information,
 * including track points and waypoints
 */
public class Track
{
	// Data points
	private DataPoint[] _dataPoints = null;
	// Scaled x, y values
	private double[] _xValues = null;
	private double[] _yValues = null;
	private boolean _scaled = false;
	private int _numPoints = 0;
	private boolean _hasTrackpoint = false;
	private boolean _hasWaypoint = false;
	private FieldList _masterFieldList = null;
	// variable ranges
	private DoubleRange _latRange = null, _longRange = null;
	private DoubleRange _xRange = null, _yRange = null;


	/**
	 * Constructor for empty track
	 */
	public Track()
	{
		// create field list
		_masterFieldList = new FieldList();
		// make empty DataPoint array
		_dataPoints = new DataPoint[0];
		_numPoints = 0;
	}

	/**
	 * Constructor using fields and points from another Track
	 * @param inFieldList Field list from another Track object
	 * @param inPoints (edited) point array
	 */
	public Track(FieldList inFieldList, DataPoint[] inPoints)
	{
		_masterFieldList = inFieldList;
		_dataPoints = inPoints;
		if (_dataPoints == null) {
			_dataPoints = new DataPoint[0];
		}
		_numPoints = _dataPoints.length;
	}

	/**
	 * Request that a rescale be done to recalculate derived values
	 */
	public void requestRescale() {
		_scaled = false;
	}

	/**
	 * Extend the track's field list with the given additional fields
	 * @param inFieldList list of fields to be added
	 */
	private void extendFieldList(FieldList inFieldList) {
		_masterFieldList = _masterFieldList.merge(inFieldList);
	}

	////////////////// Modification methods //////////////////////


	/**
	 * Combine this Track with new data
	 * @param inOtherTrack other track to combine
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
		UpdateMessageBroker.informSubscribers();
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
			UpdateMessageBroker.informSubscribers();
		}
	}

	/**
	 * Delete the specified point
	 * @param inIndex point index
	 * @return true if successful
	 */
	public boolean deletePoint(int inIndex)
	{
		DataPoint point = getPoint(inIndex);
		if (point == null) {
			return false;
		}
		if (point.getSegmentStart())
		{
			DataPoint nextTrackPoint = getNextTrackPoint(inIndex+1);
			if (nextTrackPoint != null) {
				nextTrackPoint.setSegmentStart(true);
			}
		}
		DataPoint[] newPointArray = new DataPoint[_numPoints - 1];
		// Copy points before the selected point
		if (inIndex > 0) {
			System.arraycopy(_dataPoints, 0, newPointArray, 0, inIndex);
		}
		// Copy points after
		int numAfter = _numPoints - inIndex - 1;
		if (numAfter > 0) {
			System.arraycopy(_dataPoints, inIndex + 1, newPointArray, inIndex, numAfter);
		}
		// Copy points over original array
		_dataPoints = newPointArray;
		_numPoints --;
		// needs to be scaled again
		_scaled = false;
		return true;
	}

	/**
	 * Rearrange all the points in the track according to the given list
	 * @param inIndexes point indexes for new ordering
	 */
	public boolean rearrangePoints(List<Integer> inIndexes)
	{
		if (inIndexes == null || inIndexes.size() != _numPoints) {
			return false;
		}
		DataPoint[] newPointArray = new DataPoint[_numPoints];
		// Move points around
		for (int i=0; i<_numPoints; i++) {
			newPointArray[i] = getPoint(inIndexes.get(i));
		}
		// Copy array references
		_dataPoints = newPointArray;
		_scaled = false;
		return true;
	}


	//////// information methods /////////////


	/**
	 * Get the point at the given index
	 * @param inPointNum index number, starting at 0
	 * @return DataPoint object, or null if out of range
	 */
	public DataPoint getPoint(int inPointNum)
	{
		if (inPointNum > -1 && inPointNum < getNumPoints()) {
			return _dataPoints[inPointNum];
		}
		return null;
	}

	/**
	 * @return the number of (valid) points in the track
	 */
	public int getNumPoints() {
		return _numPoints;
	}

	/**
	 * @return The range of x values as a DoubleRange object
	 */
	public DoubleRange getXRange()
	{
		if (!_scaled) {scalePoints();}
		return _xRange;
	}

	/**
	 * @return The range of y values as a DoubleRange object
	 */
	public DoubleRange getYRange()
	{
		if (!_scaled) {scalePoints();}
		return _yRange;
	}

	/**
	 * @return The range of lat values as a DoubleRange object
	 */
	public DoubleRange getLatRange()
	{
		if (!_scaled) {scalePoints();}
		return _latRange;
	}
	/**
	 * @return The range of lon values as a DoubleRange object
	 */
	public DoubleRange getLonRange()
	{
		if (!_scaled) {scalePoints();}
		return _longRange;
	}

	/**
	 * @param inPointNum point index, starting at 0
	 * @return scaled x value of specified point
	 */
	public double getX(int inPointNum)
	{
		if (!_scaled) {scalePoints();}
		return _xValues[inPointNum];
	}

	/**
	 * @param inPointNum point index, starting at 0
	 * @return scaled y value of specified point
	 */
	public double getY(int inPointNum)
	{
		if (!_scaled) {scalePoints();}
		return _yValues[inPointNum];
	}

	/**
	 * @return the master field list
	 */
	public FieldList getFieldList() {
		return _masterFieldList;
	}


	/**
	 * Checks if any data exists for the specified field
	 * @param inField Field to examine
	 * @return true if data exists for this field
	 */
	public boolean hasData(Field inField) {
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
		// Loop over selected point range
		for (int i=inStart; i<=inEnd; i++)
		{
			DataPoint point = getPoint(i);
			if (point == null) {
				continue;
			}
			final boolean hasValue;
			if (inField == Field.ALTITUDE) {
				hasValue = point.hasAltitude();
			}
			else if (inField == Field.TIMESTAMP) {
				hasValue = point.hasTimestamp();
			}
			else if (inField == Field.PHOTO) {
				hasValue = point.getPhoto() != null;
			}
			else if (inField == Field.AUDIO) {
				hasValue = point.getAudio() != null;
			}
			else {
				hasValue = point.getFieldValue(inField) != null;
			}
			if (hasValue) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return true if track has altitude data
	 */
	public boolean hasAltitudeData() {
		return hasData(Field.ALTITUDE);
	}

	/**
	 * @return true if track contains at least one trackpoint
	 */
	public boolean hasTrackPoints()
	{
		if (!_scaled) {scalePoints();}
		return _hasTrackpoint;
	}

	/**
	 * @return true if track contains waypoints
	 */
	public boolean hasWaypoints()
	{
		if (!_scaled) {scalePoints();}
		return _hasWaypoint;
	}

	/**
	 * @return true if track contains any points marked for deletion
	 */
	public boolean hasMarkedPoints()
	{
		if (_numPoints < 1) {
			return false;
		}
		// Loop over points looking for any marked for deletion
		for (int i=0; i<=_numPoints-1; i++)
		{
			if (_dataPoints[i] != null && _dataPoints[i].getDeleteFlag()) {
				return true;
			}
		}
		// None found
		return false;
	}

	/**
	 * Clear all the deletion markers
	 */
	public void clearDeletionMarkers()
	{
		for (int i=0; i<_numPoints; i++) {
			_dataPoints[i].setMarkedForDeletion(false);
		}
	}

	/**
	 * Collect all the waypoints into the given List
	 * @param inList List to fill with waypoints
	 */
	public void getWaypoints(List<DataPoint> inList)
	{
		// clear list
		inList.clear();
		// loop over points and copy all waypoints into list
		for (int i=0; i<=_numPoints-1; i++)
		{
			if (_dataPoints[i] != null && _dataPoints[i].isWaypoint())
			{
				inList.add(_dataPoints[i]);
			}
		}
	}


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
				if (_dataPoints[i] == inPoint) {
					return i;
				}
			}
		}
		// not found
		return -1;
	}

	/**
	 * @return true if all the points in the track have the same source file
	 */
	public boolean hasSingleSourceFile()
	{
		SourceInfo prevInfo = null;
		for (int p=0; p < getNumPoints(); p++)
		{
			DataPoint point = getPoint(p);
			SourceInfo info = (point == null ? null : point.getSourceInfo());
			if (info == null || (prevInfo != null && info != prevInfo)) {
				return false;
			}
			if (info.getFile() == null) {
				return false;
			}
			prevInfo = info;
		}
		return true;
	}

	///////// Internal processing methods ////////////////


	/**
	 * Scale all the points in the track to gain x and y values
	 * ready for plotting
	 */
	private synchronized void scalePoints()
	{
		// Loop through all points in track, to see limits of lat, long
		_longRange = new DoubleRange();
		_latRange = new DoubleRange();
		_hasWaypoint = false; _hasTrackpoint = false;
		for (int p=0; p < getNumPoints(); p++)
		{
			DataPoint point = getPoint(p);
			if (point != null && point.isValid())
			{
				_longRange.addValue(point.getLongitude().getDouble());
				_latRange.addValue(point.getLatitude().getDouble());
				if (point.isWaypoint()) {
					_hasWaypoint = true;
				}
				else {
					_hasTrackpoint = true;
				}
			}
		}

		// Loop over points and calculate scales
		_xValues = new double[getNumPoints()];
		_yValues = new double[getNumPoints()];
		_xRange = new DoubleRange();
		_yRange = new DoubleRange();
		for (int p=0; p < getNumPoints(); p++)
		{
			DataPoint point = getPoint(p);
			if (point != null)
			{
				_xValues[p] = MapUtils.getXFromLongitude(point.getLongitude().getDouble());
				_xRange.addValue(_xValues[p]);
				_yValues[p] = MapUtils.getYFromLatitude(point.getLatitude().getDouble());
				_yRange.addValue(_yValues[p]);
			}
		}
		_scaled = true;
	}

	/**
	 * Find the nearest track point to the specified point
	 * @param inPointIndex index of point within track
	 * @return point index of nearest track point
	 */
	public int getNearestTrackPointIndex(int inPointIndex) {
		return getNearestPointIndex(_xValues[inPointIndex], _yValues[inPointIndex], -1.0, true);
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
		double mDist, yDist;
		try {
			for (int i=0; i < getNumPoints(); i++)
			{
				if (!inJustTrackPoints || !_dataPoints[i].isWaypoint())
				{
					yDist = Math.abs(_yValues[i] - inY);
					if (yDist < nearestDist || nearestDist < 0.0)
					{
						// y dist is within range, so check x too
						mDist = yDist + getMinXDist(_xValues[i] - inX);
						if (mDist < nearestDist || nearestDist < 0.0)
						{
							nearestPoint = i;
							nearestDist = mDist;
						}
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException obe) {
			return -1; // probably moving the mouse while data is changing
		}
		// Check whether it's within required distance
		if (nearestDist > inMaxDist && inMaxDist > 0.0) {
			return -1;
		}
		return nearestPoint;
	}

	/**
	 * @param inX x value of point
	 * @return minimum wrapped value
	 */
	private static double getMinXDist(double inX) {
		return Math.min(Math.min(Math.abs(inX), Math.abs(inX-1.0)), Math.abs(inX+1.0));
	}

	/**
	 * Get the next track point starting from the given index
	 * @param inStartIndex index to start looking from
	 * @return next track point, or null if end of data reached
	 */
	public DataPoint getNextTrackPoint(int inStartIndex) {
		return getNextTrackPoint(inStartIndex, _numPoints, true);
	}

	/**
	 * Get the previous track point starting from the given index
	 * @param inStartIndex index to start looking from
	 * @return next track point, or null if end of data reached
	 */
	public DataPoint getPreviousTrackPoint(int inStartIndex)
	{
		// end index is given as _numPoints but actually it just counts down to -1
		return getNextTrackPoint(inStartIndex, _numPoints, false);
	}

	/**
	 * Get the next track point starting from the given index
	 * @param inStartIndex index to start looking from
	 * @param inEndIndex index to stop looking (inclusive)
	 * @param inCountUp true for next, false for previous
	 * @return next track point, or null if end of data reached
	 */
	private DataPoint getNextTrackPoint(int inStartIndex, int inEndIndex, boolean inCountUp)
	{
		int increment = inCountUp ? 1 : -1;
		for (int i=inStartIndex; i<=inEndIndex; i+=increment)
		{
			DataPoint point = getPoint(i);
			// Exit if end of data reached - there wasn't a track point
			if (point == null) {
				return null;
			}
			if (point.isValid() && !point.isWaypoint()) {
				// next track point found
				return point;
			}
		}
		return null;
	}

	/**
	 * @param inStartIndex start index of range
	 * @param inEndIndex end index of range
	 * @return true if there are any track points in this range
	 */
	public boolean isTrackPointWithin(int inStartIndex, int inEndIndex)
	{
		for (int i = inStartIndex; i <= inEndIndex; i++)
		{
			if (!getPoint(i).isWaypoint()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param inStartIndex start index of range
	 * @param inEndIndex end index of range
	 * @return true if there are any segment breaks in this range
	 */
	public boolean isSegmentBreakWithin(int inStartIndex, int inEndIndex)
	{
		for (int i = inStartIndex; i <= inEndIndex; i++)
		{
			DataPoint point = getPoint(i);
			if (!point.isWaypoint() && point.getSegmentStart()) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Append the given point to the end of the track
	 * @param inPoint point to append
	 * @return true if successful
	 */
	public boolean appendPoint(DataPoint inPoint) {
		return insertPoint(inPoint, _numPoints);
	}

	/**
	 * Re-insert the specified point at the given index
	 * @param inPoint point to insert
	 * @param inIndex index at which to insert the point
	 * @return true if it worked, false otherwise
	 */
	public boolean insertPoint(DataPoint inPoint, int inIndex)
	{
		if (inIndex > _numPoints || inPoint == null) {
			return false;
		}
		// Make new array to copy points over to
		DataPoint[] newPointArray = new DataPoint[_numPoints + 1];
		if (inIndex > 0) {
			System.arraycopy(_dataPoints, 0, newPointArray, 0, inIndex);
		}
		newPointArray[inIndex] = inPoint;
		if (inIndex < _numPoints) {
			System.arraycopy(_dataPoints, inIndex, newPointArray, inIndex+1, _numPoints - inIndex);
		}
		// Change over to new array
		_dataPoints = newPointArray;
		_numPoints++;
		extendFieldList(inPoint.getFieldList());
		// needs to be scaled again
		_scaled = false;
		UpdateMessageBroker.informSubscribers();
		return true;
	}

	/**
	 * Append the specified point range to the end of the track
	 * @param inPoints list of points to append
	 * @return true if it worked, false otherwise
	 */
	public boolean appendRange(List<DataPoint> inPoints)
	{
		if (inPoints == null || inPoints.isEmpty()) {
			return false;
		}
		// Make new array to copy points over to
		DataPoint[] newPointArray = new DataPoint[_numPoints + inPoints.size()];
		System.arraycopy(_dataPoints, 0, newPointArray, 0, _numPoints);
		int index = _numPoints;
		for (DataPoint point : inPoints)
		{
			newPointArray[index] = point;
			extendFieldList(point.getFieldList());
			index++;
		}
		// Change over to new array
		_dataPoints = newPointArray;
		_numPoints = _dataPoints.length;
		_scaled = false;
		return true;
	}
}
