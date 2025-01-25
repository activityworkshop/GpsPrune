package tim.prune.function;

import java.util.ArrayList;
import java.util.List;

import tim.prune.App;
import tim.prune.cmd.Command;
import tim.prune.cmd.PointFlag;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.java8.ListUtils;

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
	public CropToSelection(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.croptrack";
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
		// check for all selected
		if (selStart == 0 && selEnd == (track.getNumPoints() - 1)) {
			return;
		}

		ArrayList<Integer> indexesToKeep = new ArrayList<>();
		ArrayList<Integer> indexesToDelete = new ArrayList<>();
		int numMedia = fillLists(indexesToKeep, indexesToDelete, (i) -> i>=selStart && i<=selEnd);
		List<PointFlag> breakList = null;
		// Add segment break to the first track point within the selected range
		DataPoint firstPoint = track.getFirstTrackPointBetween(selStart, selEnd);
		if (firstPoint != null) {
			breakList = ListUtils.makeList(new PointFlag(firstPoint, true));
		}
		Command command = createCommand(indexesToKeep, indexesToDelete, breakList, numMedia);
		if (command != null)
		{
			Describer undoDescriber = new Describer("undo.deletepoint", "undo.deletepoints");
			command.setDescription(undoDescriber.getDescriptionWithCount(indexesToDelete.size()));
			_app.execute(command);
		}
	}
}
