package tim.prune.undo;

import tim.prune.data.TrackInfo;

/**
 * Abstract class to hold the selection handling required by all
 * Undo operations which have undeleted something
 */
public abstract class UndoDeleteOperation implements UndoOperation
{
	/** Flag to remember whether the deleted point was at the beginning or end of the selected range */
	private boolean _isAtBoundaryOfSelectedRange = false;

	/**
	 * @param inAtBoundary true if deleted point was at the beginning or end of the selected range
	 */
	public void setAtBoundaryOfSelectedRange(boolean inAtBoundary)
	{
		_isAtBoundaryOfSelectedRange = inAtBoundary;
	}

	/**
	 * Modify the current point/range selection after the delete operation is undone
	 * @param inTrackInfo track info object
	 * @param inStartIndex start index of reinserted range
	 * @param inEndIndex end index of reinserted range
	 */
	protected void modifySelection(TrackInfo inTrackInfo, int inStartIndex, int inEndIndex)
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
		// Was the deleted point at the start or end of the selected range?
		final boolean wasAtStart = numPointsInserted == 1 && inStartIndex == rangeStart && _isAtBoundaryOfSelectedRange;
		final boolean wasAtEnd   = numPointsInserted == 1 && inStartIndex == (rangeEnd+1) && _isAtBoundaryOfSelectedRange;
		if (rangeEnd >= inStartIndex && rangeEnd > rangeStart || wasAtStart || wasAtEnd)
		{
			rangeEnd += numPointsInserted;
			if (rangeStart >= inStartIndex) {
				rangeStart += numPointsInserted;
			}
			// Extend selection if the deleted point was at the start or end
			if (wasAtStart) {
				rangeStart--;
			}
			inTrackInfo.getSelection().selectRange(rangeStart, rangeEnd);
		}
	}
}
