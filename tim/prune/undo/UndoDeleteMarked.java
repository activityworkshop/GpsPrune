package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo the deletion of marked points
 */
public class UndoDeleteMarked implements UndoOperation
{
	private DataPoint[] _contents = null;
	protected int _numPointsDeleted = -1;
	private boolean[] _segmentStarts = null;


	/**
	 * Constructor
	 * @param inTrack track contents to copy
	 */
	public UndoDeleteMarked(Track inTrack)
	{
		_contents = inTrack.cloneContents();
		// Copy boolean segment start flags
		_segmentStarts = new boolean[inTrack.getNumPoints()];
		for (int i=0; i<inTrack.getNumPoints(); i++) {
			_segmentStarts[i] = inTrack.getPoint(i).getSegmentStart();
		}
	}


	/**
	 * Set the number of points deleted
	 * (only known after attempted compression)
	 * @param inNum number of points deleted
	 */
	public void setNumPointsDeleted(int inNum)
	{
		_numPointsDeleted = inNum;
	}


	/**
	 * @return description of operation including parameters
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.deletemarked");
		if (_numPointsDeleted > 0)
			desc = desc + " (" + _numPointsDeleted + ")";
		return desc;
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// restore track to previous values
		inTrackInfo.getTrack().replaceContents(_contents);
		// Copy boolean segment start flags
		Track track = inTrackInfo.getTrack();
		if (_segmentStarts.length != track.getNumPoints())
			throw new UndoException("Cannot undo delete - track length no longer matches");
		for (int i=0; i<_segmentStarts.length; i++) {
			track.getPoint(i).setSegmentStart(_segmentStarts[i]);
		}
		// clear selection
		inTrackInfo.getSelection().clearAll();
	}
}