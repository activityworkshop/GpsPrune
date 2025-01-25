package tim.prune.function;

import java.util.ArrayList;
import java.util.List;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.cmd.CompoundCommand;
import tim.prune.cmd.PointFlag;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.java8.ListUtils;

/**
 * Class to provide the function to delete the currently selected range
 */
public class DeleteSelectedRangeFunction extends DeleteBitOfTrackFunction
{
	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public DeleteSelectedRangeFunction(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.deleterange";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		// check track
		Track track = _app.getTrackInfo().getTrack();
		if (track == null || track.getNumPoints() <= 0) {
			return;
		}
		// check selection
		final int selStart = _app.getTrackInfo().getSelection().getStart();
		final int selEnd = _app.getTrackInfo().getSelection().getEnd();
		if (selStart < 0 || selEnd < 0 || selEnd <= selStart) {
			return;
		}

		ArrayList<Integer> indexesToKeep = new ArrayList<>();
		ArrayList<Integer> indexesToDelete = new ArrayList<>();
		int numMedia = fillLists(indexesToKeep, indexesToDelete, (i) -> i<selStart || i>selEnd);
		List<PointFlag> nextBreak = null;
		// Add segment break after this range if there was one within it
		if (track.isSegmentBreakWithin(selStart, selEnd))
		{
			DataPoint nextPoint = track.getNextTrackPoint(selEnd + 1);
			if (nextPoint != null) {
				nextBreak = ListUtils.makeListOfFlag(new PointFlag(nextPoint, true));
			}
		}
		CompoundCommand command = createCommand(indexesToKeep, indexesToDelete, nextBreak, numMedia);
		command.setDescription(I18nManager.getTextWithNumber("undo.deletepoints", indexesToDelete.size()));
		_app.execute(command);
	}
}
