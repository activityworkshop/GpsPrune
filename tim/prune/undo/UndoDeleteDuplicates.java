package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.Track;

/**
 * Undo duplicate deletion
 */
public class UndoDeleteDuplicates extends UndoCompress
{

	/**
	 * Constructor
	 * @param inTrack Track object
	 */
	public UndoDeleteDuplicates(Track inTrack)
	{
		super(inTrack);
	}


	/**
	 * @return description of operation including parameters
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.deleteduplicates");
		if (_numPointsDeleted > 0)
			desc = desc + " (" + _numPointsDeleted + ")";
		return desc;
	}
}
