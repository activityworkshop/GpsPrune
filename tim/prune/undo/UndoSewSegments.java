package tim.prune.undo;

import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo the sewing together of track segments
 */
public class UndoSewSegments extends UndoReorder
{
	/** All segment start flags need to be remembered as well */
	private boolean[] _segmentStartFlags = null;

	/**
	 * Constructor
	 * @param inTrack track contents to copy
	 */
	public UndoSewSegments(Track inTrack)
	{
		super(inTrack, "undo.sewsegments");
		// Also remember segment start flags, as they may have been changed by reversals
		final int numPoints = inTrack.getNumPoints();
		_segmentStartFlags = new boolean[numPoints];
		for (int i=0; i<numPoints; i++) {
			_segmentStartFlags[i] = inTrack.getPoint(i).getSegmentStart();
		}
	}

	/** Perform the undo */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Put all the points back in the right order
		super.performUndo(inTrackInfo);
		// And then restore the segment flags
		for (int i=0; i<_segmentStartFlags.length; i++)
		{
			DataPoint point = inTrackInfo.getTrack().getPoint(i);
			if (point != null && !point.isWaypoint()) {
				point.setSegmentStart(_segmentStartFlags[i]);
			}
		}
	}
}
