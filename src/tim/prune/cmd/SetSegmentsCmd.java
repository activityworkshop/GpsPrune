package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command to set the segment flags on one or more points
 */
public class SetSegmentsCmd extends Command
{
	private final HashMap<DataPoint, Boolean> _segmentFlags = new HashMap<>();

	/**
	 * Constructor
	 */
	public SetSegmentsCmd() {
		super(null);
	}

	/**
	 * Constructor giving list of point flags
	 */
	public SetSegmentsCmd(List<PointFlag> inPoints)
	{
		super(null);
		for (PointFlag pointFlag : inPoints) {
			addSegmentFlag(pointFlag.getPoint(), pointFlag.getFlag());
		}
	}

	/**
	 * Constructor
	 */
	private SetSegmentsCmd(SetSegmentsCmd inParent) {
		super(inParent);
	}

	public void addSegmentFlag(DataPoint inPoint) {
		addSegmentFlag(inPoint, true);
	}

	public void addSegmentFlag(DataPoint inPoint, boolean inFlag)
	{
		if (inPoint != null) {
			_segmentFlags.put(inPoint, inFlag);
		}
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.DATA_EDITED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		for (Map.Entry<DataPoint, Boolean> entry : _segmentFlags.entrySet())
		{
			final DataPoint point = entry.getKey();
			final boolean segmentFlag = entry.getValue();
			point.setSegmentStart(segmentFlag);
		}
		return !_segmentFlags.isEmpty();
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		SetSegmentsCmd undo = new SetSegmentsCmd(this);
		for (DataPoint point : _segmentFlags.keySet()) {
			undo.addSegmentFlag(point, point.getSegmentStart());
		}
		return undo;
	}
}
