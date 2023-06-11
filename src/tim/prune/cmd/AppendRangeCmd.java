package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

import java.util.List;

/**
 * Command to append a range of points to the track
 */
public class AppendRangeCmd extends Command
{
	private final List<DataPoint> _points;

	public AppendRangeCmd(List<DataPoint> inPoints) {
		this(null, inPoints);
	}

	AppendRangeCmd(Command inParent, List<DataPoint> inPoints)
	{
		super(inParent);
		_points = inPoints;
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.DATA_ADDED_OR_REMOVED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		if (_points == null || _points.isEmpty()) {
			return false;
		}
		return inInfo.appendRange(_points);
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo) {
		return new DeleteFinalRangeCmd(this, _points.size());
	}
}
