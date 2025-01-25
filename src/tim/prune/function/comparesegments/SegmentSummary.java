package tim.prune.function.comparesegments;

import tim.prune.data.DataPoint;
import tim.prune.data.Timestamp;

/**
 * Hold a running summary of the current segment and its validity for the comparison function
 */
public class SegmentSummary
{
	private boolean _valid = true;
	private int _pointCount = 0;
	private final int _startIndex;
	private final Timestamp _startTimestamp;
	private DataPoint _latestPoint = null;
	private double _totalRadians = 0.0;


	SegmentSummary(int inStartIndex, DataPoint inPoint)
	{
		_startIndex = inStartIndex;
		_startTimestamp = inPoint == null ? null : inPoint.getTimestamp();
		addPoint(inPoint);
	}

	/** Add a point to the segment */
	public void addPoint(DataPoint inPoint)
	{
		if (inPoint == null || !inPoint.isValid() || !inPoint.hasTimestamp())
		{
			_valid = false;
			return;
		}
		if (_latestPoint != null)
		{
			_totalRadians += DataPoint.calculateRadiansBetween(_latestPoint, inPoint);
			_valid = _valid && !inPoint.getTimestamp().isBefore(_latestPoint.getTimestamp());
		}
		_latestPoint = inPoint;
		_pointCount++;
	}

	public boolean isValid() {
		return _valid && _pointCount > 1;
	}

	public int getPointCount() {
		return _pointCount;
	}

	public int getStartIndex() {
		return _startIndex;
	}

	public Timestamp getStartTimestamp() {
		return _startTimestamp;
	}

	public long getDurationInSeconds()
	{
		if (_latestPoint != null && _latestPoint.hasTimestamp()) {
			return _latestPoint.getTimestamp().getSecondsSince(_startTimestamp);
		}
		return 0L;
	}

	public double getDistanceInRadians() {
		return _totalRadians;
	}
}
