package tim.prune.cmd;

import java.util.List;

/**
 * Command to cut and move a section of the track
 */
public class CutAndMoveCmd extends CompoundCommand
{
	/**
	 * Constructor
	 * @param inIndexes list of indexes to reorder the track
	 * @param inSegmentFlags list of segment flags to set
	 */
	public CutAndMoveCmd(List<Integer> inIndexes, List<PointFlag> inSegmentFlags)
	{
		addCommand(new RearrangePointsCmd(inIndexes));
		addCommand(new SetSegmentsCmd(inSegmentFlags));
	}
}
