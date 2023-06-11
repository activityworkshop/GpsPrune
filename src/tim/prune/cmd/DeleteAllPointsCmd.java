package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

import java.util.ArrayList;

/**
 * Remove all the points in the track
 */
public class DeleteAllPointsCmd extends Command
{
	public DeleteAllPointsCmd() {
		super(null);
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.DATA_ADDED_OR_REMOVED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		if (inInfo.getTrack().getNumPoints() == 0) {
			return false;
		}
		inInfo.getTrack().cropTo(0);
		inInfo.getSelection().clearAll();
		return true;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		ArrayList<DataPoint> points = new ArrayList<>();
		final int totalPoints = inInfo.getTrack().getNumPoints();
		for (int i=0; i<totalPoints; i++) {
			points.add(inInfo.getTrack().getPoint(i));
		}
		return new AppendRangeCmd(this, points);
	}
}
