package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.Track;

/**
 * Undo splitting of track segments
 */
public class UndoSplitSegments extends UndoMergeTrackSegments
{
	/** Constructor */
	public UndoSplitSegments(Track inTrack) {
		super(inTrack, 0, inTrack.getNumPoints()-1);
	}

	/**
	 * @return description of operation
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.splitsegments");
	}
}
