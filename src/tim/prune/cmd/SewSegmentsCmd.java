package tim.prune.cmd;

import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class to perform the segment-sewing
 */
public class SewSegmentsCmd extends CompoundCommand
{
	private final List<Integer> _indexesToDelete;

	/**
	 * Constructor
	 * @param inIndexesToKeep list of indexes to keep, in order
	 * @param inIndexesToDelete list of indexes to delete
	 * @param inSegmentFlags list of segment flags to set
	 */
	public SewSegmentsCmd(List<Integer> inIndexesToKeep, List<Integer> inIndexesToDelete,
		List<PointFlag> inSegmentFlags)
	{
		_indexesToDelete = inIndexesToDelete;
		addCommand(new RearrangePointsCmd(combine(inIndexesToKeep, inIndexesToDelete)));
		addCommand(new DeleteFinalRangeCmd(inIndexesToDelete.size()));
		addCommand(new SetSegmentsCmd(inSegmentFlags));
	}

	private List<Integer> combine(List<Integer> inIndexesToKeep, List<Integer> inIndexesToDelete)
	{
		ArrayList<Integer> allPoints = new ArrayList<>(inIndexesToKeep);
		allPoints.addAll(inIndexesToDelete);
		return allPoints;
	}

	/**
	 * @return the number of segment joins made
	 */
	public int getNumJoins() {
		return _indexesToDelete.size();
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		return makeInverse(inInfo, null,
			new AppendRangeCmd(getCommand(1), getDeletedPoints(inInfo.getTrack())), null);
	}

	private List<DataPoint> getDeletedPoints(Track inTrack)
	{
		ArrayList<DataPoint> points = new ArrayList<>();
		for (int index : _indexesToDelete) {
			points.add(inTrack.getPoint(index));
		}
		return points;
	}
}
