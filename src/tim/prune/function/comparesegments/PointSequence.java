package tim.prune.function.comparesegments;

import java.util.ArrayList;
import java.util.List;

import tim.prune.data.Timestamp;

/**
 * Hold a sequence of PointData objects
 * and link them forwards to each other
 */
public class PointSequence
{
	private final ArrayList<PointData> _points = new ArrayList<>();
	private PointData _lastPoint = null;

	void addPoint(PointData inPointData)
	{
		_points.add(inPointData);
		_lastPoint = inPointData;
	}

	int getNumPoints() {
		return _points.size();
	}

	PointData getFirstPoint() {
		return _points.get(0);
	}

	PointData getLastPoint() {
		return _lastPoint;
	}

	Timestamp getFirstTimestamp() {
		return _points.isEmpty() ? null : getFirstPoint()._point.getTimestamp();
	}

	List<PointData> getPoints() {
		return _points;
	}
}
