package tim.prune.undo;

import tim.prune.data.TrackInfo;

/**
 * Abstract class to hold the selection handling required by all
 * Undo operations which have undeleted something
 */
public abstract class UndoDeleteOperation implements UndoOperation
{
	/**
	 * Modify the current point/range selection after the delete operation is undone
	 * @param inTrackInfo track info object
	 * @param inStartIndex start index of reinserted range
	 * @param inEndIndex end index of reinserted range
	 */
	protected static void modifySelection(TrackInfo inTrackInfo, int inStartIndex, int inEndIndex)
	{
		final int numPointsInserted = inEndIndex - inStartIndex + 1;
		// See if there is a currently selected point, if so does it need to be modified
		final int currentPoint = inTrackInfo.getSelection().getCurrentPointIndex();
		if (currentPoint >= inStartIndex)
		{
			inTrackInfo.selectPoint(currentPoint + numPointsInserted);
		}
		// Same for currently selected range
		int rangeStart = inTrackInfo.getSelection().getStart();
		int rangeEnd   = inTrackInfo.getSelection().getEnd();
		if (rangeEnd >= inStartIndex && rangeEnd > rangeStart)
		{
			rangeEnd += numPointsInserted;
			if (rangeStart >= inStartIndex) {
				rangeStart += numPointsInserted;
			}
			inTrackInfo.getSelection().selectRange(rangeStart, rangeEnd);
		}
	}
}
