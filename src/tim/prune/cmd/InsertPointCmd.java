package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Command to insert or append a single point to the track
 */
public class InsertPointCmd extends Command
{
	private final DataPoint _point;
	private final int _pointIndex;
	private final boolean _nextSegmentFlag;


	/**
	 * Constructor
	 * @param inPoint point to insert
	 * @param inPointIndex point index to insert before, or -1 to append
	 */
	public InsertPointCmd(DataPoint inPoint, int inPointIndex) {
		this(null, inPoint, inPointIndex, false);
	}

	/**
	 * Constructor to make inverse
	 * @param inParent parent command
	 * @param inPoint point object
	 * @param inPointIndex point index of insertion
	 * @param inNextSegmentFlag segment flag of following track point, if any
	 */
	InsertPointCmd(Command inParent, DataPoint inPoint, int inPointIndex, boolean inNextSegmentFlag)
	{
		super(inParent);
		_point = inPoint;
		_pointIndex = inPointIndex;
		_nextSegmentFlag = inNextSegmentFlag;
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.DATA_ADDED_OR_REMOVED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		if (_point == null) {
			return false;
		}
		final boolean added;
		if (_pointIndex < 0) {
			added = inInfo.getTrack().appendPoint(_point);
			inInfo.selectPoint(inInfo.getTrack().getNumPoints() - 1);
		}
		else {
			added = inInfo.getTrack().insertPoint(_point, _pointIndex);
			inInfo.getSelection().modifyPointInserted(_pointIndex);
			inInfo.selectPoint(_pointIndex);
		}
		// Modify following segment flag
		if (isUndo() && added && _pointIndex >= 0)
		{
			DataPoint nextTrackPoint = inInfo.getTrack().getNextTrackPoint(_pointIndex + 1);
			if (nextTrackPoint != null) {
				nextTrackPoint.setSegmentStart(_nextSegmentFlag);
			}
		}
		return added;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo) {
		return new DeletePointCmd(this, _pointIndex);
	}
}
