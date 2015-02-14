package tim.prune.undo;

import tim.prune.data.Track;

/**
 * Operation to undo a photo rearrangement
 */
public class UndoRearrangePhotos extends UndoReorder
{
	/**
	 * Constructor
	 * @param inTrack track contents to copy
	 */
	public UndoRearrangePhotos(Track inTrack)
	{
		super(inTrack, "undo.rearrangephotos");
	}

}