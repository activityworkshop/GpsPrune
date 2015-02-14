package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a waypoint rearrangement
 */
public class UndoRearrangeWaypoints implements UndoOperation
{
	private DataPoint[] _contents = null;


	/**
	 * Constructor
	 * @param inTrack track contents to copy
	 */
	public UndoRearrangeWaypoints(Track inTrack)
	{
		_contents = inTrack.cloneContents();
	}


	/**
	 * @return description of operation
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.rearrangewaypoints");
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// restore track to previous values
		inTrackInfo.getTrack().replaceContents(_contents);
	}
}