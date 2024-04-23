package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

public class DeletePointCmd extends Command
{
	private final int _pointIndex;

	public DeletePointCmd(int inPointIndex) {
		this(null, inPointIndex);
	}

	protected DeletePointCmd(Command inParent, int inPointIndex)
	{
		super(inParent);
		_pointIndex = inPointIndex;
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.DATA_ADDED_OR_REMOVED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		final int pointIndex = (_pointIndex < 0 ? inInfo.getTrack().getNumPoints() - 1 : _pointIndex);
		return inInfo.deletePoint(pointIndex);
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		DataPoint pointToDelete = inInfo.getTrack().getPoint(_pointIndex);
		DataPoint nextTrackPoint = inInfo.getTrack().getNextTrackPoint(_pointIndex + 1);
		boolean nextSegmentFlag = nextTrackPoint != null && nextTrackPoint.getSegmentStart();
		return new InsertPointCmd(this, pointToDelete, _pointIndex, nextSegmentFlag);
	}
}
