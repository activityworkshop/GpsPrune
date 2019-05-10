package tim.prune.function;

import tim.prune.App;
import tim.prune.data.Track;

/**
 * Class to provide the function to crop the track
 * to the current selection
 */
public class CropToSelection extends DeleteBitOfTrackFunction
{
	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public CropToSelection(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.croptrack";
	}

	/**
	 * @return name key for undo operation
	 */
	protected String getUndoNameKey() {
		return "undo.croptrack";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		// check track
		Track track = _app.getTrackInfo().getTrack();
		if (track == null || track.getNumPoints() <= 0) return;
		// check selection
		final int selStart = _app.getTrackInfo().getSelection().getStart();
		final int selEnd = _app.getTrackInfo().getSelection().getEnd();
		if (selStart < 0 || selEnd < 0 || selEnd <= selStart) return;
		// check for all selected
		if (selStart == 0 && selEnd == (track.getNumPoints() - 1)) return;

		// Pass indexes to parent class
		deleteTwoSections(0, selStart-1, selEnd+1, track.getNumPoints()-1);
	}
}
