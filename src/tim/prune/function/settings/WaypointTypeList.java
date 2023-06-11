package tim.prune.function.settings;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractListModel;

import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Track;

public class WaypointTypeList extends AbstractListModel<String>
{
	/** Sorted list of waypoint types */
	private final ArrayList<String> _types = new ArrayList<>();

	public WaypointTypeList() {
		_types.add("first one");
		_types.add("second one");
		_types.add("third one");
	}

	/**
	 * @param inTrack current track
	 */
	public void compile(Track inTrack)
	{
		_types.clear();
		final int numPoints = inTrack.getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = inTrack.getPoint(i);
			String type = point.isWaypoint() ? point.getFieldValue(Field.WAYPT_TYPE) : null;
			if (type != null && !type.isBlank() && !_types.contains(type)) {
				_types.add(type);
			}
		}
		Collections.sort(_types);
	}

	@Override
	public String getElementAt(int index) {
		return _types.get(index);
	}

	@Override
	public int getSize() {
		return _types.size();
	}
}
