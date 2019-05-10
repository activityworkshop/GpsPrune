package tim.prune.undo;

import tim.prune.data.Track;

/**
 * Operation to undo a waypoint rearrangement
 */
public class UndoRearrangeWaypoints extends UndoReorder
{
	/**
	 * Constructor
	 * @param inTrack track contents to copy
	 */
	public UndoRearrangeWaypoints(Track inTrack)
	{
		super(inTrack, "undo.rearrangewaypoints");
	}

}