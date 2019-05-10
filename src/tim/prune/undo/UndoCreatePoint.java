package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.TrackInfo;

/**
 * Undo creation of new point
 */
public class UndoCreatePoint implements UndoOperation
{
	/**
	 * @return description of operation
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.createpoint");
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		if (inTrackInfo.getTrack().getNumPoints() < 1)
		{
			throw new UndoException(getDescription());
		}
		// Reset selection if last point selected
		if (inTrackInfo.getSelection().getCurrentPointIndex() == (inTrackInfo.getTrack().getNumPoints()-1)) {
			inTrackInfo.getSelection().clearAll(); // Note: Informers told twice now!
		}
		// Remove last point
		inTrackInfo.getTrack().cropTo(inTrackInfo.getTrack().getNumPoints() - 1);
	}
}
