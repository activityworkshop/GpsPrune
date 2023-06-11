package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

import java.util.ArrayList;

/**
 * Remove the end of the track, cropping it to a smaller size
 */
public class DeleteFinalRangeCmd extends Command
{
	private final int _numPointsToDelete;

	public DeleteFinalRangeCmd(int inNumToDelete) {
		this(null, inNumToDelete);
	}

	protected DeleteFinalRangeCmd(Command inParent, int inNumToDelete)
	{
		super(inParent);
		_numPointsToDelete = inNumToDelete;
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.DATA_ADDED_OR_REMOVED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		if (_numPointsToDelete < 0 || _numPointsToDelete > inInfo.getTrack().getNumPoints()) {
			return false;
		}
		inInfo.getTrack().cropTo(inInfo.getTrack().getNumPoints() - _numPointsToDelete);
		inInfo.getSelection().clearAll();
		return true;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		ArrayList<DataPoint> points = new ArrayList<>();
		final int totalPoints = inInfo.getTrack().getNumPoints();
		final int startIdx = totalPoints - _numPointsToDelete;
		for (int i=startIdx; i<totalPoints; i++) {
			points.add(inInfo.getTrack().getPoint(i));
		}
		return new AppendRangeCmd(this, points);
	}
}
