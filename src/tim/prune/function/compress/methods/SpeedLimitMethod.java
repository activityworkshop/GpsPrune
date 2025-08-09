package tim.prune.function.compress.methods;

import tim.prune.data.DataPoint;
import tim.prune.data.MarkingData;
import tim.prune.data.SpeedValue;
import tim.prune.data.Track;
import tim.prune.data.UnitSetLibrary;
import tim.prune.function.compress.TrackDetails;

public abstract class SpeedLimitMethod extends CompressionMethod
{
	private final double _speedLimit;
	private final boolean _deleteFaster;

	public SpeedLimitMethod(double inSpeedLimit, boolean inDeleteFaster)
	{
		_speedLimit = inSpeedLimit;
		_deleteFaster = inDeleteFaster;
	}

	public String getParam() {
		return "" + Math.abs(_speedLimit);
	}

	protected double getSpeedLimit() {
		return _speedLimit;
	}

	public int compress(Track inTrack, TrackDetails inDetails, MarkingData inMarkings)
	{
		if (_speedLimit <= 0.0) {
			return 0;
		}
		int numPoints = inTrack.getNumPoints();
		// Calculate speeds using delete flags
		Double[] speeds = calculateSpeeds(inTrack, inMarkings);
		int numDeleted = 0;

		// Loop over all points looking for speed values the wrong side of limit
		for (int i=0; i<numPoints; i++)
		{
			if (inMarkings.isPointMarkedForDeletion(i)) {
				continue;
			}
			Double pointSpeed = speeds[i];
			if (pointSpeed == null) {
				continue;
			}
			final DataPoint currPoint = inTrack.getPoint(i);
			// Don't delete any waypoints or photo points, or start/end of segments
			if (!currPoint.isWaypoint() && !currPoint.hasMedia()
				&& !inDetails.isSegmentStart(i) && !inDetails.isSegmentEnd(i))
			{
				if ((_deleteFaster && pointSpeed > _speedLimit)
					|| (!_deleteFaster && pointSpeed < _speedLimit))
				{
					inMarkings.markPointForDeletion(i, true, true);
					numDeleted++;
				}
			}
		}
		return numDeleted;
	}

	private Double[] calculateSpeeds(Track inTrack, MarkingData inMarkings)
	{
		CompressionSpeedCalculator calculator = new CompressionSpeedCalculator(inMarkings);
		final int numPoints = inTrack.getNumPoints();
		Double[] results = new Double[numPoints];
		SpeedValue value = new SpeedValue();
		for (int i=0; i<numPoints; i++)
		{
			calculator.calculateHorizontalSpeed(inTrack, i, UnitSetLibrary.getMetricUnitSet(), value);
			results[i] = value.isValid() ? value.getValue() : null;
		}
		return results;
	}
}
