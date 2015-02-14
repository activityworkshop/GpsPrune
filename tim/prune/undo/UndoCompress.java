package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a track compression
 */
public class UndoCompress implements UndoOperation
{
	private DataPoint[] _contents = null;
	protected int _numPointsDeleted = -1;


	/**
	 * Constructor
	 * @param inTrack track contents to copy
	 */
	public UndoCompress(Track inTrack)
	{
		_contents = inTrack.cloneContents();
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
		String desc = I18nManager.getText("undo.compress");
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
		// clear selection
		inTrackInfo.getSelection().clearAll();
	}
}