package tim.prune.cmd;

import java.util.List;

/**
 * Command to reverse a specific range of the track
 */
public class ReverseRangeCmd extends CompoundCommand
{
	public ReverseRangeCmd(List<Integer> inPointIndexes, int inStartIndex, int inEndIndex,
	                    List<PointFlag> inSegmentPoints)
	{
		addCommand(new RearrangePointsCmd(inPointIndexes));
		if (inSegmentPoints != null)
		{
			SetSegmentsCmd segmentCommand = new SetSegmentsCmd();
			for (PointFlag pf : inSegmentPoints) {
				segmentCommand.addSegmentFlag(pf.getPoint(), pf.getFlag());
			}
			addCommand(segmentCommand);
		}
	}
}
