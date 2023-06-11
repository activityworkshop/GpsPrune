package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.TrackInfo;

import java.util.ArrayList;
import java.util.List;

public class RearrangePointsCmd extends Command
{
	private final List<Integer> _indexes;

	/**
	 * Constructor
	 * @param inIndexes list of point indices
	 */
	public RearrangePointsCmd(List<Integer> inIndexes) {
		this(null, inIndexes);
	}

	/**
	 * Constructor
	 * @param inParent parent command
	 * @param inIndexes list of point indices
	 */
	protected RearrangePointsCmd(RearrangePointsCmd inParent, List<Integer> inIndexes)
	{
		super(inParent);
		_indexes = inIndexes;
	}

	/**
	 * @param inReferences list of point references
	 */
	public static RearrangePointsCmd from(List<PointReference> inReferences) {
		return new RearrangePointsCmd(convertReferencesToIntegers(inReferences));
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.DATA_EDITED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		if (getInverse() == null) {
			return false;
		}
		return inInfo.getTrack().rearrangePoints(_indexes);
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		try {
			return new RearrangePointsCmd(this, makeOppositeIndexes());
		}
		catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * @return inverse list of indexes for undo operation
	 */
	private List<Integer> makeOppositeIndexes()
	{
		Integer[] copy = new Integer[_indexes.size()];
		for (int i=0; i<_indexes.size(); i++) {
			copy[_indexes.get(i)] = i;
		}
		// Java 8 equivalent of List.of
		ArrayList<Integer> result = new ArrayList<>();
		for (int i : copy) {
			result.add(i);
		}
		return result;
	}


	/**
	 * Convert the given list of point references into a simpler list for further processing
	 * @param inRefs point references
	 * @return list of indexes
	 */
	private static List<Integer> convertReferencesToIntegers(List<PointReference> inRefs)
	{
		ArrayList<Integer> indexes = new ArrayList<>();
		for (PointReference ref : inRefs) {
			indexes.add(ref.getIndex());
		}
		return indexes;
	}
}
