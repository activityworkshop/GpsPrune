package tim.prune.cmd;

import tim.prune.data.DataPoint;
import java.util.List;

/**
 * Command to correlate either photos or audio objects
 */
public class CorrelateMediaCmd extends CompoundCommand
{
	/**
	 * Constructor
	 * @param inLinkType specifying photos, audios or both
	 * @param inPoints points to append
	 * @param inConnections links between points and media
	 */
	public CorrelateMediaCmd(MediaLinkType inLinkType, List<DataPoint> inPoints, List<PointAndMedia> inConnections)
	{
		if (!inPoints.isEmpty()) {
			addCommand(new AppendRangeCmd(inPoints));
		}
		addCommand(new ConnectMultipleMediaCmd(inLinkType, inConnections));
	}
}
