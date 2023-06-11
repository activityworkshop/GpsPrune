package tim.prune.cmd;

import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;
import tim.prune.function.Describer;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to shuffle the existing points into a different order,
 * and then crop to a reduced number of points.
 * Used by for example compression, or delete rectangle
 */
public class ShuffleAndCropCmd extends CompoundCommand
{
	private final List<Integer> _indexesToDelete = new ArrayList<>();

	/**
	 * Constructor
	 * @param inIndexesToKeep list of indexes to keep
	 * @param inIndexesToDelete list of indexes to delete
	 * @param inSegmentFlags list of point flags
	 */
	public ShuffleAndCropCmd(List<Integer> inIndexesToKeep, List<Integer> inIndexesToDelete,
		List<PointFlag> inSegmentFlags)
	{
		List<Integer> totalIndexes = makeTotalIndexes(inIndexesToKeep, inIndexesToDelete);
		addCommand(new RearrangePointsCmd(totalIndexes));
		addCommand(new DeleteFinalRangeCmd(inIndexesToDelete.size()));
		if (inSegmentFlags != null && !inSegmentFlags.isEmpty()) {
			addCommand(new SetSegmentsCmd(inSegmentFlags));
		}
		_indexesToDelete.addAll(inIndexesToDelete);

		Describer confirmDescriber = new Describer("confirm.deletepoint.single", "confirm.deletepoint.multi");
		setConfirmText(confirmDescriber.getDescriptionWithCount(inIndexesToDelete.size()));
		Describer undoDescriber = new Describer("undo.deletepoint", "undo.deletepoints");
		setDescription(undoDescriber.getDescriptionWithCount(inIndexesToDelete.size()));
	}

	/**
	 * Build up a complete list of indexes, including the ones to be deleted
	 * @param inIndexesToKeep indexes to retain
	 * @param inIndexesToDelete indexes to delete
	 * @return list suitable for a full rearrangement
	 */
	private static List<Integer> makeTotalIndexes(List<Integer> inIndexesToKeep, List<Integer> inIndexesToDelete)
	{
		ArrayList<Integer> indexes = new ArrayList<>(inIndexesToKeep);
		indexes.addAll(inIndexesToDelete);
		return indexes;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		ArrayList<DataPoint> deletedPoints = new ArrayList<>();
		for (int i : _indexesToDelete) {
			deletedPoints.add(inInfo.getTrack().getPoint(i));
		}
		return makeInverse(inInfo, null, new AppendRangeCmd(deletedPoints), null);
	}
}
